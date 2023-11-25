package me.nullaqua.api.io;

import me.nullaqua.api.util.function.SupplierWithThrow;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Objects;

public class KeyObjectInputStream extends ObjectInputStream
{
    private final KeyInputStream stream;
    private IOStreamKey defaultKey=IOStreamKey.EmptyKey;

    private KeyObjectInputStream(KeyInputStream stream) throws IOException
    {
        super(stream);
        this.stream=stream;
    }

    public static KeyObjectInputStream create(InputStream in) throws IOException
    {
        return new KeyObjectInputStream(new KeyInputStream(in));
    }

    public static KeyObjectInputStream create(InputStream in,IOStreamKey baseKey) throws IOException
    {
        return new KeyObjectInputStream(new KeyInputStream(in,baseKey));
    }

    public IOStreamKey defaultKey()
    {
        return defaultKey;
    }

    public KeyObjectInputStream defaultKey(IOStreamKey defaultKey)
    {
        if (!Objects.isNull(defaultKey))
        {
            this.defaultKey=defaultKey;
        }
        else
        {
            this.defaultKey=IOStreamKey.EmptyKey;
        }
        stream.key(defaultKey);
        return this;
    }

    public Object readObject(IOStreamKey key) throws IOException, ClassNotFoundException
    {
        if (key==null)
        {
            key=defaultKey;
        }
        stream.key(key);
        try
        {
            return super.readObject();
        }
        finally
        {
            stream.key(defaultKey);
        }
    }

    public boolean readBoolean(IOStreamKey key) throws IOException
    {
        return runWithKey(super::readBoolean,key);
    }

    private <T> T runWithKey(SupplierWithThrow<T> run,IOStreamKey key) throws IOException
    {
        if (key==null)
        {
            key=defaultKey;
        }
        stream.key(key);
        try
        {
            return run.get(IOException.class);
        }
        finally
        {
            stream.key(defaultKey);
        }
    }

    public byte readByte(IOStreamKey key) throws IOException
    {
        return runWithKey(super::readByte,key);
    }

    public int readUnsignedByte(IOStreamKey key) throws IOException
    {
        return runWithKey(super::readUnsignedByte,key);
    }

    public char readChar(IOStreamKey key) throws IOException
    {
        return runWithKey(super::readChar,key);
    }

    public short readShort(IOStreamKey key) throws IOException
    {
        return runWithKey(super::readShort,key);
    }

    public int readUnsignedShort(IOStreamKey key) throws IOException
    {
        return runWithKey(super::readUnsignedShort,key);
    }

    public int readInt(IOStreamKey key) throws IOException
    {
        return runWithKey(super::readInt,key);
    }

    public long readLong(IOStreamKey key) throws IOException
    {
        return runWithKey(super::readLong,key);
    }

    public float readFloat(IOStreamKey key) throws IOException
    {
        return runWithKey(super::readFloat,key);
    }

    public double readDouble(IOStreamKey key) throws IOException
    {
        return runWithKey(super::readDouble,key);
    }

    public void readFully(byte[] buf,IOStreamKey key) throws IOException
    {
        readFully(buf,0,buf.length,key);
    }

    public void readFully(byte[] buf,int off,int len,IOStreamKey key) throws IOException
    {
        runWithKey(()->
                   {
                       super.readFully(buf,off,len);
                       return 0;
                   },key);
    }

    public String readUTF(IOStreamKey key) throws IOException
    {
        return runWithKey(super::readUTF,key);
    }
}
