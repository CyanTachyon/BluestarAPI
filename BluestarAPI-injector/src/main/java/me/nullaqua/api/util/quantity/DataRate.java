package me.nullaqua.api.util.quantity;

import me.nullaqua.api.util.quantity.unit.DataRateUnit;
import me.nullaqua.api.util.quantity.unit.DataSizeUnit;
import me.nullaqua.api.util.quantity.unit.TimeUnit;

/**
 * 数据速率类，用于表示数据的传输速率。
 */
public class DataRate
{
    private final long size; // 数据大小
    private final long time; // 时间

    /**
     * 构造方法，创建一个DataRate对象。
     *
     * @param size 数据大小
     * @param time 时间
     */
    public DataRate(long size,long time)
    {
        this.size=size;
        this.time=time;
    }

    /**
     * 构造方法，创建一个DataRate对象。
     *
     * @param size     数据大小
     * @param sizeUnit 数据大小单位
     * @param time     时间
     * @param timeUnit 时间单位
     */
    public DataRate(double size,DataSizeUnit sizeUnit,double time,TimeUnit timeUnit)
    {
        this.size=Math.round(size*sizeUnit.size());
        this.time=Math.round(time*timeUnit.time());
    }

    /**
     * 构造方法，创建一个DataRate对象。
     *
     * @param rate 数据速率
     * @param unit 数据速率单位
     */
    public DataRate(double rate,DataRateUnit unit)
    {
        this(rate,unit.sizeUnit(),unit.timeUnit());
    }

    /**
     * 构造方法，创建一个DataRate对象。
     *
     * @param rate     数据速率
     * @param sizeUnit 数据大小单位
     * @param timeUnit 时间单位
     */
    public DataRate(double rate,DataSizeUnit sizeUnit,TimeUnit timeUnit)
    {
        this.size=Math.round(rate*sizeUnit.size());
        this.time=timeUnit.time();
    }

    /**
     * 构造方法，创建一个DataRate对象。
     *
     * @param size 数据大小
     * @param time 时间
     */
    public DataRate(DataSize size,Time time)
    {
        this.size=size.size();
        this.time=time.time();
    }

    /**
     * 构造方法，创建一个DataRate对象。
     *
     * @param size     数据大小
     * @param timeUnit 时间单位
     */
    public DataRate(DataSize size,TimeUnit timeUnit)
    {
        this.size=size.size();
        this.time=timeUnit.time();
    }

    /**
     * 构造方法，创建一个DataRate对象。
     *
     * @param sizeUnit 数据大小单位
     * @param time     时间
     */
    public DataRate(DataSizeUnit sizeUnit,Time time)
    {
        this.size=sizeUnit.size();
        this.time=time.time();
    }

    /**
     * 获取数据大小。
     *
     * @return 数据大小
     */
    public long size()
    {
        return size;
    }

    /**
     * 获取时间。
     *
     * @return 时间
     */
    public long time()
    {
        return time;
    }

    /**
     * 获取数据速率。
     *
     * @param sizeUnit 数据大小单位
     * @param timeUnit 时间单位
     * @return 数据速率
     */
    public double rate(DataSizeUnit sizeUnit,TimeUnit timeUnit)
    {
        return size(sizeUnit)/time(timeUnit);
    }

    /**
     * 获取指定单位的数据大小。
     *
     * @param unit 数据大小单位
     * @return 指定单位的数据大小
     */
    public double size(DataSizeUnit unit)
    {
        return size*1.0/unit.size();
    }

    /**
     * 获取指定单位的时间。
     *
     * @param unit 时间单位
     * @return 指定单位的时间
     */
    public double time(TimeUnit unit)
    {
        return time*1.0/unit.time();
    }

    /**
     * 将数据速率转换为字符串表示。
     *
     * @param sizeUnit 数据大小单位
     * @param timeUnit 时间单位
     * @return 数据速率的字符串表示
     */
    public String toString(DataSizeUnit sizeUnit,TimeUnit timeUnit)
    {
        return toString(sizeUnit,timeUnit,2);
    }

    /**
     * 将数据速率转换为字符串表示。
     *
     * @param unit 数据速率单位
     * @return 数据速率的字符串表示
     */
    public String toString(DataRateUnit unit)
    {
        return toString(unit,2);
    }

    /**
     * 将数据速率转换为字符串表示。
     *
     * @param unit    数据速率单位
     * @param decimal 小数位数
     * @return 数据速率的字符串表示
     */
    public String toString(DataRateUnit unit,int decimal)
    {
        return toString(unit.sizeUnit(),unit.timeUnit(),decimal);
    }

    /**
     * 将数据速率转换为字符串表示。
     *
     * @param sizeUnit 数据大小单位
     * @param timeUnit 时间单位
     * @param decimal  小数位数
     * @return 数据速率的字符串表示
     */
    public String toString(DataSizeUnit sizeUnit,TimeUnit timeUnit,int decimal)
    {
        return String.format("%."+decimal+"f %s/%s",rate(new DataRateUnit(sizeUnit,timeUnit)),sizeUnit,timeUnit);
    }

    /**
     * 获取数据速率。
     *
     * @param unit 数据速率单位
     * @return 数据速率
     */
    public double rate(DataRateUnit unit)
    {
        return size(unit)/time(unit);
    }

    /**
     * 获取指定单位的数据大小。
     *
     * @param unit 数据速率单位
     * @return 指定单位的数据大小
     */
    public double size(DataRateUnit unit)
    {
        return size(unit.sizeUnit());
    }

    /**
     * 获取指定单位的时间。
     *
     * @param unit 数据速率单位
     * @return 指定单位的时间
     */
    public double time(DataRateUnit unit)
    {
        return time(unit.timeUnit());
    }

    /**
     * 将数据速率转换为字符串表示。
     *
     * @param unit 时间单位
     * @return 数据速率的字符串表示
     */
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

    /**
     * 将数据速率转换为字符串表示，使用秒作为时间单位。
     *
     * @return 数据速率的字符串表示
     */
    @Override
    public String toString()
    {
        return toString(TimeUnit.s);
    }

    public DataRate add(DataRate dataRate)
    {
        return new DataRate(this.rate(DataSizeUnit.B,TimeUnit.s)+dataRate.rate(DataSizeUnit.B,TimeUnit.s),DataSizeUnit.B,TimeUnit.s);
    }

    public DataRate sub(DataRate dataRate)
    {
        return new DataRate(this.rate(DataSizeUnit.B,TimeUnit.s)-dataRate.rate(DataSizeUnit.B,TimeUnit.s),DataSizeUnit.B,TimeUnit.s);
    }
}