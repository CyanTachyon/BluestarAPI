package me.lanzhi.bluestarapi.api;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;

public final class Bluestar
{
    private static BluestarManager mainManager=null;
    private static CommandManager commandManager=null;
    private static EnchantmentManager enchantmentManager=null;

    public static void setCommandManager(CommandManager commandManager)
    {
        if (Bluestar.commandManager!=null)
        {
            return;
        }
        Bluestar.commandManager=commandManager;
    }

    public static void setEnchantmentManager(EnchantmentManager enchantmentManager)
    {
        if (Bluestar.enchantmentManager!=null)
        {
            return;
        }
        Bluestar.enchantmentManager=enchantmentManager;
    }

    public static void setMainManager(BluestarManager mainManager)
    {
        if (Bluestar.mainManager!=null)
        {
            return;
        }
        Bluestar.mainManager=mainManager;
    }

    public static BluestarManager getMainManager()
    {
        if (mainManager==null)
        {
            BluestarManager.upData();
        }
        return mainManager;
    }

    public static CommandManager getCommandManager()
    {
        if (commandManager==null)
        {
            CommandManager.upData();
        }
        return commandManager;
    }

    public static EnchantmentManager getEnchantmentManager()
    {
        if (enchantmentManager==null)
        {
            EnchantmentManager.upData();
        }
        return enchantmentManager;
    }

    @Deprecated
    public static void setBlock(Location location,Material block,String playerName)
    {
        getMainManager().setBlock(location,block,playerName);
    }

    @Deprecated
    public static  <T extends Event> T callEvent(T event)
    {
        return getMainManager().callEvent(event);
    }

    @Deprecated
    public static int randomInt(int bound)
    {
        return getMainManager().randomInt(bound);
    }

    @Deprecated
    public static int randomInt()
    {
        return getMainManager().randomInt();
    }

    @Deprecated
    public static long randomLong()
    {
        return getMainManager().randomLong();
    }

    @Deprecated
    public static double randomDouble()
    {
        return getMainManager().randomDouble();
    }

    @Deprecated
    public static void useCommand(CommandSender sender,String cmd,Plugin plugin)
    {
        getCommandManager().useCommand(sender,cmd,plugin);
    }
}
