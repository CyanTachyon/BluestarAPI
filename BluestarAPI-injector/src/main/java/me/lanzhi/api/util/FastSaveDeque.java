package me.lanzhi.api.util;

import me.lanzhi.api.reflect.MethodAccessor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FastSaveDeque<E> extends FastDeque<E>
{
    private final Lock readLock;
    private final Lock writeLock;

    public FastSaveDeque()
    {
        super();
        ReadWriteLock lock=new ReentrantReadWriteLock();
        readLock=lock.readLock();
        writeLock=lock.writeLock();
    }

    @Override
    public void addFirst(E e)
    {
        try
        {
            writeLock.lock();
            super.addFirst(e);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public void addLast(E e)
    {
        try
        {
            writeLock.lock();
            super.addLast(e);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public E removeFirst() throws NoSuchElementException
    {
        try
        {
            writeLock.lock();
            return super.removeFirst();
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public E removeLast() throws NoSuchElementException
    {
        try
        {
            writeLock.lock();
            return super.removeLast();
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public E getFirst() throws NoSuchElementException
    {
        try
        {
            readLock.lock();
            return super.getFirst();
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public E getLast() throws NoSuchElementException
    {
        try
        {
            readLock.lock();
            return super.getLast();
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public boolean removeFirstOccurrence(Object o)
    {
        try
        {
            writeLock.lock();
            return super.removeFirstOccurrence(o);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public boolean removeLastOccurrence(Object o)
    {
        try
        {
            writeLock.lock();
            return super.removeLastOccurrence(o);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c)
    {
        try
        {
            writeLock.lock();
            return super.retainAll(c);
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

    @Override
    public boolean removeAll(Object o)
    {
        try
        {
            writeLock.lock();
            return super.removeAll(o);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c)
    {
        try
        {
            readLock.lock();
            return super.containsAll(c);
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public boolean contains(Object o)
    {
        try
        {
            readLock.lock();
            return super.contains(o);
        }
        finally
        {
            readLock.unlock();
        }
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
    public SaveQueIterator iterator()
    {
        return new SaveQueIterator();
    }

    @NotNull
    @Override
    public Object[] toArray()
    {
        try
        {
            writeLock.lock();
            return super.toArray();
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    @NotNull
    public SaveDesQueIterator descendingIterator()
    {
        return new SaveDesQueIterator();
    }

    @Override
    public E set(FastDeque<E>.QueIterator iterator,E e)
    {
        try
        {
            writeLock.lock();
            return super.set(iterator,e);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public void insert(FastDeque<E>.QueIterator iterator,E e)
    {
        try
        {
            writeLock.lock();
            super.insert(iterator,e);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public E removeItem(FastDeque<E>.QueIterator iterator)
    {
        try
        {
            writeLock.lock();
            return super.removeItem(iterator);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    @NotNull
    public Iterable<E> descending()
    {
        return this::descendingIterator;
    }

    @Override
    public void insert(FastDeque<E>.DesQueIterator iterator,E e)
    {
        try
        {
            writeLock.lock();
            super.insert(iterator,e);
        }
        finally
        {
            writeLock.unlock();
        }
        ;
    }

    @Override
    public E set(FastDeque<E>.DesQueIterator iterator,E e)
    {
        try
        {
            writeLock.lock();
            return super.set(iterator,e);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public E removeItem(FastDeque<E>.DesQueIterator iterator)
    {
        try
        {
            writeLock.lock();
            return super.removeItem(iterator);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public FastSaveDeque<E> clone()
    {
        FastSaveDeque<E> deque=new FastSaveDeque<>();
        for (E e: this)
        {
            deque.addLast(e);
        }
        return deque;
    }

    @Override
    public FastSaveDeque<E> deepClone() throws Throwable
    {
        FastSaveDeque<E> deque=new FastSaveDeque<>();
        for (E e: this)
        {
            MethodAccessor methodAccessor=MethodAccessor.getDeclaredMethod(e.getClass(),"clone");
            methodAccessor.invoke(e);
            deque.addLast(e);
        }
        return deque;
    }

    public class SaveQueIterator extends QueIterator
    {
        @Override
        public E next()
        {
            try
            {
                readLock.lock();
                return super.next();
            }
            finally
            {
                readLock.unlock();
            }
        }

        @Override
        public E previous()
        {
            try
            {
                readLock.lock();
                return super.previous();
            }
            finally
            {
                readLock.unlock();
            }
        }
    }

    public class SaveDesQueIterator extends DesQueIterator
    {
        @Override
        public E next()
        {
            try
            {
                readLock.lock();
                return super.next();
            }
            finally
            {
                readLock.unlock();
            }
        }

        @Override
        public E previous()
        {
            try
            {
                readLock.lock();
                return super.previous();
            }
            finally
            {
                readLock.unlock();
            }
        }
    }
}
