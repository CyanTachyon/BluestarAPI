package me.lanzhi.api.util.collection;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.RandomAccess;

public class BitVector implements Collection<Boolean>, RandomAccess, Cloneable, java.io.Serializable
{
    private static final long serialVersionUID=1L;
    private long[] data;
    private long top=-1;
    private long falseCount=0;
    private long trueCount=0;

    public BitVector(boolean... bits)
    {
        this();
        for (boolean b: bits)
        {
            put(b);
        }
    }

    public BitVector()
    {
        data=new long[1];
    }

    public void put(boolean b)
    {
        expand();
        if (b)
        {
            data[(int) (top>>6)]|=1L<<(top&63);
            trueCount++;
        }
        else
        {
            falseCount++;
        }
        ++top;
    }

    private void expand()
    {
        if (size()<data.length<<6)
        {
            return;
        }
        expand(data.length<<1);
    }

    public int size()
    {
        return (int) (top+1);
    }

    private void expand(int size)
    {
        if (size<data.length<<6)
        {
            return;
        }
        long[] newData=new long[size];
        System.arraycopy(data,0,newData,0,size());
        data=newData;
    }

    public BitVector(byte... bits)
    {
        this.data=new long[(bits.length>>3)+1];
        for (int i=0;i<bits.length-7;i+=8)
        {
            data[i>>3]=(bits[i]&0xFFL)|
                       ((bits[i+1]&0xFFL)<<8)|
                       ((bits[i+2]&0xFFL)<<16)|
                       ((bits[i+3]&0xFFL)<<24)|
                       ((bits[i+4]&0xFFL)<<32)|
                       ((bits[i+5]&0xFFL)<<40)|
                       ((bits[i+6]&0xFFL)<<48)|
                       ((bits[i+7]&0xFFL)<<56);
        }
        if ((bits.length&7)>0)
        {
            long l=0;
            for (int i=bits.length-1;i>=bits.length-(bits.length&7);i--)
            {
                l<<=8;
                l|=bits[i]&0xFFL;
            }
            data[data.length-1]=l;
        }
        for (long l: data)
        {
            trueCount+=Long.bitCount(l);
        }
        top=((long) bits.length<<3)-1;
        falseCount=size()-trueCount;
    }

    public BitVector(int... bits)
    {
        this.data=new long[(bits.length>>1)+1];
        for (int i=0;i<bits.length-1;i+=2)
        {
            data[i>>1]=(bits[i]&0xFFFFFFFFL)|((bits[i+1]&0xFFFFFFFFL)<<32);
        }
        if ((bits.length&1)==1)
        {
            data[data.length-1]=bits[bits.length-1]&0xFFFFFFFFL;
        }
        for (long l: data)
        {
            trueCount+=Long.bitCount(l);
        }
        top=((long) bits.length<<5)-1;
        falseCount=size()-trueCount;
    }

    public BitVector(long... bits)
    {
        this.data=Arrays.copyOf(bits,bits.length);
        for (long l: bits)
        {
            trueCount+=Long.bitCount(l);
        }
        top=((long) bits.length<<6)-1;
        falseCount=size()-trueCount;
    }

    public BitVector(BigInteger value)
    {
        this();
        for (int i=0;i<value.bitLength();i++)
        {
            put(value.testBit(i));
        }
    }

    public long falseCount()
    {
        return falseCount;
    }

    public long trueCount()
    {
        return trueCount;
    }

    public void put(boolean... bits)
    {
        for (boolean b: bits)
        {
            put(b);
        }
    }

    public void put(long... bits)
    {
        for (long l: bits)
        {
            for (int i=0;i<64;i++)
            {
                put((l&(1L<<i))!=0);
            }
        }
    }

    public void put(BigInteger value)
    {
        var bytes=value.toByteArray();
        for (byte b: bytes)
        {
            for (int i=0;i<8;i++)
            {
                put((b&(1<<i))!=0);
            }
        }
    }

    @Override
    public boolean isEmpty()
    {
        return size()==0;
    }

    @NotNull
    @Override
    public Iterator<Boolean> iterator()
    {
        return new Iterator<Boolean>()
        {
            private int index=0;

            @Override
            public boolean hasNext()
            {
                return index<size();
            }

            @Override
            public Boolean next()
            {
                return get(index++);
            }
        };
    }

    public boolean get(int index)
    {
        return (data[index>>6]&(1L<<(index&63)))!=0;
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T @NotNull [] a)
    {
        if (a.getClass().equals(Boolean[].class))
        {
            return (T[]) toArray();
        }
        return null;
    }

    @Override
    public Object[] toArray()
    {
        Object[] array=new Object[size()];
        for (int i=0;i<size();i++)
        {
            array[i]=get(i);
        }
        return array;
    }

    public boolean[] toBooleanArray()
    {
        boolean[] array=new boolean[size()];
        for (int i=0;i<size();i++)
        {
            array[i]=get(i);
        }
        return array;
    }

    @Override
    public boolean remove(Object o)
    {
        return false;
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
    public boolean contains(Object o)
    {
        if (o instanceof Boolean)
        {
            return contains((boolean) o);
        }
        return false;
    }

    public boolean contains(boolean b)
    {
        if (b)
        {
            return trueCount>0;
        }
        else
        {
            return falseCount>0;
        }
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Boolean> c)
    {
        for (Boolean b: c)
        {
            add(b);
        }
        return true;
    }

    @Override
    public boolean add(Boolean aBoolean)
    {
        put(aBoolean);
        return true;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c)
    {
        return false;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c)
    {
        return false;
    }

    @Override
    public void clear()
    {
        data=new long[1];
        top=-1;
        falseCount=0;
        trueCount=0;
    }

    @NotNull
    public BigInteger toBigInteger()
    {
        return new BigInteger(toString(),2);
    }

    @Override
    public String toString()
    {
        StringBuilder sb=new StringBuilder();
        for (int i=0;i<size();i++)
        {
            sb.append(get(i)?'1':'0');
        }
        return sb.toString();
    }

    @Override
    public BitVector clone()
    {
        BitVector bv=new BitVector();
        bv.data=data.clone();
        bv.top=top;
        bv.falseCount=falseCount;
        bv.trueCount=trueCount;
        return bv;
    }

    //左移
    public BitVector leftShift(int n)
    {
        if (n==0)
        {
            return clone();
        }
        if (n<0)
        {
            return rightShift(-n);
        }
        int shift=n&63;
        int shift0=n>>6;
        long[] newData=new long[data.length+shift0+1];
        System.arraycopy(data,0,newData,shift0,data.length);
        if (shift!=0)
        {
            for (int i=newData.length-1;i>shift0;i--)
            {
                newData[i]<<=shift;
                newData[i]|=newData[i-1] >>> (64-shift);
            }
            newData[shift0]<<=shift;
        }
        BitVector bv=new BitVector();
        bv.data=newData;
        bv.top=top+n;
        bv.trueCount=trueCount;
        bv.falseCount=size()-trueCount;
        return bv;
    }

    public void set(int index,boolean b)
    {
        if (index>=size())
        {
            expand((index>>6)+1);
            for (int i=size();i<index;i++)
            {
                put(false);
            }
            put(b);
            return;
        }
        if (get(index)==b)
        {
            return;
        }
        if (b)
        {
            data[index>>6]|=1L<<(index&63);
            trueCount++;
            falseCount--;
        }
        else
        {
            data[index>>6]&=~(1L<<(index&63));
            trueCount--;
            falseCount++;
        }
    }

    //右移
    public BitVector rightShift(int n)
    {
        if (n==0)
        {
            return clone();
        }
        if (n<0)
        {
            return leftShift(-n);
        }
        int shift=n&63;
        int shift0=n>>6;
        if (shift0>=data.length)
        {
            return new BitVector();
        }
        long[] newData=new long[data.length-shift0];
        System.arraycopy(data,shift0,newData,0,data.length-shift0);
        BitVector bv=new BitVector();
        bv.data=newData;
        bv.top=top-n;
        if (shift!=0)
        {
            for (int i=0;i<newData.length-1;i++)
            {
                newData[i] >>>= shift;
                newData[i]|=newData[i+1]<<(64-shift);
            }
            newData[newData.length-1] >>>= shift;
        }
        for (long newDatum: newData)
        {
            bv.trueCount+=Long.bitCount(newDatum);
        }
        bv.falseCount=bv.size()-bv.trueCount;
        return bv;
    }

    public void remove()
    {
        if (size()==0)
        {
            return;
        }
        if (get(size()-1))
        {
            trueCount--;
        }
        else
        {
            falseCount--;
        }
        top--;
    }

    //位运算

    public BitVector and(BitVector bv)
    {
        BitVector result=new BitVector();
        int newSize=Math.min(bv.size(),this.size());
        int len=newSize>>6;
        int len0=newSize&63;
        result.data=new long[len+1];
        for (int i=0;i<len;i++)
        {
            result.data[i]=this.data[i]&bv.data[i];
            result.trueCount+=Long.bitCount(result.data[i]);
        }
        if (len0!=0)
        {
            result.data[len]=this.data[len]&bv.data[len]&((1L<<len0)-1);
            result.trueCount+=Long.bitCount(result.data[len]);
        }
        result.falseCount=newSize-result.trueCount;
        result.top=newSize-1;
        return result;
    }

    public BitVector or(BitVector bv)
    {
        BitVector result=new BitVector();
        int newSize=Math.max(bv.size(),this.size());
        int len=newSize>>6;
        int len0=newSize&63;
        result.data=new long[len+1];
        for (int i=0;i<len;i++)
        {
            result.data[i]=this.data[i]|bv.data[i];
            result.trueCount+=Long.bitCount(result.data[i]);
        }
        if (len0!=0)
        {
            result.data[len]=this.data[len]|bv.data[len]|((1L<<len0)-1);
            result.trueCount+=Long.bitCount(result.data[len]);
        }
        result.falseCount=newSize-result.trueCount;
        result.top=newSize-1;
        return result;
    }

    public BitVector xor(BitVector bv)
    {
        BitVector result=new BitVector();
        int newSize=Math.max(bv.size(),this.size());
        int len=newSize>>6;
        int len0=newSize&63;
        result.data=new long[len+1];
        for (int i=0;i<len;i++)
        {
            result.data[i]=this.data[i]^bv.data[i];
            result.trueCount+=Long.bitCount(result.data[i]);
        }
        if (len0!=0)
        {
            result.data[len]=this.data[len]^bv.data[len]^((1L<<len0)-1);
            result.trueCount+=Long.bitCount(result.data[len]);
        }
        result.falseCount=newSize-result.trueCount;
        result.top=newSize-1;
        return result;
    }

    public BitVector not()
    {
        BitVector result=new BitVector();
        int newSize=this.size();
        int len=newSize>>6;
        int len0=newSize&63;
        result.data=new long[len+1];
        for (int i=0;i<len;i++)
        {
            result.data[i]=~this.data[i];
            result.trueCount+=Long.bitCount(result.data[i]);
        }
        if (len0!=0)
        {
            result.data[len]=~this.data[len]&((1L<<len0)-1);
            result.trueCount+=Long.bitCount(result.data[len]);
        }
        result.falseCount=newSize-result.trueCount;
        result.top=newSize-1;
        return result;
    }

    public BitVector andNot(BitVector bv)
    {
        BitVector result=new BitVector();
        int newSize=Math.max(bv.size(),this.size());
        int len=newSize>>6;
        int len0=newSize&63;
        result.data=new long[len+1];
        for (int i=0;i<len;i++)
        {
            result.data[i]=this.data[i]&~bv.data[i];
            result.trueCount+=Long.bitCount(result.data[i]);
        }
        if (len0!=0)
        {
            result.data[len]=this.data[len]&~bv.data[len]&((1L<<len0)-1);
            result.trueCount+=Long.bitCount(result.data[len]);
        }
        result.falseCount=newSize-result.trueCount;
        result.top=newSize-1;
        return result;
    }

    public int getMaxSize()
    {
        return data.length<<6;
    }

    public void setMaxSize(int size)
    {
        if (size<size())
        {
            throw new IllegalArgumentException("Size: "+size+" is less than current size: "+size());
        }
        int newSize=(size>>6)+1;
        if (newSize>data.length)
        {
            long[] newData=new long[newSize];
            System.arraycopy(data,0,newData,0,data.length);
            data=newData;
        }
    }
}