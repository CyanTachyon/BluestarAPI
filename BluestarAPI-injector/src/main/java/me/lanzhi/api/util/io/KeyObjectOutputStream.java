package me.lanzhi.api.util.io;

import me.lanzhi.api.util.function.RunAndThrow;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class KeyObjectOutputStream extends ObjectOutputStream
{
    private final KeyOutputStream stream;

    private KeyObjectOutputStream(KeyOutputStream stream) throws IOException
    {
        super(stream);
        this.stream=stream;
    }

    public static KeyObjectOutputStream create(OutputStream stream) throws IOException
    {
        return new KeyObjectOutputStream(new KeyOutputStream(stream));
    }

    public void writeObject(Object o,IOStreamKey key) throws IOException
    {
        runWithKey(()->super.writeObject(o),key);
    }

    private void runWithKey(RunAndThrow function,IOStreamKey key) throws IOException
    {
        if (key==null)
        {
            key=IOStreamKey.EmptyKey;
        }
        stream.key(key);
        try
        {
            function.run(IOException.class);
        }
        finally
        {
            stream.key(IOStreamKey.EmptyKey);
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