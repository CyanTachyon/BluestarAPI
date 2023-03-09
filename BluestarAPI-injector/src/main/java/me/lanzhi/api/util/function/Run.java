package me.lanzhi.api.util.function;

public interface Run
{
    default Runnable toRunnable()
    {
        return this::run;
    }

    void run();

    default RunWithThrow toRunWithThrow()
    {
        return this::run;
    }
}
