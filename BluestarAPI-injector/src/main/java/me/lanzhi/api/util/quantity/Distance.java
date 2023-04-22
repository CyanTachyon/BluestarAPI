package me.lanzhi.api.util.quantity;

import me.lanzhi.api.util.quantity.unit.DistanceUnit;

public class Distance
{
    private final double distance;

    public Distance(double distance)
    {
        this.distance=distance;
    }

    public Distance(double distance,DistanceUnit unit)
    {
        this.distance=distance*unit.distance();
    }

    public double distance()
    {
        return distance;
    }

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

    public String toString(DistanceUnit unit)
    {
        return toString(unit,2);
    }

    public String toString(DistanceUnit unit,int decimal)
    {
        return String.format("%."+decimal+"f %s",distance(unit),unit);
    }

    public double distance(DistanceUnit unit)
    {
        return distance/unit.distance();
    }
}