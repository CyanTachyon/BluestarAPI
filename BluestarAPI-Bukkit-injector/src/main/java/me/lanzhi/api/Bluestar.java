package me.lanzhi.api;

import me.lanzhi.api.command.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;

public final class Bluestar
{
    private Bluestar()
    {
    }

    @Deprecated
    public static void useCommand(CommandSender sender, String cmd, Plugin plugin) {
        CommandManager.useCommand(sender, cmd, plugin);
    }

    public static int randomInt(int bound)
    {
        return BluestarUtils.randomInt(bound);
    }

    public static int randomInt()
    {
        return BluestarUtils.randomInt();
    }

    public static long randomLong()
    {
        return BluestarUtils.randomLong();
    }

    public static double randomDouble()
    {
        return BluestarUtils.randomDouble();
    }

    public static <T extends Event> T callEvent(T event) {
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }
}
