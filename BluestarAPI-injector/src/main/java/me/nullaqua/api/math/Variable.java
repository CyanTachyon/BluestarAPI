package me.nullaqua.api.math;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class Variable extends Number
{
    private final String name;
    private Number vault;

    private Variable(String name,Number vault)
    {
        this.name=name;
        this.vault=vault;
        check();
    }

    private void check()
    {
        var var=vault();
        while (var instanceof Variable)
        {
            if (var==this)
            {
                throw new AssertionError("变量循环引用");
            }
            var=((Variable) var).vault();
        }
    }

    public static Variable create(String name,Number vault)
    {
        try
        {
            Double.parseDouble(name);
            throw new AssertionError("变量名称不合法");
        }
        catch (Exception ignored)
        {
        }
        return new Variable(name,vault);
    }

    public String getName()
    {
        return name;
    }

    @NotNull
    public Number vault()
    {
        synchronized (this)
        {
            return vault;
        }
    }

    public Variable vault(Number vault)
    {
        if (Objects.nonNull(vault))
        {
            this.vault=vault;
        }
        return this;
    }

    @Override
    public int intValue()
    {
        return vault().intValue();
    }

    @Override
    public long longValue()
    {
        return vault().longValue();
    }

    @Override
    public float floatValue()
    {
        return vault().floatValue();
    }

    @Override
    public double doubleValue()
    {
        return vault().doubleValue();
    }

    @Override
    public int hashCode()
    {
        return name.hashCode();
    }

    @Override
    public String toString()
    {
        return "{Variable: "+name+" }";
    }
}
