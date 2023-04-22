package me.lanzhi.api.util.quantity.unit;

public enum DataSizeUnit
{
    B(1),
    KB(1024),
    MB(1024*1024),
    GB(1024*1024*1024),
    TB(1024*1024*1024*1024L);

    private final long size;

    DataSizeUnit(long size)
    {
        this.size=size;
    }

    public long size()
    {
        return size;
    }

    @Override
    public String toString()
    {
        return name();
    }
}