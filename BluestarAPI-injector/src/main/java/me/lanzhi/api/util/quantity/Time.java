package me.lanzhi.api.util.quantity;

import me.lanzhi.api.util.quantity.unit.TimeUnit;

/**
 * Represents a time value.
 */
public class Time
{
    private final long time;

    /**
     * Constructs a Time object with the given time value.
     *
     * @param time The time value.
     */
    public Time(long time)
    {
        this.time=time;
    }

    /**
     * Constructs a Time object with the given time value and unit.
     *
     * @param time The time value.
     * @param unit The time unit.
     */
    public Time(double time,TimeUnit unit)
    {
        this.time=Math.round(time*unit.time());
    }

    /**
     * Returns the time value.
     *
     * @return The time value.
     */
    public long time()
    {
        return time;
    }

    /**
     * Returns a string representation of the time value.
     * If the time value is greater than a day, it will be represented in days.
     * If the time value is greater than an hour, it will be represented in hours.
     * If the time value is greater than a minute, it will be represented in minutes.
     * If the time value is greater than a second, it will be represented in seconds.
     * Otherwise, it will be represented in milliseconds.
     *
     * @return The string representation of the time value.
     */
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

    /**
     * Returns a string representation of the time value in the specified unit.
     *
     * @param unit The time unit.
     * @return The string representation of the time value.
     */
    public String toString(TimeUnit unit)
    {
        return toString(unit,2);
    }

    /**
     * Returns a string representation of the time value in the specified unit with the specified decimal places.
     *
     * @param unit    The time unit.
     * @param decimal The number of decimal places.
     * @return The string representation of the time value.
     */
    public String toString(TimeUnit unit,int decimal)
    {
        return String.format("%."+decimal+"f %s",time(unit),unit);
    }

    /**
     * Returns the time value in the specified unit.
     *
     * @param unit The time unit.
     * @return The time value.
     */
    public double time(TimeUnit unit)
    {
        return time*1.0/unit.time();
    }
}