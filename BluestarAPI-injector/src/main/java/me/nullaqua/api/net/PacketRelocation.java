package me.nullaqua.api.net;

import me.lucko.jarrelocator.Relocation;

public class PacketRelocation
{

    private final String pattern;
    private final String relocatedPattern;

    public PacketRelocation(String pattern,String relocatedPattern)
    {
        this.pattern=pattern;
        this.relocatedPattern=relocatedPattern;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this==o)
        {
            return true;
        }
        if (!(o instanceof PacketRelocation))
        {
            return false;
        }
        PacketRelocation that=(PacketRelocation) o;
        if (getPattern()!=null?!getPattern().equals(that.getPattern()):that.getPattern()!=null)
        {
            return false;
        }
        return getRelocatedPattern()!=
               null?getRelocatedPattern().equals(that.getRelocatedPattern()):that.getRelocatedPattern()==null;
    }

    public String getPattern()
    {
        return pattern;
    }

    public String getRelocatedPattern()
    {
        return relocatedPattern;
    }

    public Relocation toRelocation()
    {
        return new Relocation(pattern,relocatedPattern);
    }

    @Override
    public int hashCode()
    {
        int result=getPattern()!=null?getPattern().hashCode():0;
        result=31*result+(getRelocatedPattern()!=null?getRelocatedPattern().hashCode():0);
        return result;
    }
}