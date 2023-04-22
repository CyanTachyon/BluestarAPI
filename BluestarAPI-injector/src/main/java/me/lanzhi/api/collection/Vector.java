package me.lanzhi.api.collection;

import java.util.*;

public class Vector<E> implements Collection<E>, RandomAccess, Cloneable, java.io.Serializable
{
    private E[] data;
    private int top=-1;

    public Vector()
    {
        this(1);
    }

    public Vector(int size)
    {
        data=(E[]) new Object[size];
    }

    @Override
    public boolean isEmpty()
    {
        return size()==0;
    }

    public int size()
    {
        return top+1;
    }

    @Override
    public Iterator<E> iterator()
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
            public E next()
            {
                return data[pos++];
            }
        };
    }

    @Override
    public <T> T[] toArray(T[] a)
    {
        E[] objects=toArray();
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
    public E[] toArray()
    {
        return Arrays.copyOf(data,size());
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
    public boolean contains(Object o)
    {
        if (o!=null)
        {
            for (E b: this)
            {
                if (Objects.equals(b,o))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends E> c)
    {
        for (E b: c)
        {
            this.add(b);
        }
        return true;
    }

    @Override
    public boolean add(E e)
    {
        Objects.requireNonNull(e);
        put(e);
        return true;
    }

    public void put(E b)
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
        E[] newData=(E[]) new Object[data.length<<1];
        System.arraycopy(data,0,newData,0,size());
        data=newData;
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear()
    {
        this.top=-1;
    }

    public E set(int index,E b)
    {
        E res=get(index);
        data[index]=b;
        return res;
    }

    public E get(int pos)
    {
        if (pos<0||pos>=size())
        {
            throw new IndexOutOfBoundsException("Index: "+pos+" is out of bounds: 0~"+(size()-1));
        }
        return data[pos];
    }

    public E top()
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
        E[] es=(E[]) new Object[size];
        System.arraycopy(data,0,es,0,size());
        this.data=es;
    }

    @Override
    public Vector<E> clone()
    {
        var clone=new Vector<E>();
        clone.put(this.toArray());
        return clone;
    }

    public void put(E[] es)
    {
        for (E b: es)
        {
            put(b);
        }
    }
}