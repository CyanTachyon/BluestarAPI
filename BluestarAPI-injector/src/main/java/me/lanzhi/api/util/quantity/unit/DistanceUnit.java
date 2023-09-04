package me.lanzhi.api.util.quantity.unit;

public enum DistanceUnit
{
    M(1000),
    KM(1000000),
    DM(100),
    CM(10),
    MM(1),
    UM(0.001),
    NM(0.000000001);

    private final double distance;

    /**
     * 构造方法，用于创建DistanceUnit枚举对象
     *
     * @param distance 距离
     */
    DistanceUnit(double distance)
    {
        this.distance=distance;
    }

    /**
     * 获取距离
     *
     * @return 距离
     */
    public double distance()
    {
        return distance;
    }

    /**
     * 将DistanceUnit对象转换为字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString()
    {
        return name();
    }
}