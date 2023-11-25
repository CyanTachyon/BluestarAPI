package me.nullaqua.api.util.quantity.unit;

public enum TimeUnit
{
    ms(1),
    s(1000),
    min(1000*60),
    h(1000*60*60),
    day(1000*60*60*24);

    private final long time;

    /**
     * 构造方法，用于创建TimeUnit枚举对象
     *
     * @param time 时间
     */
    TimeUnit(long time)
    {
        this.time=time;
    }

    /**
     * 获取时间
     *
     * @return 时间
     */
    public long time()
    {
        return time;
    }

    /**
     * 将TimeUnit对象转换为字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString()
    {
        return name();
    }
}