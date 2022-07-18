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
        return mainManager;
    }

    public static CommandManager getCommandManager()
    {
        return commandManager;
    }

    public static EnchantmentManager getEnchantmentManager()
    {
        return enchantmentManager;
    }

    @Deprecated
    public void setBlock(Location location,Material block,String playerName)
    {
        mainManager.setBlock(location,block,playerName);
    }

    @Deprecated
    public <T extends Event> T callEvent(T event)
    {
        return mainManager.callEvent(event);
    }

    @Deprecated
    public int randomInt(int bound)
    {
        return mainManager.randomInt(bound);
    }

    @Deprecated
    public int randomInt()
    {
        return mainManager.randomInt();
    }

    @Deprecated
    public long randomLong()
    {
        return mainManager.randomLong();
    }

    @Deprecated
    public double randomDouble()
    {
        return mainManager.randomDouble();
    }

    @Deprecated
    public void useCommand(CommandSender sender,String cmd,Plugin plugin)
    {
        commandManager.useCommand(sender,cmd,plugin);
    }
}
