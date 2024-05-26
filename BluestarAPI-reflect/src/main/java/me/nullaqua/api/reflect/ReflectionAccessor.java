package me.nullaqua.api.reflect;

import org.jetbrains.annotations.Nullable;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "unchecked", "JavaLangClash", "JavaLangInvocation"})
@CallerSensitive
public final class ReflectionAccessor
{
    static final MethodHandles.Lookup LOOKUP;
    static final Unsafe UNSAFE;
    static final MethodType STATIC_FIELD_GETTER = MethodType.methodType(Object.class);
    static final MethodType STATIC_FIELD_SETTER = MethodType.methodType(Void.TYPE, Object.class);
    static final MethodType VIRTUAL_FIELD_GETTER = MethodType.methodType(Object.class, Object.class);
    static final MethodType VIRTUAL_FIELD_SETTER = MethodType.methodType(Void.TYPE, Object.class, Object.class);
    static final StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private ReflectionAccessor()
    {
    }

    static
    {
        Unsafe unsafe;
        MethodHandles.Lookup lookup;
        try
        {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = (Unsafe) theUnsafe.get(null);
            Field trustedLookup = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            long offset = unsafe.staticFieldOffset(trustedLookup);
            Object baseValue = unsafe.staticFieldBase(trustedLookup);
            lookup = (MethodHandles.Lookup) unsafe.getObject(baseValue, offset);
        }
        catch (Exception e)
        {
            lookup = MethodHandles.lookup();
            unsafe = null;
        }
        UNSAFE = unsafe;
        LOOKUP = lookup;
    }

    @Nullable
    public static Class<?> getClass(String name)
    {
        try
        {
            return Class.forName(name);
        }
        catch (Throwable e)
        {
            return null;
        }
    }

    static void checkVisibility(Class<?> clazz)
    {
        if (clazz.getPackage().equals(ReflectionAccessor.class.getPackage()))
        {
            // 不允许访问内部类
            throw new UnsupportedOperationException("This class cannot be accessed");
        }
    }

    //////////////////
    /// get caller ///
    //////////////////

    public static Invoker<?> getInvokerFromStackFrame(StackWalker.StackFrame stackFrame)
    {
        if (stackFrame == null) return null;
        try
        {
            if (stackFrame.getMethodName().equals("<clinit>"))
                return UnsafeOperation.getClinitMethod(stackFrame.getDeclaringClass());
            final var methodName = stackFrame.getMethodName();
            final var parameterArray = stackFrame.getMethodType().parameterArray();
            if (stackFrame.getMethodName().equals("<init>"))
            {
                final var con = stackFrame.getDeclaringClass().getDeclaredConstructor(parameterArray);
                return new ConstructorAccessor<>(con);
            }
            return new MethodAccessor(stackFrame.getDeclaringClass().getDeclaredMethod(methodName, parameterArray));
        }
        catch (Throwable e)
        {
            return null;
        }
    }

    private static final Predicate<? super StackWalker.StackFrame> filter = stackFrame ->
    {
        if (stackFrame.getDeclaringClass().isAnnotationPresent(CallerSensitive.class)) return false;
        final var m = ReflectionAccessor.getInvokerFromStackFrame(stackFrame);
        if (m == null) return false;
        if (m instanceof MethodAccessor)
        {
            final var method = ((MethodAccessor) m).getMethodOrNull();
            if (method == null) return true;
            return !method.isAnnotationPresent(CallerSensitive.class);
        }
        if (m instanceof ConstructorAccessor)
        {
            return !((ConstructorAccessor<?>) m).getConstructor().isAnnotationPresent(CallerSensitive.class);
        }
        return true;
    };

    @CallerSensitive
    public static List<StackWalker.StackFrame> getCallers()
    {
        return walker.walk(frames -> frames.filter(filter).collect(Collectors.toList()));
    }

    @CallerSensitive
    public static List<Class<?>> getCallerClasses()
    {
        return getCallers().stream().map(StackWalker.StackFrame::getDeclaringClass).collect(Collectors.toList());
    }

    @CallerSensitive
    public static List<Invoker<?>> getCallerMethods()
    {
        return getCallers().stream().map(ReflectionAccessor::getInvokerFromStackFrame).collect(Collectors.toList());
    }

    @CallerSensitive
    public static StackWalker.StackFrame getCaller()
    {
        return getCallers().stream().findFirst().orElse(null);
    }

    @CallerSensitive
    public static Invoker<?> getCallerMethod()
    {
        return getInvokerFromStackFrame(getCaller());
    }

    @CallerSensitive
    public static Class<?> getCallerClass()
    {
        final var caller = getCaller();
        if (caller != null) return caller.getDeclaringClass();
        else return null;
    }

    /////////////
    /// utils ///
    /////////////

    /**
     * 获取所有的父类(不包含Object.class)
     * @param type 类型
     * @return 所有的父类, 其中0为type本身, 之后依次为父类, 父类的父类, ...
     */
    public static List<Class<?>> getAllSuperClass(Class<?> type)
    {
        List<Class<?>> clazz = new ArrayList<>();
        if (type == null)
        {
            return clazz;
        }
        while (type != null && type != Object.class)
        {
            clazz.add(type);
            type = type.getSuperclass();
        }
        return clazz;
    }

    /**
     * 获取两个类的最近的共同父类, 即该类为两个类的共同父类, 且不存在某一类为该类的子类并也是两个类的共同父类
     * @param a 类a
     * @param b 类b
     * @return 两个类的最近的共同父类
     */
    public static Class<?> getBothSuperClass(Class<?> a, Class<?> b)
    {
        if (a == null || b == null)
        {
            return Object.class;
        }
        Set<Class<?>> aSuperClass = new HashSet<>(getAllSuperClass(a));
        while (b != null)
        {
            if (aSuperClass.contains(b))
                return b;
            b = b.getSuperclass();
        }
        return Object.class;
    }

    /**
     * 获取两个对象都拥有的字段, 即他们的所有共同父类的所有字段
     * @param a 对象a
     * @param b 对象b
     * @return 两个对象的共同父类的所有字段
     */
    public static List<FieldAccessor> getBothFields(Object a, Object b)
    {
        if (a == null || b == null)
        {
            return new ArrayList<>();
        }
        return getBothFields(a.getClass(), b.getClass());
    }

    /**
     * 获取两个类都拥有的字段, 即他们的所有共同父类的所有字段
     * @param a 类a
     * @param b 类b
     * @return 两个类的共同父类的所有字段
     */
    public static List<FieldAccessor> getBothFields(Class<?> a, Class<?> b)
    {
        return FieldAccessor.getFieldsInSuperClasses(getBothSuperClass(a, b));
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
        Map<Object, Object> map = new HashMap<>();
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
