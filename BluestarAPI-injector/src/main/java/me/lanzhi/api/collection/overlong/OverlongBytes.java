package me.lanzhi.api.collection.overlong;

import me.lanzhi.api.collection.ByteVector;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

public final class OverlongBytes implements OverlongVector<Byte>
{
    private final Vector<ByteVector> vector=new Vector<>();

    @Override
    public long length()
    {
        if (vector.isEmpty())
        {
            return 0;
        }
        return vector.lastElement().size()+(long) (vector.size()-1)*Integer.MAX_VALUE;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c)
    {
        for (Object o: c)
        {
            if (!contains(o))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Byte> c)
    {
        for (Byte b: c)
        {
            add(b);
        }
        return true;
    }

    @Override
    public boolean add(Byte aByte)
    {
        put(aByte);
        return true;
    }

    public void put(byte b)
    {
        if (vector.isEmpty()||vector.lastElement().size()==Integer.MAX_VALUE)
        {
            var v=new ByteVector();
            v.setMaxSize(Integer.MAX_VALUE);
            vector.add(v);
        }
        vector.lastElement().put(b);
    }

    @Override
    public void clear()
    {
        vector.clear();
    }

    @Override
    public Byte get(long index)
    {
        int i=(int) (index >>> 31);
        int j=(int) (index&0x7FFFFFFF);
        return vector.get(i).get(j);
    }

    @Override
    public Byte set(long index,Byte element)
    {
        int i=(int) (index >>> 31);
        int j=(int) (index&0x7FFFFFFF);
        return vector.get(i).set(j,element);
    }

    public long getMaxSize()
    {
        if (vector.isEmpty())
        {
            return 0;
        }
        return vector.lastElement().getMaxSize()+(long) (vector.size()-1)*Integer.MAX_VALUE;
    }

    @Override
    public OverlongBytes clone()
    {
        var clone=new OverlongBytes();
        clone.addAll(this);
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
            final Iterator<Byte> iterator=iterator();

            @Override
            public int read()
            {
                return iterator.hasNext()?iterator.next():-1;
            }
        };
    }

    @NotNull
    @Override
    public Iterator<Byte> iterator()
    {
        return new Iterator<>()
        {
            private int i=0;
            private int j=0;

            @Override
            public boolean hasNext()
            {
                return i<vector.size()&&j<vector.get(i).size();
            }

            @Override
            public Byte next()
            {
                Byte b=vector.get(i).get(j);
                if (++j==Integer.MAX_VALUE)
                {
                    i++;
                    j=0;
                }
                return b;
            }
        };
    }
}