package me.lanzhi.api.util.function;

public interface Run
{
    /**
     * 将Run对象转换为Runnable对象。
     *
     * @return Runnable对象
     */
    default Runnable toRunnable()
    {
        return this::run;
    }

    /**
     * 执行操作。
     */
    void run();

    /**
     * 将Run对象转换为RunWithThrow对象。
     *
     * @return RunWithThrow对象
     */
    default RunWithThrow toRunWithThrow()
    {
        return this::run;
    }
}