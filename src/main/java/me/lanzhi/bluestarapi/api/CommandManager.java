package me.lanzhi.bluestarapi.api;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class CommandManager
{
    public static void upData()
    {
        Bluestar.setCommandManager(new CommandManager());
    }

    private final Constructor<PluginCommand> constructor;

    CommandManager()
    {
        Constructor<PluginCommand> constructor1;
        try
        {
            constructor1=PluginCommand.class.getDeclaredConstructor(String.class,Plugin.class);
            constructor1.setAccessible(true);
        }
        catch (NoSuchMethodException e)
        {
            constructor1=null;
            e.printStackTrace();
        }
        constructor=constructor1;
    }

    public void useCommand(CommandSender sender,String cmd,Plugin plugin)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Bukkit.getServer().dispatchCommand(sender,cmd);
            }
        }.runTask(plugin);
    }

    public CommandMap getCommandMap()
    {
        try
        {
            Field field=Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            return (CommandMap) field.get(Bukkit.getServer());
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public PluginCommand newPluginCommand(String name,Plugin plugin)
    {
        if (constructor==null)
        {
            return null;
        }
        try
        {
            return constructor.newInstance(name,plugin);
        }
        catch (InstantiationException|IllegalAccessException|InvocationTargetException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public boolean registerCommand(String fallbackPrefix,Command command)
    {
        try
        {
            return getCommandMap().register(fallbackPrefix,command);
        }
        catch (Throwable e)
        {
            return false;
        }
    }

    public boolean registerPluginCommand(PluginCommand command)
    {
        return registerCommand(command.getPlugin().getName(),command);
    }
}
