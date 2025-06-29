package me.nullaqua.api.reflect;

import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static me.nullaqua.api.reflect.ReflectionAccessor.LOOKUP;

@SuppressWarnings("unused")
public final class MethodAccessor implements Invoker<Object>
{
    private final static MethodAccessor getMethods;

    static
    {
        try
        {
            getMethods = new MethodAccessor(Class.class.getDeclaredMethod("getDeclaredMethods0", boolean.class));
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static final Class<?> javaMethodAccessor;
    private static MethodAccessor javaMethodAccessorInvoke = null;
    private static final Class<?> javaConstructorAccessor;
    private static MethodAccessor javaConstructorAccessorInvoke = null;

    static
    {
        try
        {
            javaMethodAccessor = Class.forName("jdk.internal.reflect.MethodAccessor");
            javaConstructorAccessor = Class.forName("jdk.internal.reflect.ConstructorAccessor");
        }
        catch (Exception e)
        {
            throw new ExceptionInInitializerError(e);
        }
    }

    static MethodAccessor getJavaMethodAccessorInvoke() throws NoSuchMethodException
    {
        if (javaMethodAccessorInvoke != null) return javaMethodAccessorInvoke;
        Method method = javaMethodAccessor.getDeclaredMethod("invoke", Object.class, Object[].class);
        javaMethodAccessorInvoke = new MethodAccessor(method);
        return javaMethodAccessorInvoke;
    }

    static MethodAccessor getJavaConstructorAccessorInvoke() throws NoSuchMethodException
    {
        if (javaConstructorAccessorInvoke != null) return javaConstructorAccessorInvoke;
        Method method = javaConstructorAccessor.getDeclaredMethod("newInstance", Object[].class);
        javaConstructorAccessorInvoke = new MethodAccessor(method);
        return javaConstructorAccessorInvoke;
    }

    public final Object methodHandle;
    private final String methodName;
    private final MethodType methodType;
    private final Class<?> declaringClass;

    public MethodAccessor(Method method)
    {
        Object handler = method;
        Objects.requireNonNull(method);
        ReflectionAccessor.checkVisibility(method.getDeclaringClass());

        try
        {
            MethodHandle unreflected;
            boolean staticMethod = Modifier.isStatic(method.getModifiers());
            unreflected = LOOKUP.unreflect(method);
            MethodHandle target = unreflected.asFixedArity();
            int paramCount = unreflected.type().parameterCount()-(staticMethod?0:1);
            MethodType methodType = MethodType.genericMethodType(1, true);
            target = target.asSpreader(Object[].class, paramCount);
            if (staticMethod)
            {
                target = MethodHandles.dropArguments(target, 0, Object.class);
            }
            handler = target.asType(methodType);
        }
        catch (Throwable ignored)
        {
            ignored.printStackTrace();
            method.setAccessible(true);
        }
        this.methodHandle = handler;
        this.methodName = method.getName();
        this.declaringClass = method.getDeclaringClass();
        this.methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
    }

    MethodAccessor(Class<?> declaringClass, Object methodAccessor, String methodName, MethodType methodType)
    {
        this.methodHandle = methodAccessor;
        this.methodName = methodName;
        this.methodType = methodType;
        this.declaringClass = declaringClass;
    }

    public static MethodAccessor makeAccessorWithoutLookup(Method method)
    {
        return new MethodAccessor(
            method.getDeclaringClass(),
            method,
            method.getName(),
            MethodType.methodType(method.getReturnType(), method.getParameterTypes())
        );
    }

    public static List<Method> getJvmMethods(Class<?> clazz)
    {
        if (clazz == null)
        {
            return null;
        }
        try
        {
            return new ArrayList<>(Arrays.asList((Method[]) getMethods.invokeMethod(clazz, false)));
        }
        catch (Throwable e)
        {
            return null;
        }
    }

    public static List<MethodAccessor> getMethods(Class<?> clazz)
    {
        if (clazz == null)
        {
            return null;
        }
        List<MethodAccessor> list = new ArrayList<>();
        for (Method method: getJvmMethods(clazz))
        {
            list.add(new MethodAccessor(method));
        }
        return list;
    }

    public static MethodAccessor getMethod(Class<?> clazz, String name, Class<?>... classes)
    {
        if (clazz == null || name == null)
        {
            return null;
        }
        classes = classes == null?new Class[0]:classes;
        try
        {
            for (MethodAccessor method: getMethods(clazz))
            {
                if (method.methodName.equals(name) && Arrays.equals(method.methodType().parameterArray(), classes))
                {
                    return method;
                }
            }
            return null;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * 在一个类的所有父类中查找方法, 有同名且参数类型相同的方法则返回子类中的方法
     * @param c 类
     * @param name 方法名
     * @param classes 参数类型
     * @return 方法访问器, 若不存在则返回null
     */
    public static MethodAccessor getMethodInSuperClasses(Class<?> c, String name, Class<?>... classes)
    {
        if (c == null || name == null)
        {
            return null;
        }
        classes = classes == null?new Class[0]:classes;
        for (Class<?> clazz: ReflectionAccessor.getAllSuperClass(c))
        {
            try
            {
                return getMethod(clazz, name, classes);
            }
            catch (Exception ignored)
            {
            }
        }
        return null;
    }

    /**
     * 获取一个类的所有方法, 包括父类的方法
     * @param c 类
     * @return 方法访问器列表
     */
    public static List<MethodAccessor> getMethodsInSuperClasses(Class<?> c)
    {
        List<MethodAccessor> list = new ArrayList<>();
        for (Method method: c.getDeclaredMethods())
        {
            list.add(new MethodAccessor(method));
        }
        c = c.getSuperclass();
        for (Class<?> clazz: ReflectionAccessor.getAllSuperClass(c))
        {
            for (Method method: clazz.getDeclaredMethods())
            {
                if (!Modifier.isStatic(method.getModifiers()))
                {
                    list.add(new MethodAccessor(method));
                }
            }
        }
        return list;
    }

    /**
     * 返回一个对象的所有方法, 包括父类的方法
     * @param o 对象
     * @return 方法访问器列表
     */
    public static List<MethodAccessor> getMethodsInSuperClasses(Object o)
    {
        if (o == null)
        {
            return Collections.emptyList();
        }
        return getMethodsInSuperClasses(o.getClass());
    }

    public Object invokeMethod(Object target, Object... args) throws Throwable
    {
        if (methodHandle instanceof MethodHandle)
        {
            return ((MethodHandle) methodHandle).invoke(target, args);
        }
        if (methodHandle instanceof Method)
        {
            return ((Method) methodHandle).invoke(target, args);
        }
        if (javaMethodAccessor.isInstance(methodHandle))
        {
            return getJavaMethodAccessorInvoke().invokeMethod(methodHandle, target, args);
        }
        if (javaConstructorAccessor.isInstance(methodHandle))
        {
            return getJavaConstructorAccessorInvoke().invokeMethod(methodHandle, args);
        }
        throw new UnsupportedOperationException("Unsupported method handle type: "+methodHandle.getClass());
    }

    @Override
    public Object invoke(Object... args) throws Throwable
    {
        if (args.length == 0) throw new IllegalArgumentException("No target provided");
        return invokeMethod(args[0], Arrays.copyOfRange(args, 1, args.length));
    }

    public String methodName()
    {
        return methodName;
    }

    public Class<?> declaringClass()
    {
        return declaringClass;
    }

    public MethodType methodType()
    {
        return methodType;
    }

    @Nullable
    public Method getMethodOrNull()
    {
        if (methodHandle instanceof Method) return (Method) methodHandle;
        try
        {
            for (Method method: getJvmMethods(declaringClass))
            {
                if (method.getName().equals(methodName) && method.getParameterCount() == methodType.parameterCount())
                {
                    return method;
                }
            }
            return null;
        }
        catch (Throwable e)
        {
            return null;
        }
    }

    @Override
    public String toString()
    {
        return "MethodAccessor{"+
            "methodHandle="+methodHandle+
            ", methodName='"+methodName+'\''+
            ", methodType="+methodType+
            ", declaringClass="+declaringClass+
            '}';
    }
}
