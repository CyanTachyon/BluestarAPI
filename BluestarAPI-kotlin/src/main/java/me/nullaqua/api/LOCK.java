package me.nullaqua.api;

import me.nullaqua.api.reflect.BluestarAPI;

/**
 * LOCK类,通过需要不空的LOCK实例,并且其没有任何可用构造函数,从而使对应方法无法被调用
 */
@SuppressWarnings("ALL")
public final class LOCK extends BluestarAPI
{
    private final Void lock;
    private LOCK()
    {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    public LOCK check()
    {
        if (lock == null) throw new RuntimeException("Check failed");
        return this;
    }
}