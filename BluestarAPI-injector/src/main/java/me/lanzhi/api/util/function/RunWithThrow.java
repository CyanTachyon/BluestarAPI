package me.lanzhi.api.util.function;

/**
 * 一个可能会抛出异常的操作。
 */
public interface RunWithThrow
{
    /**
     * 检查异常类型是否匹配。
     *
     * @param throwable 异常对象
     * @param t         异常类型
     * @param <T>       异常类型
     * @return 匹配的异常对象
     */
    static <T extends Throwable> T check(Throwable throwable,Class<T> t)
    {
        if (t.isAssignableFrom(throwable.getClass()))
        {
            return t.cast(throwable);
        }
        if (throwable instanceof RuntimeException)
        {
            throw (RuntimeException) throwable;
        }
        throw new RuntimeException(throwable);
    }

    /**
     * 执行操作。并抛出指定类型的受检异常。其他异常将被包装为RuntimeException抛出。
     *
     * @param t   指定类型的受检异常
     * @param <T> 指定类型的受检异常
     * @throws T 指定类型的受检异常
     */
    default <T extends Throwable> void run(Class<T> t) throws T
    {
        try
        {
            this.run();
        }
        catch (Throwable throwable)
        {
            throw check(throwable,t);
        }
    }

    /**
     * 执行操作，可能会抛出异常。
     *
     * @throws Throwable 异常
     */
    void run() throws Throwable;

    /**
     * 将RunWithThrow对象转换为Runnable对象。
     *
     * @return Runnable对象
     */
    default Runnable toRunnable()
    {
        return toRun().toRunnable();
    }

    /**
     * 将RunWithThrow对象转换为Run对象。
     *
     * @return Run对象
     */
    default Run toRun()
    {
        return () -> run(RuntimeException.class);
    }
}