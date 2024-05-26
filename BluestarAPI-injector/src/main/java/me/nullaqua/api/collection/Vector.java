package me.nullaqua.api.collection;

import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("unused")
public class Vector<E> implements Collection<E>, RandomAccess, Cloneable, java.io.Serializable
{
    private E[] data;
    private int top = -1;

    public Vector()
    {
        this(1);
    }

    @SuppressWarnings("unchecked")
    public Vector(int size)
    {
        data = (E[]) new Object[size];
    }

    @Override
    public boolean isEmpty()
    {
        return size() == 0;
    }

    public int size()
    {
        return top+1;
    }

    @NotNull
    @Override
    public Iterator<E> iterator()
    {
        return new Iterator<>()
        {
            private int pos = 0;

            @Override
            public boolean hasNext()
            {
                return pos < size();
            }

            @Override
            public E next()
            {
                return data[pos++];
            }
        };
    }

    @NotNull
    @Override
    @SuppressWarnings({"unchecked", "SuspiciousSystemArraycopy"})
    public <T> T @NotNull [] toArray(T[] a)
    {
        E[] objects = toArray();
        if (a.length < size())
        {
            return (T[]) Arrays.copyOf(objects, size(), a.getClass());
        }
        System.arraycopy(objects, 0, a, 0, size());
        if (a.length > size())
        {
            a[size()] = null;
        }
        return a;
    }

    @NotNull
    @Override
    public E @NotNull [] toArray()
    {
        return Arrays.copyOf(data, size());
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
        if (o != null)
        {
            for (E b: this)
            {
                if (Objects.equals(b, o))
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
        data[++top] = b;
    }

    private void expand()
    {
        if (size() < data.length)
        {
            return;
        }
        expand(data.length);
    }

    @SuppressWarnings("unchecked")
    private void expand(int min)
    {
        if (min < data.length)
        {
            return;
        }
        int x = 1;
        while (min >= (data.length<<x)) ++x;
        E[] newData = (E[]) new Object[data.length<<x];
        System.arraycopy(data, 0, newData, 0, data.length);
        data = newData;
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
        this.top = -1;
    }

    public E set(int index, E b)
    {
        //如果index小于0,抛出异常,如果大于等于size,扩容
        if (index < 0)
        {
            throw new IndexOutOfBoundsException("Index: "+index+" is out of bounds: 0~"+(size()-1));
        }
        expand(index);
        E old = data[index];
        data[index] = b;
        top = Math.max(top, index);
        return old;
    }

    public E get(int pos)
    {
        if (pos < 0 || pos >= size())
        {
            return null;
        }
        return data[pos];
    }

    public E top()
    {
        if (top >= 0)
        {
            return get(top);
        }
        throw new NoSuchElementException();
    }

    public boolean remove()
    {
        if (top >= 0)
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

    @SuppressWarnings("unchecked")
    public void setMaxSize(int size)
    {
        if (size < size())
        {
            throw new IllegalArgumentException("Size: "+size+" is less than current size: "+size());
        }
        E[] es = (E[]) new Object[size];
        System.arraycopy(data, 0, es, 0, size());
        this.data = es;
    }

    @Override
    public Vector<E> clone()
    {
        var clone = new Vector<E>();
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

    @Override
    public int hashCode()
    {
        int res = 0;
        for (E b: this)
        {
            res += b.hashCode();
        }
        return res;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj)
    {
        if (obj instanceof Vector)
        {
            Vector<E> v = (Vector<E>) obj;
            if (v.size() != size())
            {
                return false;
            }
            for (int i = 0; i < size(); ++i)
            {
                if (!Objects.equals(v.get(i), get(i)))
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < size(); ++i)
        {
            sb.append(get(i));
            if (i != size()-1)
            {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}