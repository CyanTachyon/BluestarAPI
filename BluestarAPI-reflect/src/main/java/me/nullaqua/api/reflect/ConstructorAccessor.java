package me.nullaqua.api.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.util.Objects;

import static me.nullaqua.api.reflect.ReflectionAccessor.LOOKUP;

/**
 * 构造器访问器类
 *
 * @param <T> 泛型参数
 */
public class ConstructorAccessor<T> implements Invoker<T>
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
        Objects.requireNonNull(constructor);
        ReflectionAccessor.checkVisibility(constructor.getDeclaringClass());

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
     * 调用方法
     *
     * @param args 参数
     * @return 返回值
     * @throws Throwable 异常
     */
    @SuppressWarnings("unchecked")
    @Override
    public T invoke(Object... args) throws Throwable
    {
        if (constructorAccessor==null)
        {
            ReflectionAccessor.checkVisibility(constructor.getDeclaringClass());
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
}