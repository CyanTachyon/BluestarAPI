package me.nullaqua.api.util.quantity;

import me.nullaqua.api.util.quantity.unit.DistanceUnit;

/**
 * 表示一个距离测量。
 */
public class Distance
{
    private final double distance;

    /**
     * 构造一个具有给定距离值的Distance对象。
     *
     * @param distance 距离值
     */
    public Distance(double distance)
    {
        this.distance=distance;
    }

    /**
     * 构造一个具有给定距离值和单位的Distance对象。
     *
     * @param distance 距离值
     * @param unit     距离单位
     */
    public Distance(double distance,DistanceUnit unit)
    {
        this.distance=distance*unit.distance();
    }

    /**
     * 返回距离值。
     *
     * @return 距离值
     */
    public double distance()
    {
        return distance;
    }

    /**
     * 返回距离的字符串表示。
     * 如果距离大于KM，则会转换为KM单位。
     * 如果距离大于M，则会转换为M单位。
     * 如果距离大于DM，则会转换为DM单位。
     * 如果距离大于CM，则会转换为CM单位。
     * 如果距离大于MM，则会转换为MM单位。
     * 如果距离大于UM，则会转换为UM单位。
     * 否则，会转换为NM单位。
     *
     * @return 距离的字符串表示
     */
    @Override
    public String toString()
    {
        if (distance>DistanceUnit.KM.distance())
        {
            return toString(DistanceUnit.KM);
        }
        else if (distance>DistanceUnit.M.distance())
        {
            return toString(DistanceUnit.M);
        }
        else if (distance>DistanceUnit.DM.distance())
        {
            return toString(DistanceUnit.DM);
        }
        else if (distance>DistanceUnit.CM.distance())
        {
            return toString(DistanceUnit.CM);
        }
        else if (distance>DistanceUnit.MM.distance())
        {
            return toString(DistanceUnit.MM);
        }
        else if (distance>DistanceUnit.UM.distance())
        {
            return toString(DistanceUnit.UM);
        }
        else
        {
            return toString(DistanceUnit.NM);
        }
    }

    /**
     * 返回距离的字符串表示，使用指定的单位。
     *
     * @param unit 距离单位
     * @return 距离的字符串表示
     */
    public String toString(DistanceUnit unit)
    {
        return toString(unit,2);
    }

    /**
     * 返回距离的字符串表示，使用指定的单位和小数位数。
     *
     * @param unit    距离单位
     * @param decimal 小数位数
     * @return 距离的字符串表示
     */
    public String toString(DistanceUnit unit,int decimal)
    {
        return String.format("%."+decimal+"f %s",distance(unit),unit);
    }

    /**
     * 将距离转换为指定单位的距离。
     *
     * @param unit 距离单位
     * @return 指定单位的距离
     */
    public double distance(DistanceUnit unit)
    {
        return distance/unit.distance();
    }

    public Distance add(Distance distance)
    {
        return new Distance(this.distance+distance.distance);
    }

    public Distance sub(Distance distance)
    {
        return new Distance(this.distance-distance.distance);
    }
}