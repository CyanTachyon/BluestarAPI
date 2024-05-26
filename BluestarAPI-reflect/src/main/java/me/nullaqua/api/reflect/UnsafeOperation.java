package me.nullaqua.api.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;

import static me.nullaqua.api.reflect.ReflectionAccessor.LOOKUP;
import static me.nullaqua.api.reflect.ReflectionAccessor.UNSAFE;

@SuppressWarnings("unused")
public class UnsafeOperation
{
    UnsafeOperation()
    {
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

    public static MethodAccessor getClinitMethod(Class<?> clazz) throws Throwable
    {
        final var h = LOOKUP.unreflect(UnsafeOperation.class.getDeclaredMethod("initClass", Class.class));
        return new MethodAccessor(clazz, h, "<clinit>", MethodType.methodType(Void.TYPE, Class.class));
    }

    private static void initClass(Class<?> clazz) throws Throwable
    {
        Class.forName(clazz.getName(), true, clazz.getClassLoader());
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
}