package me.lanzhi.api.util.quantity.Unit;

public enum TimeUnit
{
    ms(1),
    s(1000),
    min(1000*60),
    h(1000*60*60),
    day(1000*60*60*24);

    private final long time;

    TimeUnit(long time)
    {
        this.time=time;
    }

    public long time()
    {
        return time;
    }

    @Override
    public String toString()
    {
        return name();
    }
}