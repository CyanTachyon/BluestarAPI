package me.lanzhi.api.collection;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FastLinkedList<E> extends LinkedDeque<E> implements List<E>
{
    public FastLinkedList()
    {
        super();
    }

    public FastLinkedList(Collection<E> collection)
    {
        super(collection);
    }

    @Override
    public boolean addAll(int index,@NotNull Collection<? extends E> c) throws IndexOutOfBoundsException
    {
        if (index>size()||index<0)
        {
            throw new IndexOutOfBoundsException(
                    "Can not invoke LinkedList.addAll(),because \"index\" is out of range(0~size)");
        }
        if (index==size())
        {
            return addAll(c);
        }
        QueIterator iterator=super.iterator();
        while (index-->0)
        {
            iterator.next();
        }
        for (E e: c)
        {
            iterator.add(e);
        }
        return true;
    }

    @Override
    public E get(int index) throws IndexOutOfBoundsException
    {
        if (index>=size()||index<0)
        {
            throw new IndexOutOfBoundsException(
                    "Can not invoke LinkedList.get(),because \"index\" is out of range(0~size)");
        }
        QueIterator iterator=super.iterator();

        while (index-->0)
        {
            iterator.next();
        }
        return iterator.next();
    }

    @Override
    public E set(int index,E element) throws IndexOutOfBoundsException
    {
        if (index>=size()||index<0)
        {
            throw new IndexOutOfBoundsException(
                    "Can not invoke LinkedList.get(),because \"index\" is out of range(0~size)");
        }
        QueIterator iterator=super.iterator();

        while (index-->=0)
        {
            iterator.next();
        }
        return super.set(iterator,element);
    }

    @Override
    public void add(int index,E element) throws IndexOutOfBoundsException
    {
        addAll(index,Collections.singletonList(element));
    }

    @Override
    public E remove(int index) throws IndexOutOfBoundsException
    {
        if (index>=size()||index<0)
        {
            throw new IndexOutOfBoundsException(
                    "Can not invoke LinkedList.get(),because \"index\" is out of range(0~size)");
        }
        QueIterator iterator=super.iterator();

        while (index-->0)
        {
            iterator.next();
        }
        E e=iterator.next();
        iterator.remove();
        return e;
    }

    @NotNull
    @Override
    public ListIterator<E> listIterator()
    {
        return super.iterator();
    }

    @NotNull
    @Override
    public ListIterator<E> listIterator(int index)
    {
        ListIterator<E> iterator=listIterator();
        while (index-->0)
        {
            iterator.next();
        }
        return iterator;
    }

    @SafeVarargs
    public static <E> FastLinkedList<E> createList(E... o)
    {
        return new FastLinkedList<>(Arrays.asList(o));
    }

    @NotNull
    @Override
    public FastLinkedList<E> subList(int fromIndex,int toIndex) throws IndexOutOfBoundsException
    {
        if (fromIndex<0||toIndex>size()||fromIndex>toIndex)
        {
            throw new IndexOutOfBoundsException("Can not invoke LinkedList.subList(),because \"index\" is out of " +
                                                "range");
        }
        FastLinkedList<E> list=new FastLinkedList<>();
        ListIterator<E> iterator=list.listIterator(fromIndex);
        toIndex-=fromIndex;
        while (toIndex-->0)
        {
            list.addLast(iterator.next());
        }
        return list;
    }
}
