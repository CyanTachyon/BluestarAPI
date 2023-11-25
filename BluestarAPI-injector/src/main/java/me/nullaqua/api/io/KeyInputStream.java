package me.nullaqua.api.io;

import java.io.IOException;
import java.io.InputStream;

public class KeyInputStream extends InputStream
{
    private final InputStream stream;
    private IOStreamKey key=IOStreamKey.EmptyKey;
    private final IOStreamKey baseKey;

    public KeyInputStream(InputStream stream)
    {
        this(stream,IOStreamKey.EmptyKey);
    }

    public KeyInputStream(InputStream stream,IOStreamKey baseKey)
    {
        this.stream=stream;
        this.baseKey=baseKey!=null?baseKey:IOStreamKey.EmptyKey;
    }

    public KeyInputStream key(IOStreamKey key)
    {
        this.key=key;
        return this;
    }

    public IOStreamKey key()
    {
        return key;
    }

    @Override
    public final int read() throws IOException
    {
        var key=getKey();
        byte[] bytes=new byte[key.encryptNum()];
        int read=stream.read(bytes);
        if (read==-1)
        {
            return -1;
        }
        else if (read!=key.encryptNum())
        {
            throw new IOException("Read "+read+" bytes, but "+key.encryptNum()+" bytes expected.");
        }
        return key.decrypt(bytes);
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
