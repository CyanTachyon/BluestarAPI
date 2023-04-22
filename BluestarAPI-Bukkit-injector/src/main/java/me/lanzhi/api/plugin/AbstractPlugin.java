package me.lanzhi.api.plugin;

import me.lanzhi.api.net.MavenLoader;
import me.lanzhi.api.util.LoggerUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;

public abstract class AbstractPlugin extends JavaPlugin
{
    private final MavenLoader.MavenLibrary[] libraries;
    private LoggerUtils logger;

    /**
     * @param args 依赖的项目
     */
    public AbstractPlugin(String... args)
    {
        super();
        MavenLoader.MavenLibrary[] libraries=new MavenLoader.MavenLibrary[args.length];
        int i=0;
        for (String arg: args)
        {
            String[] split=arg.split(":");
            if (split.length!=2&&split.length!=3)
                throw new IllegalArgumentException("Invalid argument "+arg);
            libraries[i++]=new MavenLoader.MavenLibrary(split[0],split[1],split.length==3?split[2]:"latest");
        }
        this.libraries=libraries;
    }

    public AbstractPlugin(MavenLoader.MavenLibrary... libraries)
    {
        super();
        this.libraries=Arrays.copyOf(libraries,libraries.length);
    }

    @Override
    public final void onLoad()
    {
        loadLibs(libraries);
        onLoaded();
    }

    private void loadLibs(MavenLoader.MavenLibrary... libraries)
    {
        for (MavenLoader.MavenLibrary library: libraries)
        {
            loadLib(library);
        }
    }

    public void onLoaded()
    {
    }

    private void loadLib(MavenLoader.MavenLibrary library)
    {
        File libFolder=new File(getDataFolder(),"libs");
        logger().info("Loading library "+library);
        try
        {
            MavenLoader.loadLibrary(library,libFolder);
        }
        catch (Throwable e)
        {
            logger().severe("Failed to load library "+library);
            throw new RuntimeException(e);
        }
    }

    public LoggerUtils logger()
    {
        if (logger==null)
            return logger=new LoggerUtils(getLogger());
        return logger;
    }
}