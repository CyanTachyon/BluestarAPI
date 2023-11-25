package me.nullaqua.api.math;

public final class Variable extends Number
{
    private final String name;
    private double vault;

    private Variable(String name,double vault)
    {
        this.name=name;
        this.vault=vault;
    }

    public static Variable create(String name,double vault)
    {
        assert name!=null;
        try
        {
            Double.parseDouble(name);
            throw new AssertionError("变量名称不合法");
        }
        catch (Exception e)
        {
        }
        return new Variable(name,vault);
    }

    public String getName()
    {
        return name;
    }

    public double getVault()
    {
        synchronized (this)
        {
            return vault;
        }
    }

    public Variable setVault(double vault)
    {
        synchronized (this)
        {
            this.vault=vault;
            return this;
        }
    }

    @Override
    public int intValue()
    {
        return (int) doubleValue();
    }

    @Override
    public long longValue()
    {
        return (long) doubleValue();
    }

    @Override
    public float floatValue()
    {
        return (float) doubleValue();
    }

    @Override
    public double doubleValue()
    {
        synchronized (this)
        {
            return (double) vault;
        }
    }

    @Override
    public int hashCode()
    {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof Variable&&((Variable) obj).name.equals(name);
    }

    @Override
    public String toString()
    {
        return "{Variable: "+name+" }";
    }
}
