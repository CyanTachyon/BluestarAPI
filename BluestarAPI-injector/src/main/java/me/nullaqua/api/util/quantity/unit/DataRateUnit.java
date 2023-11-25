package me.nullaqua.api.util.quantity.unit;

public class DataRateUnit
{
    private final DataSizeUnit sizeUnit;
    private final TimeUnit timeUnit;

    /**
     * 构造方法，用于创建DataRateUnit对象
     *
     * @param sizeUnit 数据大小单位
     * @param timeUnit 时间单位
     */
    public DataRateUnit(DataSizeUnit sizeUnit,TimeUnit timeUnit)
    {
        this.sizeUnit=sizeUnit;
        this.timeUnit=timeUnit;
    }

    /**
     * 获取数据大小单位
     *
     * @return 数据大小单位
     */
    public DataSizeUnit sizeUnit()
    {
        return sizeUnit;
    }

    /**
     * 获取时间单位
     *
     * @return 时间单位
     */
    public TimeUnit timeUnit()
    {
        return timeUnit;
    }

    /**
     * 将DataRateUnit对象转换为字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString()
    {
        return sizeUnit+"/"+timeUnit;
    }
}