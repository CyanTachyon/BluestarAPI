package me.lanzhi.api.util.quantity;

import me.lanzhi.api.util.quantity.unit.DataRateUnit;
import me.lanzhi.api.util.quantity.unit.DataSizeUnit;
import me.lanzhi.api.util.quantity.unit.TimeUnit;


public class DataRate
{
    private final long size;
    private final long time;

    public DataRate(long size,long time)
    {
        this.size=size;
        this.time=time;
    }

    public DataRate(double size,DataSizeUnit sizeUnit,double time,TimeUnit timeUnit)
    {
        this.size=Math.round(size*sizeUnit.size());
        this.time=Math.round(time*timeUnit.time());
    }

    public DataRate(double rate,DataRateUnit unit)
    {
        this(rate,unit.sizeUnit(),unit.timeUnit());
    }

    public DataRate(double rate,DataSizeUnit sizeUnit,TimeUnit timeUnit)
    {
        this.size=Math.round(rate*sizeUnit.size());
        this.time=timeUnit.time();
    }

    public DataRate(DataSize size,Time time)
    {
        this.size=size.size();
        this.time=time.time();
    }

    public DataRate(DataSize size,TimeUnit timeUnit)
    {
        this.size=size.size();
        this.time=timeUnit.time();
    }

    public DataRate(DataSizeUnit sizeUnit,Time time)
    {
        this.size=sizeUnit.size();
        this.time=time.time();
    }

    public long size()
    {
        return size;
    }

    public long time()
    {
        return time;
    }

    public double rate(DataSizeUnit sizeUnit,TimeUnit timeUnit)
    {
        return size(sizeUnit)/time(timeUnit);
    }

    public double size(DataSizeUnit unit)
    {
        return size*1.0/unit.size();
    }

    public double time(TimeUnit unit)
    {
        return time*1.0/unit.time();
    }

    public String toString(DataSizeUnit sizeUnit,TimeUnit timeUnit)
    {
        return toString(sizeUnit,timeUnit,2);
    }

    public String toString(DataRateUnit unit)
    {
        return toString(unit,2);
    }

    public String toString(DataRateUnit unit,int decimal)
    {
        return toString(unit.sizeUnit(),unit.timeUnit(),decimal);
    }

    public String toString(DataSizeUnit sizeUnit,TimeUnit timeUnit,int decimal)
    {
        return String.format("%."+decimal+"f %s/%s",rate(new DataRateUnit(sizeUnit,timeUnit)),sizeUnit,timeUnit);
    }

    public double rate(DataRateUnit unit)
    {
        return size(unit)/time(unit);
    }

    public double size(DataRateUnit unit)
    {
        return size(unit.sizeUnit());
    }

    public double time(DataRateUnit unit)
    {
        return time(unit.timeUnit());
    }

    public String toString(TimeUnit unit)
    {
        if (rate(new DataRateUnit(DataSizeUnit.TB,unit))>1)
        {
            return toString(DataSizeUnit.TB,unit);
        }
        else if (rate(new DataRateUnit(DataSizeUnit.GB,unit))>1)
        {
            return toString(DataSizeUnit.GB,unit);
        }
        else if (rate(new DataRateUnit(DataSizeUnit.MB,unit))>1)
        {
            return toString(DataSizeUnit.MB,unit);
        }
        else if (rate(new DataRateUnit(DataSizeUnit.KB,unit))>1)
        {
            return toString(DataSizeUnit.KB,unit);
        }
        else
        {
            return toString(DataSizeUnit.B,unit);
        }
    }

    @Override
    public String toString()
    {
        return toString(TimeUnit.s);
    }
}