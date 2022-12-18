package me.lanzhi.api.util.io;

import java.io.IOException;
import java.io.OutputStream;

public class KeyOutputStream extends OutputStream
{
    private final OutputStream stream;
    private IOStreamKey key=IOStreamKey.EmptyKey;

    public KeyOutputStream(OutputStream stream)
    {
        this.stream=stream;
    }

    public KeyOutputStream key(IOStreamKey key)
    {
        this.key=key;
        return this;
    }

    @Override
    public final void write(int b) throws IOException
    {
        stream.write(key.encrypt((byte) b));
    }
}
