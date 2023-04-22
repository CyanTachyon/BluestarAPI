package me.lanzhi.api.collection.overlong;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

/**
 * 超长数组接口,用于存储超过{@link Integer#MAX_VALUE}个元素的类
 *
 * @param <E> 元素类型
 */
public interface OverlongVector<E> extends Iterable<E>, Cloneable, java.io.Serializable, Collection<E>, RandomAccess
{
    @Override
    default boolean retainAll(@NotNull Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean removeAll(@NotNull Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean removeIf(Predicate<? super E> filter)
    {
        throw new UnsupportedOperationException();
    }

    default boolean isEmpty()
    {
        return length()==0;
    }

    /**
     * 返回数组的大小
     *
     * @return 数组大小
     */
    long length();

    default boolean contains(Object o)
    {
        return indexOf(o)>=0;
    }

    default int indexOf(Object o)
    {
        return (int) Long.min(index(o),Integer.MAX_VALUE);
    }

    default long index(Object o)
    {
        for (long i=0;i<length();i++)
        {
            if (get(i).equals(o))
            {
                return i;
            }
        }
        return -1;
    }

    E get(long index);

    default boolean remove(Object o)
    {
        throw new UnsupportedOperationException();
    }

    void clear();

    default E set(int index,E element)
    {
        return set((long) index,element);
    }

    E set(long index,E element);

    default void add(int index,E element)
    {
        throw new UnsupportedOperationException();
    }

    default E remove(int index)
    {
        throw new UnsupportedOperationException();
    }

    default int lastIndexOf(Object o)
    {
        return (int) Long.min(lastIndex(o),Integer.MAX_VALUE);
    }

    default long lastIndex(Object o)
    {
        for (long i=length()-1;i>=0;i--)
        {
            if (get(i).equals(o))
            {
                return i;
            }
        }
        return -1;
    }

    default List<E> subList(int fromIndex,int toIndex)
    {
        List<E> list=new ArrayList<>();
        for (int i=fromIndex;i<toIndex;i++)
        {
            list.add(get(i));
        }
        return list;
    }

    default E get(int index)
    {
        return get((long) index);
    }

    default <T> T[] toArray(T[] a)
    {
        if (a.length<size())
        {
            return (T[]) Arrays.copyOf(toArray(),size(),a.getClass());
        }
        System.arraycopy(toArray(),0,a,0,size());
        if (a.length>size())
        {
            a[size()]=null;
        }
        return a;
    }

    /**
     * 返回数组的大小,当数组大小超过{@link Integer#MAX_VALUE}时,返回{@link Integer#MAX_VALUE}
     *
     * @return 数组大小
     */
    @Deprecated
    default int size()
    {
        return (int) Long.min(length(),Integer.MAX_VALUE);
    }

    default Object[] toArray()
    {
        Object[] array=new Object[size()];
        for (int i=0;i<size();i++)
        {
            array[i]=get(i);
        }
        return array;
    }

    default boolean addAll(OverlongVector<? extends E> c)
    {
        for (E e: c)
        {
            add(e);
        }
        return true;
    }

    boolean add(E e);

    boolean equals(Object o);

    int hashCode();
}