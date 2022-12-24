package me.lanzhi.api.util;

import java.util.Iterator;

public final class ForUtils
{
    private ForUtils()
    {
    }

    public static Iterable<Integer> range(int x)
    {
        return range(0,x);
    }

    public static Iterable<Integer> range(int x,int y)
    {
        return range(x,y,1);
    }

    public static Iterable<Integer> range(int x,int y,int d)
    {
        return ()->new Iterator<>()
        {
            int i=x;
            @Override
            public boolean hasNext()
            {
                return i<y;
            }

            @Override
            public Integer next()
            {
                int x=i;
                i+=d;
                return x;
            }
        };
    }

    public static Iterable<Double> range(double x,double y)
    {
        return range(x,y,1);
    }

    public static Iterable<Double> range(double x,double y,int d)
    {
        return ()->new Iterator<>()
        {
            double i=x;
            @Override
            public boolean hasNext()
            {
                return i<y;
            }

            @Override
            public Double next()
            {
                var x=i;
                i+=d;
                return x;
            }
        };
    }

    public static <T> Iterable<T> repeat(T t,int n)
    {
        return ()->new Iterator<>()
        {
            int i=0;

            @Override
            public boolean hasNext()
            {
                return i<n;
            }

            @Override
            public T next()
            {
                i++;
                return t;
            }
        };
    }

    public static <T> Iterable<T> cycle(T[] t)
    {
        return ()->new Iterator<>()
        {
            int i=0;

            @Override
            public boolean hasNext()
            {
                return true;
            }

            @Override
            public T next()
            {
                var x=t[i];
                i=(i+1)%t.length;
                return x;
            }
        };
    }

    public static <T> Iterable<T> forEach(T[] t)
    {
        return forEach(t,0,t.length);
    }

    public static <T> Iterable<T> forEach(T[] t,int start,int end)
    {
        return ()->new Iterator<>()
        {
            private final Iterator<Integer> iterator=ForUtils.range(start,end).iterator();

            @Override
            public boolean hasNext()
            {
                return iterator.hasNext();
            }

            @Override
            public T next()
            {
                return t[iterator.next()];
            }
        };
    }

    public static <T> Iterable<T> forEach(T[] t,int start)
    {
        return forEach(t,start,t.length);
    }

    public static Iterable<Integer> forEach(int[] t)
    {
        return forEach(t,0,t.length);
    }

    public static Iterable<Integer> forEach(int[] t,int start,int end)
    {
        return ()->new Iterator<>()
        {
            private final Iterator<Integer> iterator=ForUtils.range(start,end).iterator();

            @Override
            public boolean hasNext()
            {
                return iterator.hasNext();
            }

            @Override
            public Integer next()
            {
                return t[iterator.next()];
            }
        };
    }

    public static Iterable<Integer> forEach(int[] t,int start)
    {
        return forEach(t,start,t.length);
    }

    public static Iterable<Double> forEach(double[] t)
    {
        return forEach(t,0,t.length);
    }

    public static Iterable<Double> forEach(double[] t,int start,int end)
    {
        return ()->new Iterator<>()
        {
            private final Iterator<Integer> iterator=ForUtils.range(start,end).iterator();

            @Override
            public boolean hasNext()
            {
                return iterator.hasNext();
            }

            @Override
            public Double next()
            {
                return t[iterator.next()];
            }
        };
    }

    public static Iterable<Double> forEach(double[] t,int start)
    {
        return forEach(t,start,t.length);
    }

    public static Iterable<Float> forEach(float[] t)
    {
        return forEach(t,0,t.length);
    }

    public static Iterable<Float> forEach(float[] t,int start,int end)
    {
        return ()->new Iterator<>()
        {
            private final Iterator<Integer> iterator=ForUtils.range(start,end).iterator();

            @Override
            public boolean hasNext()
            {
                return iterator.hasNext();
            }

            @Override
            public Float next()
            {
                return t[iterator.next()];
            }
        };
    }

    public static Iterable<Float> forEach(float[] t,int start)
    {
        return forEach(t,start,t.length);
    }

    public static Iterable<Long> forEach(long[] t)
    {
        return forEach(t,0,t.length);
    }

    public static Iterable<Long> forEach(long[] t,int start,int end)
    {
        return ()->new Iterator<>()
        {
            private final Iterator<Integer> iterator=ForUtils.range(start,end).iterator();

            @Override
            public boolean hasNext()
            {
                return iterator.hasNext();
            }

            @Override
            public Long next()
            {
                return t[iterator.next()];
            }
        };
    }

    public static Iterable<Long> forEach(long[] t,int start)
    {
        return forEach(t,start,t.length);
    }

    public static Iterable<Short> forEach(short[] t)
    {
        return forEach(t,0,t.length);
    }

    public static Iterable<Short> forEach(short[] t,int start,int end)
    {
        return ()->new Iterator<>()
        {
            private final Iterator<Integer> iterator=ForUtils.range(start,end).iterator();

            @Override
            public boolean hasNext()
            {
                return iterator.hasNext();
            }

            @Override
            public Short next()
            {
                return t[iterator.next()];
            }
        };
    }

    public static Iterable<Short> forEach(short[] t,int start)
    {
        return forEach(t,start,t.length);
    }

    public static Iterable<Byte> forEach(byte[] t)
    {
        return forEach(t,0,t.length);
    }

    public static Iterable<Byte> forEach(byte[] t,int start,int end)
    {
        return ()->new Iterator<>()
        {
            private final Iterator<Integer> iterator=ForUtils.range(start,end).iterator();

            @Override
            public boolean hasNext()
            {
                return iterator.hasNext();
            }

            @Override
            public Byte next()
            {
                return t[iterator.next()];
            }
        };
    }

    public static Iterable<Byte> forEach(byte[] t,int start)
    {
        return forEach(t,start,t.length);
    }

    public static Iterable<Character> forEach(char[] t)
    {
        return forEach(t,0,t.length);
    }

    public static Iterable<Character> forEach(char[] t,int start,int end)
    {
        return ()->new Iterator<>()
        {
            private final Iterator<Integer> iterator=ForUtils.range(start,end).iterator();

            @Override
            public boolean hasNext()
            {
                return iterator.hasNext();
            }

            @Override
            public Character next()
            {
                return t[iterator.next()];
            }
        };
    }

    public static Iterable<Character> forEach(char[] t,int start)
    {
        return forEach(t,start,t.length);
    }

    public static Iterable<Boolean> forEach(boolean[] t)
    {
        return forEach(t,0,t.length);
    }

    public static Iterable<Boolean> forEach(boolean[] t,int start,int end)
    {
        return ()->new Iterator<>()
        {
            private final Iterator<Integer> iterator=ForUtils.range(start,end).iterator();

            @Override
            public boolean hasNext()
            {
                return iterator.hasNext();
            }

            @Override
            public Boolean next()
            {
                return t[iterator.next()];
            }
        };
    }

    public static Iterable<Boolean> forEach(boolean[] t,int start)
    {
        return forEach(t,start,t.length);
    }

    public static Iterable<Character> forEach(String s)
    {
        return ()->new Iterator<>()
        {
            int i=0;

            @Override
            public boolean hasNext()
            {
                return i<s.length();
            }

            @Override
            public Character next()
            {
                var x=s.charAt(i);
                i++;
                return x;
            }
        };
    }
}
