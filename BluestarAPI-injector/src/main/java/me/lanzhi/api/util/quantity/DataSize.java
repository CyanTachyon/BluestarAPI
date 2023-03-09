package me.lanzhi.api.util.quantity;

import me.lanzhi.api.util.quantity.Unit.DataSizeUnit;

public class DataSize
{
    private final long size;

    public DataSize(long size)
    {
        this.size=size;
    }

    public DataSize(double size,DataSizeUnit unit)
    {
        this.size=Math.round(size*unit.size());
    }

    public long size()
    {
        return size;
    }

    @Override
    public String toString()
    {
        if (size>>40>0)
        {
            return toString(DataSizeUnit.TB);
        }
        else if (size>>30>0)
        {
            return toString(DataSizeUnit.GB);
        }
        else if (size>>20>0)
        {
            return toString(DataSizeUnit.MB);
        }
        else if (size>>10>0)
        {
            return toString(DataSizeUnit.KB);
        }
        else
        {
            return toString(DataSizeUnit.B);
        }
    }

    public String toString(DataSizeUnit unit)
    {
        return toString(unit,2);
    }

    public String toString(DataSizeUnit unit,int decimal)
    {
        return String.format("%."+decimal+"f %s",size(unit),unit);
    }

    public double size(DataSizeUnit unit)
    {
        return size*1.0/unit.size();
    }
}