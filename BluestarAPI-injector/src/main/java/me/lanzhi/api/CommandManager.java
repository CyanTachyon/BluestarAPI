package me.lanzhi.api;

import me.lanzhi.api.reflect.ConstructorAccessor;
import me.lanzhi.api.reflect.FieldAccessor;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class CommandManager
{
    public static void upData()
    {
        Bluestar.setCommandManager(new CommandManager());
    }

    private final ConstructorAccessor<PluginCommand> constructor;
    private final FieldAccessor commandMap;

    CommandManager()
    {
        constructor=ConstructorAccessor.getConstructor(PluginCommand.class,String.class,Plugin.class);
        commandMap=FieldAccessor.getDeclaredField(Bukkit.getServer().getClass(),"commandMap");
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

    public SimpleCommandMap getCommandMap()
    {
        try
        {
            return (SimpleCommandMap) commandMap.get(Bukkit.getServer());
        }
        catch (Throwable e)
        {
            return null;
        }
    }

    public PluginCommand newPluginCommand(String name,Plugin plugin)
    {
        try
        {
            return constructor.invoke(name,plugin);
        }
        catch (Throwable e)
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

    public boolean registerPluginCommand(String command,Plugin plugin,CommandExecutor executor)
    {
        PluginCommand pluginCommand=newPluginCommand(command,plugin);
        assert pluginCommand!=null;
        pluginCommand.setExecutor(executor);
        return registerPluginCommand(pluginCommand);
    }
}
