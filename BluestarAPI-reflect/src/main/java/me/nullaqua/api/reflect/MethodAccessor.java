package me.nullaqua.api.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static me.nullaqua.api.reflect.ReflectionAccessor.LOOKUP;

public final class MethodAccessor implements Invoker<Object>
{
    private static final Class<?> javaMethodAccessor;
    private static MethodAccessor javaMethodAccessorInvoke = null;

    static
    {
        try
        {
            javaMethodAccessor = Class.forName("jdk.internal.reflect.MethodAccessor");
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

    private final Method method;
    private final boolean staticMethod;
    private final Object methodHandle;

    public MethodAccessor(Method method)
    {
        Objects.requireNonNull(method);
        ReflectionAccessor.checkVisibility(method.getDeclaringClass());

        MethodHandle unreflected;
        boolean staticMethod = Modifier.isStatic(method.getModifiers());
        try
        {
            unreflected = LOOKUP.unreflect(method);
        }
        catch (Exception e)
        {
            this.method = method;
            this.methodHandle = null;
            this.staticMethod = staticMethod;
            return;
        }

        MethodHandle target = unreflected.asFixedArity();
        int paramCount = unreflected.type().parameterCount()-(staticMethod?0:1);
        MethodType methodType = MethodType.genericMethodType(1, true);
        target = target.asSpreader(Object[].class, paramCount);
        if (staticMethod)
        {
            target = MethodHandles.dropArguments(target, 0, Object.class);
        }
        MethodHandle generified = target.asType(methodType);

        this.method = method;
        this.methodHandle = generified;
        this.staticMethod = staticMethod;
    }

    MethodAccessor(boolean staticMethod, Object methodAccessor)
    {
        this.method = null;
        this.methodHandle = methodAccessor;
        this.staticMethod = staticMethod;
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
            return new MethodAccessor(clazz.getDeclaredMethod(name, classes));
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static MethodAccessor getDeclaredMethod(Class<?> c, String name, Class<?>... classes)
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
                return new MethodAccessor(clazz.getDeclaredMethod(name, classes));
            }
            catch (Exception ignored)
            {
            }
        }
        return null;
    }

    public static List<MethodAccessor> getDeclaredMethods(Class<?> c)
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

    public static List<MethodAccessor> getDeclaredMethods(Object o)
    {
        if (o == null)
        {
            return Collections.emptyList();
        }
        return getDeclaredMethods(o.getClass());
    }

    public Object invokeMethod(Object target, Object... args) throws Throwable
    {
        if (methodHandle == null)
        {
            method.setAccessible(true);
            Object o = method.invoke(target, args);
            method.setAccessible(false);
            return o;
        }
        if (javaMethodAccessor.isInstance(methodHandle))
        {;
            return getJavaMethodAccessorInvoke().invokeMethod(methodHandle, target, args);
        }
        return ((MethodHandle) methodHandle).invoke(target, args);
    }

    @Override
    public Object invoke(Object... args) throws Throwable
    {
        if (args.length == 0) throw new IllegalArgumentException("No target provided");
        return invokeMethod(args[0], Arrays.copyOfRange(args, 1, args.length));
    }

    public Method getMethod()
    {
        return this.method;
    }

    public boolean isStaticMethod()
    {
        return staticMethod;
    }
}
