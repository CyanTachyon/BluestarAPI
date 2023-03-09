package me.lanzhi.api.util.function;

public interface RunWithThrow
{
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

    void run() throws Throwable;

    public static <T extends Throwable> T check(Throwable throwable,Class<T> t)
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

    default Runnable toRunnable()
    {
        return toRun().toRunnable();
    }

    default Run toRun()
    {
        return ()->run(RuntimeException.class);
    }
}
