package me.lanzhi.api.reflect;

import sun.reflect.ReflectionFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;

import static me.lanzhi.api.reflect.ReflectAccessor.LOOKUP;

/**
 * 构造器访问器类
 *
 * @param <T> 泛型参数
 */
public class ConstructorAccessor<T>
{
    private final Constructor<T> constructor;
    private final MethodHandle constructorAccessor;

    /**
     * 构造方法
     *
     * @param constructor 构造器
     */
    public ConstructorAccessor(Constructor<T> constructor)
    {
        if (constructor==null||!ReflectAccessor.isVisibility(constructor.getDeclaringClass()))
        {
            this.constructor=null;
            constructorAccessor=null;
            return;
        }

        MethodHandle target;
        MethodType methodType;
        try
        {
            MethodHandle unreflected=LOOKUP.unreflectConstructor(constructor);
            target=unreflected.asFixedArity();
            int paramCount=unreflected.type().parameterCount();
            methodType=MethodType.genericMethodType(0,true);
            target=target.asSpreader(Object[].class,paramCount);
        }
        catch (Exception e)
        {
            this.constructor=constructor;
            this.constructorAccessor=null;
            return;
        }


        this.constructor=constructor;
        this.constructorAccessor=target.asType(methodType);
    }

    /**
     * 获取构造器
     *
     * @param c       类型
     * @param classes 参数类型
     * @param <T>     泛型参数
     * @return 构造器访问器
     */
    public static <T> ConstructorAccessor<T> getConstructor(Class<T> c,Class<?>... classes)
    {
        if (c==null)
        {
            return null;
        }
        classes=classes==null?new Class[0]:classes;
        try
        {
            return new ConstructorAccessor<>(c.getConstructor(classes));
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * 获取声明的构造器
     *
     * @param c       类型
     * @param classes 参数类型
     * @param <T>     泛型参数
     * @return 构造器访问器
     */
    public static <T> ConstructorAccessor<T> getDeclaredConstructor(Class<T> c,Class<?>... classes)
    {
        if (c==null)
        {
            return null;
        }
        classes=classes==null?new Class[0]:classes;
        try
        {
            return new ConstructorAccessor<>(c.getDeclaredConstructor(classes));
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * 创建空的构造器
     *
     * @param type 类型
     * @param <T>  泛型参数
     * @return 构造器访问器
     * @throws Throwable 异常
     */
    public static <T> ConstructorAccessor<T> createBlankConstructor(Class<T> type) throws Throwable
    {
        return createConstructor(type,null);
    }

    /**
     * 创建构造器
     *
     * @param t           类型
     * @param constructor 构造器
     * @param <T>         泛型参数
     * @return 构造器访问器
     * @throws Throwable 异常
     */
    private static <T> ConstructorAccessor<T> createConstructor(Class<T> t,Constructor<?> constructor) throws Throwable
    {
        if (constructor==null)
        {
            constructor=ConstructorAccessor.getDeclaredConstructor(Object.class).getConstructor();
        }
        Class<?> c=ReflectionFactory.class;
        FieldAccessor accessor=FieldAccessor.getDeclaredField(c,"delegate");
        Object o=accessor.get(null);
        if (o==null) throw new IllegalStateException("ReflectionFactory delegate is null");
        var co=(Constructor<?>) MethodAccessor.getDeclaredMethod(o.getClass(),
                                                                 "generateConstructor",
                                                                 Class.class,
                                                                 Constructor.class).invoke(o,t,constructor);
        var ac=MethodAccessor.getDeclaredMethod(o.getClass(),"getConstructorAccessor",Constructor.class).invoke(o,co);
        if (ac==null) throw new IllegalStateException("ConstructorAccessor is null");
        return (K<T>) new K<>(co,ac);
    }

    /**
     * 调用方法
     *
     * @param args 参数
     * @return 返回值
     * @throws Throwable 异常
     */
    public T invoke(Object... args) throws Throwable
    {
        if (constructorAccessor==null)
        {
            if (!ReflectAccessor.isVisibility(constructor.getDeclaringClass()))
            {
                return null;
            }
            constructor.setAccessible(true);
            T o=constructor.newInstance(args);
            constructor.setAccessible(false);
            return o;
        }
        return (T) this.constructorAccessor.invokeExact(args);
    }

    /**
     * 获取构造器
     *
     * @return 构造器
     */
    public Constructor<T> getConstructor()
    {
        return this.constructor;
    }

    /**
     * K类
     *
     * @param <T> 泛型参数
     */
    private static class K<T> extends ConstructorAccessor<T>
    {
        private final Object instance;
        private final Constructor<T> constructor;
        private final MethodAccessor methodAccessor;

        /**
         * 构造方法
         *
         * @param constructor 构造器
         * @param instance    实例
         */
        public K(Constructor<T> constructor,Object instance)
        {
            super(null);
            this.instance=instance;
            this.constructor=constructor;
            methodAccessor=MethodAccessor.getDeclaredMethod(instance.getClass(),"newInstance",Object[].class);
        }

        /**
         * 调用方法
         *
         * @param args 参数
         * @return 返回值
         * @throws Throwable 异常
         */
        @Override
        public T invoke(Object... args) throws Throwable
        {
            return (T) methodAccessor.invoke(instance,args);
        }

        /**
         * 获取构造器
         *
         * @return 构造器
         */
        @Override
        public Constructor<T> getConstructor()
        {
            return constructor;
        }
    }
}