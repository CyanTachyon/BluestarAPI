package me.lanzhi.api.plugin;

import me.lanzhi.api.net.MavenLoader;

public abstract class KotlinPlugin extends AbstractPlugin
{
    public KotlinPlugin()
    {
        this("lastest");
    }

    public KotlinPlugin(String kotlinVer)
    {
        this(kotlinVer,new String[0]);
    }

    public KotlinPlugin(String kotlinVer,String... args)
    {
        super(join("org.jetbrains.kotlin:kotlin-stdlib-jdk8:"+kotlinVer,args));
    }

    @SafeVarargs
    private static <T> T[] join(T a,T... b)
    {
        T[] c=(T[]) new Object[b.length+1];
        c[0]=a;
        System.arraycopy(b,0,c,1,b.length);
        return c;
    }

    public KotlinPlugin(String kotlinVer,MavenLoader.MavenLibrary... libraries)
    {
        super(join(MavenLoader.of("org.jetbrains.kotlin","kotlin-stdlib-jdk8",kotlinVer),libraries));
    }

    public KotlinPlugin(MavenLoader.MavenLibrary... libraries)
    {
        super(join(MavenLoader.of("org.jetbrains.kotlin","kotlin-stdlib-jdk8","latest"),libraries));
    }
}