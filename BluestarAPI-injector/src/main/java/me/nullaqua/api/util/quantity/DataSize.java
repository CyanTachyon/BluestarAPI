package me.nullaqua.api.util.quantity;

import me.nullaqua.api.util.quantity.unit.DataSizeUnit;

public class DataSize
{
    private final long size;

    /**
     * 构造方法，用于创建DataSize对象。
     *
     * @param size 数据大小
     */
    public DataSize(long size)
    {
        this.size=size;
    }

    /**
     * 构造方法，用于创建DataSize对象。
     *
     * @param size 数据大小
     * @param unit 数据大小单位
     */
    public DataSize(double size,DataSizeUnit unit)
    {
        this.size=Math.round(size*unit.size());
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
     * 将数据大小转换为字符串表示，根据数据大小选择合适的单位。
     *
     * @return 数据大小的字符串表示
     */
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

    /**
     * 将数据大小转换为指定单位的字符串表示，保留两位小数。
     *
     * @param unit 数据大小单位
     * @return 数据大小的字符串表示
     */
    public String toString(DataSizeUnit unit)
    {
        return toString(unit,2);
    }

    /**
     * 将数据大小转换为指定单位的字符串表示，指定小数位数。
     *
     * @param unit    数据大小单位
     * @param decimal 小数位数
     * @return 数据大小的字符串表示
     */
    public String toString(DataSizeUnit unit,int decimal)
    {
        return String.format("%."+decimal+"f %s",size(unit),unit);
    }

    /**
     * 将数据大小转换为指定单位的大小。
     *
     * @param unit 数据大小单位
     * @return 指定单位的数据大小
     */
    public double size(DataSizeUnit unit)
    {
        return size*1.0/unit.size();
    }

    public DataSize add(DataSize other)
    {
        return new DataSize(size+other.size);
    }

    public DataSize sub(DataSize other)
    {
        return new DataSize(size-other.size);
    }
}