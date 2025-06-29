package me.nullaqua.api.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.BiFunction;

public class CustomJdkMethodAccessor
{
    private static final FieldAccessor methodAccessorField;

    static
    {
        try
        {
            methodAccessorField = FieldAccessor.getField(Method.class, "methodAccessor");
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * 返回一个JDK动态代理对象，代理的接口是jdk.internal.reflect.MethodAccessor
     * @param impl 代理对象的实现
     * @return 代理对象
     */
    public static Object makeJdkMethodAccessor(BiFunction<Object, Object[], Object> impl)
    {
        return makeJdkMethodAccessor(impl, null);
    }

    /**
     * 返回一个JDK动态代理对象，代理的接口是jdk.internal.reflect.MethodAccessor
     * @param impl 代理对象的实现
     * @param classLoader 类加载器
     * @return 代理对象
     */
    public static Object makeJdkMethodAccessor(BiFunction<Object, Object[], Object> impl, ClassLoader classLoader)
    {
        if (classLoader == null) classLoader = CustomJdkMethodAccessor.class.getClassLoader();
        Class<?> c;
        try
        {
            c = Class.forName("jdk.internal.reflect.MethodAccessor");
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException("jdk.internal.reflect.MethodAccessor not found");
        }
        return Proxy.newProxyInstance(classLoader, new Class[]{c}, new ProxyImpl(impl));
    }

    public static void replaceMethodAccessor(Method method, BiFunction<Object, Object[], Object> impl) throws Throwable
    {
        replaceMethodAccessor(method, impl, null);
    }

    /**
     * 通过替换method对象的methodAccessor字段，来替换method的实现，但仅能对一个method对象生效
     * @param method 要替换的method对象
     * @param impl 新的实现
     * @param classLoader 类加载器
     */
    public static void replaceMethodAccessor(Method method, BiFunction<Object, Object[], Object> impl, ClassLoader classLoader)
        throws Throwable
    {
        Object methodAccessor = makeJdkMethodAccessor(impl, classLoader);
        try
        {
            methodAccessorField.set(method, methodAccessor);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private static class ProxyImpl implements InvocationHandler
    {
        private final BiFunction<Object, Object[], Object> impl;

        public ProxyImpl(BiFunction<Object, Object[], Object> impl)
        {
            this.impl = impl;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
        {
            final var this_ = args[0];
            final var args_ = (Object[]) args[1];
            return impl.apply(this_, args_);
        }
    }
}
