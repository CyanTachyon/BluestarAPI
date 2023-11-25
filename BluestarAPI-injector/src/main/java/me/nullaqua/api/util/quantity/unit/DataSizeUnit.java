package me.nullaqua.api.util.quantity.unit;

public enum DataSizeUnit
{
    B(1),
    KB(1024),
    MB(1024*1024),
    GB(1024*1024*1024),
    TB(1024*1024*1024*1024L);

    private final long size;

    /**
     * 构造方法，用于创建DataSizeUnit枚举对象
     *
     * @param size 大小
     */
    DataSizeUnit(long size)
    {
        this.size=size;
    }

    /**
     * 获取大小
     *
     * @return 大小
     */
    public long size()
    {
        return size;
    }

    /**
     * 将DataSizeUnit对象转换为字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString()
    {
        return name();
    }
}