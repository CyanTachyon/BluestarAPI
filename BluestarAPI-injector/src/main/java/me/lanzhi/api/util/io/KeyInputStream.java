package me.lanzhi.api.util.io;

import java.io.IOException;
import java.io.InputStream;

public class KeyInputStream extends InputStream
{
    private final InputStream stream;
    private IOStreamKey key=IOStreamKey.EmptyKey;

    public KeyInputStream(InputStream stream)
    {
        this.stream=stream;
    }

    public KeyInputStream key(IOStreamKey key)
    {
        this.key=key;
        return this;
    }

    @Override
    public final int read() throws IOException
    {
        byte[] bytes=new byte[key.encryptNum()];
        if (stream.read(bytes)!=key.encryptNum())
        {
            throw new IOException("读取字节并进行解密失败");
        }
        return key.decrypt(bytes);
    }
}
