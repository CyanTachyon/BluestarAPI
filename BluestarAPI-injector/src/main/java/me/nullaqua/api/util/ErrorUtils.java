package me.nullaqua.api.util;

import me.nullaqua.api.util.function.ConsumerWithThrow;
import me.nullaqua.api.util.function.RunWithThrow;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Utility class for handling errors.
 */
public class ErrorUtils
{
    /**
     * A consumer that throws the given throwable.
     */
    private static final Consumer<Throwable> thrower;

    static
    {
        Method method;
        try
        {
            method=ErrorUtils.class.getDeclaredMethod("forceThrow0",Throwable.class);
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
        thrower=throwable->
        {
            try
            {
                method.invoke(null,throwable);
            }
            catch (IllegalAccessException|InvocationTargetException e)
            {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * A map that stores the class and its corresponding ConsumerWithThrow.
     */
    private final Map<Class,ConsumerWithThrow> map=new HashMap<>();

    /**
     * Throws the given throwable.
     *
     * @param t The throwable to be thrown.
     * @throws Throwable The throwable to be thrown.
     */
    private static void forceThrow0(Throwable t) throws Throwable
    {
        throw t;
    }

    /**
     * 添加一个错误处理器
     *
     * @param t        错误类型
     * @param consumer 错误处理器
     * @param <T>      错误类型
     */
    public final <T extends Throwable> void add(Class<T> t,ConsumerWithThrow<T> consumer)
    {
        synchronized (this)
        {
            map.put(t,consumer);
        }
    }

    /**
     * 通过错误处理器处理错误,若处理后仍有指定类型的错误则直接抛出,其他错误则转为RuntimeException抛出
     *
     * @param throwable 待处理的错误
     * @param x         指定的错误类型
     */
    public void handleException(Throwable throwable,Class<? extends Throwable>... x)
    {
        HashSet<Class> set=new HashSet<>();
        Collections.addAll(set,x);
        handleException(throwable,t->
        {
            Class c=t.getClass();
            while (Throwable.class.isAssignableFrom(c))
            {
                if (set.contains(c))
                {
                    forceThrow(t);
                    return;
                }
                c=c.getSuperclass();
            }
            throw new RuntimeException(t);
        });
    }

    /**
     * 通过错误处理器处理错误,如果没有找到对应的错误处理器,或错误处理器抛出的错误,则使用指定的错误处理器处理
     *
     * @param throwable 待处理的错误
     * @param consumer  指定的错误处理器
     */
    public void handleException(Throwable throwable,Consumer<Throwable> consumer)
    {
        try
        {
            throwException(throwable);
        }
        catch (Throwable t)
        {
            consumer.accept(t);
        }
    }

    /**
     * 强制抛出一个未拓展自RuntimeException的Throwable
     *
     * @param t 一个未拓展自RuntimeException的Throwable
     */
    public static void forceThrow(Throwable t)
    {
        thrower.accept(t);
    }

    /**
     * 通过错误处理器处理错误,如果没有找到对应的错误处理器,则直接抛出错误
     *
     * @param throwable 待处理的错误
     * @throws Throwable 未找到对应的错误处理器时直接抛出,或错误处理器抛出的错误
     */
    public void throwException(Throwable throwable) throws Throwable
    {
        synchronized (this)
        {
            Class t=throwable.getClass();
            while (Throwable.class.isAssignableFrom(t))
            {
                if (map.containsKey(t))
                {
                    map.get(t).accept(throwable);
                    return;
                }
                t=t.getSuperclass();
            }
            throw throwable;
        }
    }

    /**
     * 通过错误处理器处理错误,若处理后仍有指定类型的错误则直接抛出,其他错误则使用指定的错误处理器处理
     *
     * @param throwable 待处理的错误
     * @param c         指定的错误处理器
     * @param x         指定的错误类型
     */
    public void handleException(Throwable throwable,Consumer<Throwable> c,Class<? extends Throwable>... x)
    {
        HashSet<Class> set=new HashSet<>();
        Collections.addAll(set,x);
        handleException(throwable,t->
        {
            Class aClass=t.getClass();
            while (Throwable.class.isAssignableFrom(aClass))
            {
                if (set.contains(aClass))
                {
                    forceThrow(t);
                    return;
                }
                aClass=aClass.getSuperclass();
            }
            c.accept(t);
        });
    }

    /**
     * 运行指定的代码,若有错误则使用错误处理器处理,若未找到对应的错误处理器,或错误处理器抛出的错误,则转为RuntimeException抛出
     *
     * @param runnable 指定的代码
     */
    public void handle(RunWithThrow runnable)
    {
        try
        {
            runnable.run();
        }
        catch (Throwable throwable)
        {
            handleException(throwable);
        }
    }

    /**
     * 通过错误处理器处理错误,如果没有找到对应的错误处理器,或错误处理器抛出的错误,则转为RuntimeException抛出
     *
     * @param throwable 待处理的错误
     */
    public void handleException(Throwable throwable)
    {
        handleException(throwable,t->
        {
            if (t instanceof RuntimeException)
            {
                throw (RuntimeException) t;
            }
            else
            {
                throw new RuntimeException(t);
            }
        });
    }

    /**
     * 运行指定的代码,若有错误则使用错误处理器处理,若处理后仍有指定类型的错误则直接抛出,其他错误则转为RuntimeException抛出
     *
     * @param run 指定的代码
     * @param x   指定的错误类型
     * @param <T> 指定的错误类型
     * @throws T 指定的错误类型
     */
    public <T extends Throwable> void handle(RunWithThrow run,Class<T> x) throws T
    {
        try
        {
            run.run();
        }
        catch (Throwable throwable)
        {
            handleException(throwable,x);
        }
    }

    //--Force to throw Throwable which is not extended from RuntimeException--//

    /**
     * 通过错误处理器处理错误,若处理后仍有指定类型的错误则直接抛出,其他错误则转为RuntimeException抛出
     *
     * @param throwable 待处理的错误
     * @param tClass    指定的错误类型
     */
    public <T extends Throwable> void handleException(Throwable throwable,Class<T> tClass) throws T
    {
        handleException(throwable,t->
        {
            if (t instanceof RuntimeException)
            {
                throw (RuntimeException) t;
            }
            else
            {
                throw new RuntimeException(t);
            }
        },tClass);
    }

    /**
     * 通过错误处理器处理错误,若处理后仍有指定类型的错误则直接抛出,其他错误则使用指定的错误处理器处理
     *
     * @param throwable 待处理的错误
     * @param c         指定的错误处理器
     * @param x         指定的错误类型
     * @param <T>       指定的错误类型
     * @throws T 指定的错误类型
     */
    public <T extends Throwable> void handleException(Throwable throwable,Consumer<Throwable> c,Class<T> x) throws T
    {
        try
        {
            throwException(throwable);
        }
        catch (Throwable t)
        {
            if (x.isAssignableFrom(t.getClass()))
            {
                throw x.cast(t);
            }
            c.accept(t);
        }
    }

    /**
     * 运行指定的代码,若有错误则使用错误处理器处理,若处理后仍有错误则直接抛出,其他错误则使用指定的错误处理器处理
     *
     * @param run 指定的代码
     * @param c   指定的错误处理器
     */
    public void handle(RunWithThrow run,Consumer<Throwable> c)
    {
        try
        {
            run.run();
        }
        catch (Throwable throwable)
        {
            handleException(throwable,c);
        }
    }

    /**
     * 运行指定的代码,若有错误则使用错误处理器处理,若处理后仍有指定类型的错误则直接抛出,其他错误则使用指定的错误处理器处理
     *
     * @param run 指定的代码
     * @param c   指定的错误处理器
     * @param x   指定的错误类型
     * @param <T> 指定的错误类型
     * @throws T 指定的错误类型
     */
    public <T extends Throwable> void handle(RunWithThrow run,Consumer<Throwable> c,Class<T> x) throws T
    {
        try
        {
            run.run();
        }
        catch (Throwable throwable)
        {
            handleException(throwable,c,x);
        }
    }
}