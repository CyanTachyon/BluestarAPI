package me.nullaqua.api.collection;

import me.nullaqua.api.reflect.MethodAccessor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

public class LinkedDeque<E> implements Deque<E>, Cloneable, Serializable
{
    private Node<E> first;
    private Node<E> last;
    private int size;

    public LinkedDeque(Collection<E> collection)
    {
        this();
        addAll(collection);
    }

    public LinkedDeque()
    {
        clear();
    }

    @Override
    public void addFirst(E e)
    {
        first.insertAfter(e);
        size++;
    }

    @Override
    public void addLast(E e)
    {
        last.insertBefore(e);
        size++;
    }

    @Override
    public boolean offerFirst(E e)
    {
        addFirst(e);
        return true;
    }

    @Override
    public boolean offerLast(E e)
    {
        addLast(e);
        return true;
    }

    @Override
    public E removeFirst() throws NoSuchElementException
    {
        if (size==0)
        {
            throw new NoSuchElementException("Can not invoke FastDeque.removeFirst(),because the queue is empty.");
        }
        Node<E> node=first.getNext();
        E e=node.getVault();
        node.remove();
        size--;
        return e;
    }

    @Override
    public E removeLast() throws NoSuchElementException
    {
        if (size==0)
        {
            throw new NoSuchElementException("Can not invoke FastDeque.removeLast(),because the queue is empty.");
        }
        Node<E> node=last.getPrevious();
        E e=node.getVault();
        node.remove();
        size--;
        return e;
    }

    @Override
    public E pollFirst()
    {
        try
        {
            return removeFirst();
        }
        catch (NoSuchElementException e)
        {
            return null;
        }
    }

    @Override
    public E pollLast()
    {
        try
        {
            return removeLast();
        }
        catch (NoSuchElementException e)
        {
            return null;
        }
    }

    @Override
    public E getFirst() throws NoSuchElementException
    {
        if (size==0)
        {
            throw new NoSuchElementException("Can not invoke FastDeque.getFirst(),because the queue is empty.");
        }
        return first.getNext().getVault();
    }

    @Override
    public E getLast() throws NoSuchElementException
    {
        if (size==0)
        {
            throw new NoSuchElementException("Can not invoke FastDeque.getLast(),because the queue is empty.");
        }
        return last.getPrevious().getVault();
    }

    @Override
    public E peekFirst()
    {
        try
        {
            return getFirst();
        }
        catch (NoSuchElementException e)
        {
            return null;
        }
    }

    @Override
    public E peekLast()
    {
        try
        {
            return getLast();
        }
        catch (NoSuchElementException e)
        {
            return null;
        }
    }

    @Override
    public boolean removeFirstOccurrence(Object o)
    {
        Node<E> node=first.getNext();
        while (node!=last)
        {
            if (Objects.equals(o,node.getVault()))
            {
                node.remove();
                size--;
                return true;
            }
            node=node.getNext();
        }
        return false;
    }

    @Override
    public boolean removeLastOccurrence(Object o)
    {
        Node<E> node=last.getPrevious();
        while (node!=first)
        {
            if (Objects.equals(o,node.getVault()))
            {
                node.remove();
                size--;
                return true;
            }
            node=node.getPrevious();
        }
        return false;
    }

    @Override
    public boolean add(E e)
    {
        return this.offerLast(e);
    }

    @Override
    public boolean offer(E e)
    {
        return this.offerLast(e);
    }

    @Override
    public E remove()
    {
        return this.removeFirst();
    }

    @Override
    public E poll()
    {
        return this.pollFirst();
    }

    @Override
    public E element()
    {
        return this.getFirst();
    }

    @Override
    public E peek()
    {
        return this.peekFirst();
    }

    @Override
    public boolean addAll(Collection<? extends E> c)
    {
        for (E e: c)
        {
            addLast(e);
        }
        return true;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c)
    {
        boolean flag=false;
        for (Object object: c)
        {
            flag|=this.removeAll(object);
        }
        return flag;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c)
    {
        boolean flag=false;
        Node<E> node=first.getNext();
        while (node!=last)
        {
            if (!c.contains(node.getVault()))
            {
                node.remove();
                size--;
                flag=true;
            }
            node=node.getNext();
        }
        return flag;
    }

    @Override
    public LinkedDeque<E> clone()
    {
        LinkedDeque<E> deque=new LinkedDeque<>();
        for (E e: this)
        {
            deque.addLast(e);
        }
        return deque;
    }    @Override
    public void clear()
    {
        first=new Node<>();
        last=new Node<>();
        first.setPrevious(first);
        first.setNext(last);
        last.setVault(null);
        last.setPrevious(first);
        last.setNext(last);
        last.setVault(null);
        size=0;
    }

    @Override
    public void push(E e)
    {
        addFirst(e);
    }

    @Override
    public E pop()
    {
        return removeFirst();
    }

    public boolean removeAll(Object o)
    {
        boolean flag=false;
        Node<E> node=first.getNext();
        while (node!=last)
        {
            if (Objects.equals(o,node.getVault()))
            {
                node.remove();
                size--;
                flag=true;
            }
            node=node.getNext();
        }
        return flag;
    }

    @Override
    public boolean remove(Object o)
    {
        return removeFirstOccurrence(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c)
    {
        boolean flag=true;
        for (Object o: c)
        {
            flag&=contains(o);
        }
        return flag;
    }

    @Override
    public boolean contains(Object o)
    {
        Node<E> node=first.getNext();
        while (node!=last)
        {
            if (Objects.equals(o,node.getVault()))
            {
                return true;
            }
            node=node.getNext();
        }
        return false;
    }

    @Override
    public int size()
    {
        return size;
    }

    @Override
    public boolean isEmpty()
    {
        return size<=0;
    }

    @Override
    public QueIterator iterator()
    {
        return new QueIterator();
    }

    private static final class Node<E> implements Serializable
    {
        private E vault;
        private Node<E> previous;
        private Node<E> next;

        public Node(E vault,Node<E> previous,Node<E> next)
        {
            this.vault=vault;
            this.previous=previous;
            this.next=next;
        }

        public Node()
        {
            this(null,null,null);
        }

        public E getVault()
        {
            return vault;
        }

        public E setVault(E vault)
        {
            E v=this.vault;
            this.vault=vault;
            return v;
        }

        public Node<E> getPrevious()
        {
            return previous;
        }

        public void setPrevious(Node<E> previous)
        {
            this.previous=previous;
        }

        public Node<E> getNext()
        {
            return next;
        }

        public void setNext(Node<E> next)
        {
            this.next=next;
        }

        public void insertBefore(E e)
        {
            Node<E> node=new Node<>(e,getPrevious(),this);
            if (getPrevious()!=null)
            {
                getPrevious().setNext(node);
            }
            setPrevious(node);
        }

        public void insertAfter(E e)
        {
            Node<E> node=new Node<>(e,this,getNext());
            if (getNext()!=null)
            {
                getNext().setPrevious(node);
            }
            setNext(node);
        }

        public void remove()
        {
            if (getPrevious()!=null)
            {
                getPrevious().setNext(getNext());
            }
            if (getNext()!=null)
            {
                getNext().setPrevious(getPrevious());
            }
        }
    }

    public String toString()
    {
        Object[] objects=toArray();
        for (int i=0;i<objects.length;i++)
        {
            if (this==objects[i])
            {
                objects[i]="(this)";
            }
        }
        return Arrays.toString(objects);
    }    @NotNull
    @Override
    public E[] toArray()
    {
        E[] array=(E[]) new Object[size];
        int i=0;
        for (E e: this)
        {
            array[i++]=e;
        }
        return array;
    }

    public LinkedDeque<E> deepClone() throws Throwable
    {
        LinkedDeque<E> deque=new LinkedDeque<>();
        for (E e: this)
        {
            MethodAccessor methodAccessor=MethodAccessor.getDeclaredMethod(e.getClass(),"clone");
            methodAccessor.invoke(e);
            deque.addLast(e);
        }
        return deque;
    }    @NotNull
    @Override
    public <T> T[] toArray( T @NotNull []  a)
    {
        E[] objects=toArray();
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

    @NotNull
    @Override
    public DesQueIterator descendingIterator()
    {
        return new DesQueIterator();
    }

    @NotNull
    public Iterable<E> descending()
    {
        return DesQueIterator::new;
    }

    @NotNull
    public Iterable<E> ascending()
    {
        return QueIterator::new;
    }

    public void insert(QueIterator iterator,E e)
    {
        if (iterator.getNode()!=first)
        {
            iterator.getNode().insertBefore(e);
            iterator.index++;
            size++;
        }
    }

    public E set(QueIterator iterator,E e)
    {
        if (iterator.getP()!=first&&iterator.getP()!=last)
        {
            return iterator.getP().setVault(e);
        }
        return null;
    }

    public E removeItem(QueIterator iterator)
    {
        Node<E> node=iterator.getP();
        if (node!=null&&node!=first&&node!=last)
        {
            node.remove();
            size--;
            if (iterator.pr>0)
            {
                iterator.index--;
            }
            return node.getVault();
        }
        return null;
    }

    public void insert(DesQueIterator iterator,E e)
    {
        if (iterator.getNode()!=last)
        {
            iterator.getNode().insertAfter(e);
            size++;
        }
    }

    public E set(DesQueIterator iterator,E e)
    {
        if (iterator.getP()!=first&&iterator.getP()!=last)
        {
            return iterator.getP().setVault(e);
        }
        return null;
    }

    public E removeItem(DesQueIterator iterator)
    {
        Node<E> node=iterator.getP();
        if (node!=null&&node!=first&&node!=last)
        {
            node.remove();
            size--;
            if (iterator.pr<0)
            {
                iterator.index--;
            }
            return node.getVault();
        }
        return null;
    }

    public int indexOf(Object o)
    {
        int i=0;
        for (E e: this)
        {
            if (Objects.equals(o,e))
            {
                return i;
            }
            i++;
        }
        return -1;
    }

    public int lastIndexOf(Object o)
    {
        int i=size-1;
        for (E e: descending())
        {
            if (Objects.equals(o,e))
            {
                return i;
            }
            i--;
        }
        return -1;
    }

    private void writeObject(ObjectOutputStream out) throws IOException
    {
        out.writeInt(size);
        for (var x:this)
        {
            out.writeObject(x);
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        var x=in.readInt();
        clear();
        while (x-->0)
        {
            add((E) in.readObject());
        }
    }

    @Override
    public int hashCode()
    {
        int hashCode=1;
        for (E e: this)
        {
            hashCode=31*hashCode+(e==null?0:e.hashCode());
        }
        return hashCode;
    }

    public boolean equals(Object o)
    {
        if (o==this)
        {
            return true;
        }
        if (!(o instanceof Collection))
        {
            return false;
        }

        Iterator<E> e1=iterator();
        Iterator<?> e2=((Collection<?>) o).iterator();
        while (e1.hasNext()&&e2.hasNext())
        {
            E o1=e1.next();
            Object o2=e2.next();
            if (!(Objects.equals(o1,o2)))
            {
                return false;
            }
        }
        return !(e1.hasNext()||e2.hasNext());
    }

    public class QueIterator implements ListIterator<E>
    {
        private Node<E> node;
        private Node<E> p;
        private int index;
        private int pr=0;

        public QueIterator()
        {
            node=first.next;
            p=null;
            index=0;
        }

        @Override
        public boolean hasNext()
        {
            return node!=last;
        }

        @Override
        public E next()
        {
            if (hasNext())
            {
                p=node;
                node=node.next;
                index=Math.min(index+1,size);
                pr=1;
                return p.vault;
            }
            return null;
        }

        @Override
        public void remove()
        {
            LinkedDeque.this.removeItem(this);
        }

        public void set(E e)
        {
            LinkedDeque.this.set(this,e);
        }

        private Node<E> getNode()
        {
            return node;
        }

        @Override
        public boolean hasPrevious()
        {
            return node.previous!=first;
        }

        @Override
        public E previous()
        {
            if (hasPrevious())
            {
                p=node.previous;
                node=node.previous;
                index=Math.max(index-1,0);
                pr=-1;
                return p.vault;
            }
            return null;
        }

        @Override
        public int nextIndex()
        {
            return index;
        }

        @Override
        public int previousIndex()
        {
            return index-1;
        }

        @Override
        public void add(E e)
        {
            LinkedDeque.this.insert(this,e);
        }

        private Node<E> getP()
        {
            return p;
        }
    }

    public class DesQueIterator implements ListIterator<E>
    {
        int pr=0;
        private Node<E> node;
        private Node<E> p;
        private int index;

        public DesQueIterator()
        {
            node=last.previous;
            p=null;
            index=size-1;
        }

        @Override
        public boolean hasNext()
        {
            return node!=first;
        }

        @Override
        public E next()
        {
            if (hasNext())
            {
                p=node;
                node=node.previous;
                index=Math.max(index-1,-1);
                pr=1;
                return p.vault;
            }
            return null;
        }

        @Override
        public void remove()
        {
            LinkedDeque.this.removeItem(this);
        }

        public void set(E e)
        {
            LinkedDeque.this.set(this,e);
        }

        private Node<E> getNode()
        {
            return node;
        }

        @Override
        public boolean hasPrevious()
        {
            return node.next!=last;
        }

        @Override
        public E previous()
        {
            if (hasPrevious())
            {
                p=node.next;
                node=node.next;
                index=Math.min(index+1,size-1);
                pr=-1;
                return p.vault;
            }
            return null;
        }

        @Override
        public int nextIndex()
        {
            return index;
        }

        @Override
        public int previousIndex()
        {
            return index+1;
        }

        @Override
        public void add(E e)
        {
            LinkedDeque.this.insert(this,e);
        }

        private Node<E> getP()
        {
            return p;
        }
    }
}