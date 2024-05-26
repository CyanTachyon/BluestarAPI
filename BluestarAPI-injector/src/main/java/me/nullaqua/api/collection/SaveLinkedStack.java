package me.nullaqua.api.collection;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@SuppressWarnings("unused")
public class SaveLinkedStack<E> extends LinkedStack<E>
{
    private final Lock readLock;
    private final Lock writeLock;

    public SaveLinkedStack()
    {
        super();
        ReadWriteLock lock=new ReentrantReadWriteLock();
        readLock=lock.readLock();
        writeLock=lock.writeLock();
    }

    @Override
    public int size()
    {
        try
        {
            readLock.lock();
            return super.size();
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public boolean isEmpty()
    {
        try
        {
            readLock.lock();
            return super.isEmpty();
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public @NotNull Iterator<E> iterator()
    {
        try
        {
            readLock.lock();
            return super.iterator();
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public @NotNull Object[] toArray()
    {
        try
        {
            readLock.lock();
            return super.toArray();
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public void push(E e)
    {
        try
        {
            writeLock.lock();
            super.push(e);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public void pushAll(@NotNull Collection<? extends E> c)
    {
        try
        {
            writeLock.lock();
            super.pushAll(c);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public void clear()
    {
        try
        {
            writeLock.lock();
            super.clear();
        }
        finally
        {
            writeLock.unlock();
        }
    }
}
