package me.lanzhi.api.io;

import me.lanzhi.api.BluestarUtils;

import java.io.Serializable;

public final class IOAccessor implements Serializable
{
    private static final IOStreamKey.HexKey hexKey=new IOStreamKey.HexKey();

    private IOAccessor()
    {
    }

    public static IOStreamKey.XorKey randomXorKey()
    {
        return xorKey(BluestarUtils.randomByte());
    }

    public static IOStreamKey.XorKey xorKey(byte b)
    {
        return new IOStreamKey.XorKey(b);
    }

    public static IOStreamKey.HexKey hexKey()
    {
        return hexKey;
    }

    public static IOStreamKey plusKeys(IOStreamKey... key)
    {
        return IOStreamKey.plus(key);
    }
}