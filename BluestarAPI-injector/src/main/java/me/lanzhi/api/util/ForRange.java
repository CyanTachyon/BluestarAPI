package me.lanzhi.api.util;

import java.util.Iterator;

public final class ForRange
{
    private ForRange()
    {}

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
}
