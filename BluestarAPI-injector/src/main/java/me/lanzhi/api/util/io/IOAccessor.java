package me.lanzhi.api.util.io;

import me.lanzhi.api.Bluestar;

import java.io.Serializable;

public final class IOAccessor implements Serializable
{
    private IOAccessor()
    {
    }

    public static IOStreamKey.XorKey randomXorKey()
    {
        return new IOStreamKey.XorKey((byte) Bluestar.randomInt());
    }
}
