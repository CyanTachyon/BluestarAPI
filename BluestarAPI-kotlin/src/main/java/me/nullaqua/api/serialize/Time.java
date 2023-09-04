package me.nullaqua.api.serialize;

public class Time
{
    long time;
    long clock;

    public Time(long time,long clock)
    {
        this.time=time;
        this.clock=clock;
    }

    public Time()
    {
        this.time=System.currentTimeMillis();
        this.clock=System.nanoTime();
    }

    public boolean before(Time time)
    {
        return this.time<time.time||(this.time==time.time&&this.clock<=time.clock);
    }

    public boolean after(Time time)
    {
        return this.time>time.time||(this.time==time.time&&this.clock>=time.clock);
    }

    public boolean equals(Time time)
    {
        return this.time==time.time&&this.clock==time.clock;
    }
}