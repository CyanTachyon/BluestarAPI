package me.lanzhi.api.util.quantity.unit;

public class DataRateUnit
{
    private final DataSizeUnit sizeUnit;
    private final TimeUnit timeUnit;

    public DataRateUnit(DataSizeUnit sizeUnit,TimeUnit timeUnit)
    {
        this.sizeUnit=sizeUnit;
        this.timeUnit=timeUnit;
    }

    public DataSizeUnit sizeUnit()
    {
        return sizeUnit;
    }

    public TimeUnit timeUnit()
    {
        return timeUnit;
    }

    @Override
    public String toString()
    {
        return sizeUnit+"/"+timeUnit;
    }
}