package me.lanzhi.api.util.quantity;

import me.lanzhi.api.util.quantity.unit.TimeUnit;

public class Time
{
    private final long time;

    public Time(long time)
    {
        this.time=time;
    }

    public Time(double time,TimeUnit unit)
    {
        this.time=Math.round(time*unit.time());
    }

    public long time()
    {
        return time;
    }

    @Override
    public String toString()
    {
        if (time>TimeUnit.day.time())
        {
            return toString(TimeUnit.day);
        }
        else if (time>TimeUnit.h.time())
        {
            return toString(TimeUnit.h);
        }
        else if (time>TimeUnit.min.time())
        {
            return toString(TimeUnit.min);
        }
        else if (time>TimeUnit.s.time())
        {
            return toString(TimeUnit.s);
        }
        else
        {
            return toString(TimeUnit.ms);
        }
    }

    public String toString(TimeUnit unit)
    {
        return toString(unit,2);
    }

    public String toString(TimeUnit unit,int decimal)
    {
        return String.format("%."+decimal+"f %s",time(unit),unit);
    }

    public double time(TimeUnit unit)
    {
        return time*1.0/unit.time();
    }
}