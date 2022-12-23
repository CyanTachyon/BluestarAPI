package me.lanzhi.api.util.collection;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class LinkedStack<E> implements Iterable<E>
{
    private int size=0;
    private Node<E> top;

    public LinkedStack()
    {
        clear();
    }

    public int size()
    {
        return size;
    }

    public boolean isEmpty()
    {
        return size==0;
    }

    @NotNull
    @Override
    public Iterator<E> iterator()
    {
        return new StackIterator<>(top);
    }

    @NotNull
    public Object[] toArray()
    {
        Object[] array=new Object[size];
        int i=size-1;
        for (E e: this)
        {
            array[size--]=e;
        }
        return array;
    }

    @NotNull
    public <T> T[] toArray(T[] a)
    {
        Object[] objects=toArray();
        if (a.length<size)
        {
            return (T[]) Arrays.copyOf(objects,size,a.getClass());
        }
        System.arraycopy(objects,0,a,0,size);
        if (a.length>size)
        {
            a[size]=null;
        }
        return a;
    }

    public void push(E e)
    {
        top=new Node<>(e,top);
        size++;
    }

    public void pushAll(@NotNull Collection<? extends E> c)
    {
        for (E e: c)
        {
            this.push(e);
        }
    }

    public void clear()
    {
        top=null;
        size=0;
    }

    private void writeObject(ObjectOutputStream out) throws IOException
    {
        out.writeObject(toArray());
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        var objects=(E[])in.readObject();
        clear();
        for (E e:objects)
        {
            push(e);
        }
    }

    private static final class Node<E>
    {
        private final E vault;
        private final Node<E> next;

        public Node()
        {
            this.vault=null;
            this.next=this;
        }

        private Node(E vault,Node<E> next)
        {
            this.vault=vault;
            this.next=next;
        }

        public E getVault()
        {
            return vault;
        }

        public Node<E> getNext()
        {
            return next;
        }
    }

    public static final class StackIterator<E> implements Iterator<E>
    {
        private Node<E> node;

        private StackIterator(Node<E> top)
        {
            node=top;
        }

        @Override
        public boolean hasNext()
        {
            return node!=null;
        }

        @Override
        public E next()
        {
            E e=get();
            nextOne();
            return e;
        }

        public E get()
        {
            return node!=null?node.getVault():null;
        }

        public void nextOne()
        {
            node=node!=null?node.getNext():null;
        }
    }
}
