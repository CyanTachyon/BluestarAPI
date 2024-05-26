package me.nullaqua.api.collection;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

@SuppressWarnings({"unused", "unchecked"})
public class ByteVector implements Collection<Byte>, RandomAccess, Cloneable, java.io.Serializable
{
    private byte[] data;
    private int top=-1;

    public ByteVector()
    {
        this(1);
    }

    public ByteVector(int size)
    {
        data=new byte[size];
    }

    public void put(byte[] bytes)
    {
        for (byte b: bytes)
        {
            put(b);
        }
    }

    public void put(byte b)
    {
        expand();
        data[++top]=b;
    }

    private void expand()
    {
        if (size()<data.length)
        {
            return;
        }
        long length=(long) data.length<<1;
        setMaxSize(length>Integer.MAX_VALUE?Integer.MAX_VALUE:(int) length);
    }

    public int size()
    {
        return top+1;
    }

    @Override
    public boolean isEmpty()
    {
        return size()==0;
    }

    @Override
    public boolean contains(Object o)
    {
        if (o instanceof Byte)
        {
            for (byte b: this)
            {
                if ((byte) o==b)
                {
                    return true;
                }
            }
        }
        return false;
    }

    @NotNull
    @Override
    public Iterator<Byte> iterator()
    {
        return new Iterator<>()
        {
            private int pos=0;

            @Override
            public boolean hasNext()
            {
                return pos<size();
            }

            @Override
            public Byte next()
            {
                return data[pos++];
            }
        };
    }

    @NotNull
    @Override
    public Byte @NotNull [] toArray()
    {
        byte[] bytes=toByteArray();
        Byte[] bts=new Byte[bytes.length];
        for (int i=1;i<bts.length;i++)
        {
            bts[i]=bytes[i];
        }
        return bts;
    }

    @Override
    public <T> T @NotNull [] toArray(T[] a)
    {
        Byte[] objects=toArray();
        if (a.length<size())
        {
            return (T[]) Arrays.copyOf(objects,size(),a.getClass());
        }
        System.arraycopy(objects,0,a,0,size());
        if (a.length>size())
        {
            a[size()]=null;
        }
        return a;
    }

    @Override
    public boolean add(Byte aByte)
    {
        Objects.requireNonNull(aByte);
        put(aByte);
        return true;
    }

    @Override
    public boolean remove(Object o)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        for (Object o: c)
        {
            if (!this.contains(o))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends Byte> c)
    {
        for (Byte b: c)
        {
            this.add(b);
        }
        return true;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear()
    {
        this.top=-1;
    }

    public byte[] toByteArray()
    {
        byte[] bytes=new byte[size()];
        System.arraycopy(data,0,bytes,0,size());
        return bytes;
    }

    public byte set(int index,byte b)
    {
        byte res=get(index);
        data[index]=b;
        return res;
    }

    public byte top()
    {
        if (top>=0)
        {
            return get(top);
        }
        throw new NoSuchElementException();
    }

    public boolean remove()
    {
        if (top>=0)
        {
            --top;
            return true;
        }
        return false;
    }

    public byte get(int pos)
    {
        if (pos<0||pos>=size())
        {
            throw new IndexOutOfBoundsException("Index: "+pos+" is out of bounds: 0~"+(size()-1));
        }
        return data[pos];
    }

    public int getMaxSize()
    {
        return data.length;
    }

    public void setMaxSize(int size)
    {
        if (size<size())
        {
            throw new IllegalArgumentException("Size: "+size+" is less than current size: "+size());
        }
        byte[] bytes=new byte[size];
        System.arraycopy(data,0,bytes,0,size());
        this.data=bytes;
    }

    @Override
    public ByteVector clone()
    {
        var clone=new ByteVector();
        clone.put(toByteArray());
        return clone;
    }

    public OutputStream toOutputStream()
    {
        return new OutputStream()
        {
            @Override
            public void write(int b)
            {
                put((byte) b);
            }
        };
    }

    public InputStream toInputStream()
    {
        return new InputStream()
        {
            private int pos=0;

            @Override
            public int read()
            {
                if (pos<size())
                {
                    return get(pos++)&0xFF;
                }
                return -1;
            }
        };
    }
}
