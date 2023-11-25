package me.nullaqua.api.io;

import me.nullaqua.api.util.function.RunWithThrow;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Objects;

public class KeyObjectOutputStream extends ObjectOutputStream
{
    private final KeyOutputStream stream;
    private IOStreamKey defaultKey=IOStreamKey.EmptyKey;

    private KeyObjectOutputStream(KeyOutputStream stream) throws IOException
    {
        super(stream);
        this.stream=stream;
    }

    public static KeyObjectOutputStream create(OutputStream stream) throws IOException
    {
        return new KeyObjectOutputStream(new KeyOutputStream(stream));
    }

    public static KeyObjectOutputStream create(OutputStream stream,IOStreamKey baseKey) throws IOException
    {
        return new KeyObjectOutputStream(new KeyOutputStream(stream,baseKey));
    }

    public IOStreamKey defaultKey()
    {
        return defaultKey;
    }

    public KeyObjectOutputStream defaultKey(IOStreamKey defaultKey)
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

    public void writeObject(Object o,IOStreamKey key) throws IOException
    {
        runWithKey(()->super.writeObject(o),key);
    }

    private void runWithKey(RunWithThrow function,IOStreamKey key) throws IOException
    {
        if (key==null)
        {
            key=defaultKey;
        }
        stream.key(key);
        try
        {
            function.run(IOException.class);
        }
        finally
        {
            stream.key(defaultKey);
        }
    }

    public void writeBoolean(boolean val,IOStreamKey key) throws IOException
    {
        runWithKey(()->super.writeBoolean(val),key);
    }

    public void writeByte(int val,IOStreamKey key) throws IOException
    {
        runWithKey(()->super.writeByte(val),key);
    }

    public void writeShort(int val,IOStreamKey key) throws IOException
    {
        runWithKey(()->super.writeShort(val),key);
    }

    public void writeChar(int val,IOStreamKey key) throws IOException
    {
        runWithKey(()->super.writeChar(val),key);
    }

    public void writeInt(int val,IOStreamKey key) throws IOException
    {
        runWithKey(()->super.writeInt(val),key);
    }

    public void writeLong(long val,IOStreamKey key) throws IOException
    {
        runWithKey(()->super.writeLong(val),key);
    }

    public void writeFloat(float val,IOStreamKey key) throws IOException
    {
        runWithKey(()->super.writeFloat(val),key);
    }

    public void writeDouble(double val,IOStreamKey key) throws IOException
    {
        runWithKey(()->super.writeDouble(val),key);
    }

    public void writeBytes(String str,IOStreamKey key) throws IOException
    {
        runWithKey(()->super.writeBytes(str),key);
    }

    public void writeChars(String str,IOStreamKey key) throws IOException
    {
        runWithKey(()->super.writeChars(str),key);
    }

    public void writeUTF(String str,IOStreamKey key) throws IOException
    {
        runWithKey(()->super.writeUTF(str),key);
    }
}