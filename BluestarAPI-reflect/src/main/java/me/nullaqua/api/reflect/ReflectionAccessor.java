package me.nullaqua.api.reflect;

import org.jetbrains.annotations.Nullable;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// 禁用警告：未使用、未检查、内部API
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

    /// 越权操作 ///

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

    /// 调用栈获取 ///

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
            final var method = ((MethodAccessor) m).getMethod();
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

    /// 深克隆 ///

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

    public static Class<?> getBothSuperClass(Class<?> a, Class<?> b)
    {
        if (a == null || b == null)
        {
            return Object.class;
        }
        List<Class<?>> aSuperClass = getAllSuperClass(a);
        List<Class<?>> bSuperClass = getAllSuperClass(b);
        for (Class<?> c: aSuperClass)
        {
            if (bSuperClass.contains(c))
            {
                return c;
            }
        }
        return Object.class;
    }

    public static List<FieldAccessor> getFields(Class<?> c)
    {
        List<FieldAccessor> accessors = new ArrayList<>();
        Arrays.asList(c.getDeclaredFields()).forEach(field -> accessors.add(new FieldAccessor(field)));
        return accessors;
    }

    public static List<FieldAccessor> getDeclaredFields(Class<?> c)
    {
        List<FieldAccessor> accessors = new ArrayList<>();
        for (var cc: getAllSuperClass(c))
        {
            accessors.addAll(getFields(cc));
        }
        return accessors;
    }

    private static List<FieldAccessor> getBothFields(Object a, Object b)
    {
        if (a == null || b == null)
        {
            return new ArrayList<>();
        }
        return getBothFields(a.getClass(), b.getClass());
    }

    private static List<FieldAccessor> getBothFields(Class<?> a, Class<?> b)
    {
        return FieldAccessor.getDeclaredFields(getBothSuperClass(a, b));
    }


    @SuppressWarnings("ConstantConditions")
    public static boolean isSimpleObject(Object o)
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

    public static <T> T cloneObject(T o) throws Throwable
    {
        if (null == o)
        {
            return null;
        }
        Map<Object, Object> map = new HashMap<>();
        return cloneObject(o, map);
    }

    private static <T> T cloneObject(T o, Map<Object, Object> map) throws Throwable
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
            newInstance = blankInstance(o.getClass());
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
            return cloneObject(o, map);
        }
        int len = Array.getLength(o);
        Object array = Array.newInstance(o.getClass().getComponentType(), len);
        map.put(o, array);
        for (int i = 0; i < len; i++)
        {
            if (Array.get(o, i) == null) continue;
            var x = cloneObject(Array.get(o, i), map);
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
        List<FieldAccessor> fields = getDeclaredFields(object.getClass());
        for (FieldAccessor f: fields)
        {
            if (!Modifier.isStatic(f.getField().getModifiers()))
            {
                f.set(newObject, cloneObject(f.get(object), map));
            }
        }
    }

    public static <T> T objectCopy(T o, T newObject) throws Throwable
    {
        Map<Object, Object> map = new HashMap<>();
        map.put(o, newObject);
        cloneFields(o, newObject, map);
        return o;
    }
}
