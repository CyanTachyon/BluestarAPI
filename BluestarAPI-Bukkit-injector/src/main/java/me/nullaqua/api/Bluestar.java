package me.nullaqua.api;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;

/**
 * 一点小工具
 */
public final class Bluestar
{
    private Bluestar()
    {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    /**
     * 随机数
     *
     * @param bound 上限
     * @return 随机数
     */
    public static int randomInt(int bound)
    {
        return BluestarUtils.randomInt(bound);
    }

    /**
     * 随机数
     * @return 随机数
     */
    public static int randomInt()
    {
        return BluestarUtils.randomInt();
    }

    /**
     * 随机数
     * @return 随机数
     */
    public static long randomLong()
    {
        return BluestarUtils.randomLong();
    }

    /**
     * 随机数
     * @return 随机数
     */
    public static double randomDouble()
    {
        return BluestarUtils.randomDouble();
    }

    /**
     * 引发事件
     * @return 事件
     */
    public static <T extends Event> T callEvent(T event)
    {
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }
}
