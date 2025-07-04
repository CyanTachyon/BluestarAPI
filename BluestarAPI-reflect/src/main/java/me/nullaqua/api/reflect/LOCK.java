package me.nullaqua.api.reflect;

/**
 * LOCK类,通过需要不空的LOCK实例,并且其没有任何可用构造函数,从而使对应方法无法被调用
 */
public final class LOCK extends BluestarAPI
{
    @SuppressWarnings("unused")
    private final Void lock;
    private LOCK()
    {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    @SuppressWarnings({"UnusedReturnValue", "ConstantValue"})
    public LOCK check()
    {
        if (lock == null) throw new RuntimeException("Check failed");
        return this;
    }
}