package me.lanzhi.api.util.io;

import java.io.IOException;
import java.io.OutputStream;

public class KeyOutputStream extends OutputStream
{
    private final OutputStream stream;
    private IOStreamKey key=IOStreamKey.EmptyKey;
    private final IOStreamKey baseKey;

    public KeyOutputStream(OutputStream stream)
    {
        this(stream,IOStreamKey.EmptyKey);
    }

    public KeyOutputStream(OutputStream stream,IOStreamKey baseKey)
    {
        this.stream=stream;
        this.baseKey=baseKey!=null?baseKey:IOStreamKey.EmptyKey;
    }

    public KeyOutputStream key(IOStreamKey key)
    {
        this.key=key;
        return this;
    }

    public IOStreamKey key()
    {
        return key;
    }

    @Override
    public final void write(int b) throws IOException
    {
        stream.write(getKey().encrypt((byte) b));
    }

    private final IOStreamKey getKey()
    {
        if (key==null||key==IOStreamKey.EmptyKey)
        {
            return baseKey;
        }
        if (baseKey==null||baseKey==IOStreamKey.EmptyKey)
        {
            return key;
        }
        return IOAccessor.plusKeys(key,baseKey);
    }
}
