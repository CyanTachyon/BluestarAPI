package me.nullaqua.api.reflect;

import sun.reflect.ReflectionFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import static me.nullaqua.api.reflect.ReflectionAccessor.LOOKUP;
import static me.nullaqua.api.reflect.ReflectionAccessor.UNSAFE;

@SuppressWarnings("unused")
public class UnsafeOperation
{
    UnsafeOperation(LOCK lock)
    {
        Objects.requireNonNull(lock);
        lock.check().check().check();
    }

    private static final MethodAccessor generateMethod;
    private static final Object methodAccessorGenerator;

    static
    {
        try
        {
            Class<?> c = Class.forName("jdk.internal.reflect.MethodAccessorGenerator");
            Constructor<?> con = c.getDeclaredConstructor();
            ConstructorAccessor<?> conAccessor = new ConstructorAccessor<>(con);
            Method method = c.getDeclaredMethod("generateMethod",
                                                Class.class,
                                                String.class,
                                                Class[].class,
                                                Class.class,
                                                Class[].class,
                                                int.class
            );
            generateMethod = new MethodAccessor(method);
            methodAccessorGenerator = conAccessor.invoke();
        }
        catch (Throwable e)
        {
            throw new ExceptionInInitializerError(e);
        }
    }

    //////////////////////
    /// blank instance ///
    //////////////////////

    @SuppressWarnings("unchecked")
    public static <T> T blankInstance(Class<T> type) throws Throwable
    {
        return (T) UNSAFE.allocateInstance(type);
    }

    public static void eraseToBlank(Object obj) throws Throwable
    {
        if (obj == null) return;
        var blank = blankInstance(obj.getClass());
        for (var field: FieldAccessor.getFieldsInSuperClasses(obj))
        {
            field.set(obj, field.get(blank));
        }
    }

    /**
     * 在成功时会返回一个新的Void类的实例
     *
     * @return void实例
     * @throws Throwable 当反射出现问题
     */
    public static Void voidInstance() throws Throwable
    {
        return blankInstance(Void.class);
    }

    private static <T> MethodAccessor createConstructor(Class<T> t,Constructor<?> constructor) throws Throwable
    {
        if (constructor==null)
        {
            constructor=ConstructorAccessor.getDeclaredConstructor(Object.class).getConstructor();
        }
        Class<?> c= ReflectionFactory.class;
        FieldAccessor accessor=FieldAccessor.getField(c,"delegate");
        Object o=accessor.get(null);
        if (o==null) throw new IllegalStateException("ReflectionFactory delegate is null");
        var co=(Constructor<?>) MethodAccessor.getMethod(o.getClass(),
                                                                 "generateConstructor",
                                                                 Class.class,
                                                                 Constructor.class).invoke(o,t,constructor);
        var ac=MethodAccessor.getMethod(o.getClass(),"getConstructorAccessor",Constructor.class).invoke(o,co);
        if (ac==null) throw new IllegalStateException("ConstructorAccessor is null");
        return new MethodAccessor(t, ac, "<init>", MethodType.methodType(Void.TYPE, constructor.getParameterTypes()));
    }

    /////////////////////
    /// <init> method ///
    /////////////////////

    public static MethodAccessor getInitMethod(Constructor<?> constructor) throws Throwable
    {
        Object m = generateMethod.invokeMethod(methodAccessorGenerator,
                                               constructor.getDeclaringClass(),
                                               "<init>",
                                               constructor.getParameterTypes(),
                                               void.class,
                                               constructor.getExceptionTypes(),
                                               constructor.getModifiers()
        );
        return new MethodAccessor(constructor.getDeclaringClass(),
                                  m,
                                  "<init>",
                                  MethodType.methodType(Void.TYPE, constructor.getParameterTypes())
        );
    }

    public static MethodAccessor getClinitMethod(Class<?> clazz)
    {
        // 由于<clinit>方法只能被虚拟机调用，因此暂时没有调用<clinit>方法的方式
        // 因此该方法现在返回的是一个空的MethodAccessor，无法实际调用<clinit>方法
        // 计划未来通过运行时生成字节码的方式，生成一个字节码与<clinit>方法相同的方法，
        // 以达到调用<clinit>方法的大致等同的效果
        return MethodAccessor.emptyMethodAccessor(clazz, "<clinit>", MethodType.methodType(Void.TYPE, Class.class));
    }

    public static <T> T invokeConstructorStepByStep(Constructor<T> constructor, Object... args) throws Throwable
    {
        final var initMethod = getInitMethod(constructor);
        final var res = blankInstance(constructor.getDeclaringClass());
        initMethod.invoke(res, args);
        return res;
    }

    ///////////////////////////////
    /// invoke without override ///
    ///////////////////////////////

    public static Object invokeWithoutOverride(Method method, Object instance, Object... args) throws Throwable
    {
        return LOOKUP.findSpecial(method.getDeclaringClass(),
                                  method.getName(),
                                  MethodType.methodType(method.getReturnType(), method.getParameterTypes()),
                                  method.getDeclaringClass()
        ).bindTo(instance).invokeWithArguments(args);
    }

    ////////////////////
    /// setModifiers ///
    ////////////////////

    private static final MethodHandle constructorModifiersSetter;
    private static final MethodHandle methodModifiersSetter;
    private static final MethodHandle fieldModifiersSetter;
    private static final MethodHandle memberNameModifiersSetter;

    static
    {
        try
        {
            constructorModifiersSetter = LOOKUP.findSetter(Constructor.class, "modifiers", int.class);
            methodModifiersSetter = LOOKUP.findSetter(Method.class, "modifiers", int.class);
            fieldModifiersSetter = LOOKUP.findSetter(Field.class, "modifiers", int.class);
            memberNameModifiersSetter = LOOKUP.findSetter(Class.forName("java.lang.invoke.MemberName"),
                                                          "flags",
                                                          int.class
            );
        }
        catch (Throwable e)
        {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static void setModifiers(Member member, int modifiers) throws Throwable
    {
        if (member instanceof Constructor<?>) setModifiers((Constructor<?>) member, modifiers);
        else if (member instanceof Method) setModifiers((Method) member, modifiers);
        else if (member instanceof Field) setModifiers((Field) member, modifiers);
        else if (member.getClass().getName().equals("java.lang.invoke.MemberName"))
            setModifiers((Object) member, modifiers);
        else throw new IllegalArgumentException("Unsupported member type: "+member.getClass().getName());
    }

    public static void setModifiers(Constructor<?> constructor, int modifiers) throws Throwable
    {
        constructorModifiersSetter.invoke(constructor, modifiers);
    }

    public static void setModifiers(Method method, int modifiers) throws Throwable
    {
        methodModifiersSetter.invoke(method, modifiers);
    }

    public static void setModifiers(Field field, int modifiers) throws Throwable
    {
        fieldModifiersSetter.invoke(field, modifiers);
    }

    public static void setModifiers(Object /*java.lang.invoke.MemberName*/ member, int modifiers) throws Throwable
    {
        memberNameModifiersSetter.invoke(member, modifiers);
    }

    /////////////////////
    /// create method ///
    /// /////////////////

    private static final Map<String, FieldAccessor> methodFields = new IdentityHashMap<>();
    private static FieldAccessor getMethodField(String name)
    {
        return methodFields.computeIfAbsent(name, n -> FieldAccessor.getField(Method.class, n));
    }

    public static Method createMethod(
        Class<?> clazz,
        int slot,
        String name,
        Class<?> returnType,
        Class<?>[] parameterTypes,
        Class<?>[] exceptionTypes,
        int modifiers,
        String signature,
        byte[] annotations,
        byte[] parameterAnnotations,
        byte[] annotationDefault,
        boolean checkArgs,
        BiFunction<Object, Object[], Object> impl
    ) throws Throwable
    {
        Method method = UnsafeOperation.blankInstance(Method.class);
        getMethodField("clazz").set(method, clazz);
        getMethodField("slot").set(method, slot);
        getMethodField("name").set(method, name);
        getMethodField("returnType").set(method, returnType);
        getMethodField("parameterTypes").set(method, parameterTypes);
        getMethodField("exceptionTypes").set(method, exceptionTypes);
        getMethodField("modifiers").set(method, modifiers);
        getMethodField("signature").set(method, signature);
        getMethodField("annotations").set(method, annotations);
        getMethodField("parameterAnnotations").set(method, parameterAnnotations);
        getMethodField("annotationDefault").set(method, annotationDefault);
        if (!checkArgs) CustomJdkMethodAccessor.replaceMethodAccessor(method, impl);
        else CustomJdkMethodAccessor.replaceMethodAccessor(method, (m, args) -> {
            if (args.length != parameterTypes.length)
                throw new IllegalArgumentException("Wrong number of arguments");
            for (int i = 0; i < args.length; i++)
            {
                if (args[i] != null && !parameterTypes[i].isInstance(args[i]))
                {
                    throw new IllegalArgumentException("Argument " + i + " is not an instance of " + parameterTypes[i]);
                }
            }
            return impl.apply(m, args);
        });
        return method;
    }

    ///////////////////
    /// force clone ///
    ///////////////////

    /**
     * 强制深克隆一个对象, 即克隆对象的所有字段, 包括数组和对象.
     * 理论上所有字段都会被强制克隆, 即使是一些不应存在两个的对象(例如线程, 类加载器, 枚举等).
     * @param o 对象
     * @return 克隆的对象
     * @param <T> 对象类型
     * @throws Throwable 异常
     */
    public static <T> T forceDeepObject(T o) throws Throwable
    {
        if (null == o)
        {
            return null;
        }
        Map<Object, Object> map = new IdentityHashMap<>();
        return forceDeepObject(o, map);
    }

    @SuppressWarnings("ConstantConditions")
    private static boolean isSimpleObject(Object o)
    {
        Class<?> type = o.getClass();
        return type.isPrimitive() ||
               type.equals(String.class) ||
               type.equals(Long.class) ||
               type.equals(Boolean.class) ||
               type.equals(Short.class) ||
               type.equals(Integer.class) ||
               type.equals(Character.class) ||
               type.equals(Float.class) ||
               type.equals(Double.class) ||
               type.equals(Void.class) ||
               type.equals(Byte.class) ||
               type.isEnum() ||
               type == Class.class ||
               type == Method.class ||
               type == Constructor.class;
    }

    @SuppressWarnings("unchecked")
    private static <T> T forceDeepObject(T o, Map<Object, Object> map) throws Throwable
    {
        if (o == null)
        {
            return null;
        }
        if (isSimpleObject(o))
        {
            return o;
        }
        Object newInstance = map.get(o);
        if (newInstance != null)
        {
            return (T) newInstance;
        }

        if (o.getClass().isArray())
        {
            newInstance = cloneArray(o, map);
        }
        else
        {
            newInstance = UnsafeOperation.blankInstance(o.getClass());
        }
        map.put(o, newInstance);
        cloneFields(o, newInstance, map);
        return (T) newInstance;
    }

    private static Object cloneArray(Object o, Map<Object, Object> map) throws Throwable
    {
        if (null == o)
        {
            return null;
        }
        if (!o.getClass().isArray())
        {
            return forceDeepObject(o, map);
        }
        int len = Array.getLength(o);
        Object array = Array.newInstance(o.getClass().getComponentType(), len);
        map.put(o, array);
        for (int i = 0; i < len; i++)
        {
            if (Array.get(o, i) == null) continue;
            var x = forceDeepObject(Array.get(o, i), map);
            Array.set(array, i, x);
        }
        return array;
    }

    private static void cloneFields(Object object, Object newObject, Map<Object, Object> map) throws Throwable
    {
        if (object == null || newObject == null)
        {
            return;
        }
        List<FieldAccessor> fields = FieldAccessor.getFieldsInSuperClasses(object.getClass());
        for (FieldAccessor f: fields)
        {
            if (!Modifier.isStatic(f.getField().getModifiers()))
            {
                f.set(newObject, forceDeepObject(f.get(object), map));
            }
        }
    }
}