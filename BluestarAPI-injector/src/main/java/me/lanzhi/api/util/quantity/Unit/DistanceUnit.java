package me.lanzhi.api.util.quantity.Unit;

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

    DistanceUnit(double distance)
    {
        this.distance=distance;
    }

    public double distance()
    {
        return distance;
    }

    @Override
    public String toString()
    {
        return name();
    }
}