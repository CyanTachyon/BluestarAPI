package me.lanzhi.api.command;

import me.lanzhi.api.Bluestar;
import me.lanzhi.api.reflect.ConstructorAccessor;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class CommandManager
{
    private final ConstructorAccessor<PluginCommand> constructor;
    private final SimpleCommandMap commandMap;

    CommandManager()
    {
        constructor=ConstructorAccessor.getDeclaredConstructor(PluginCommand.class,String.class,Plugin.class);
        var server=Bukkit.getServer();
        SimpleCommandMap commandMap=null;
        try
        {
            var method=server.getClass().getDeclaredMethod("getCommandMap");
            commandMap=(SimpleCommandMap) method.invoke(server);
        }
        catch (Throwable ignored)
        {
        }
        this.commandMap=commandMap;
    }

    public static void update()
    {
        Bluestar.setCommandManager(new CommandManager());
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

    public boolean registerPluginCommand(String command,Plugin plugin,CommandExecutor executor,String... alias)
    {
        PluginCommand pluginCommand=newPluginCommand(command,plugin);
        assert pluginCommand!=null;
        pluginCommand.setExecutor(executor);
        pluginCommand.setAliases(java.util.Arrays.asList(alias));
        return registerPluginCommand(pluginCommand);
    }

    public boolean registerPluginCommand(String command,Plugin plugin,CommandExecutor executor)
    {
        PluginCommand pluginCommand=newPluginCommand(command,plugin);
        assert pluginCommand!=null;
        pluginCommand.setExecutor(executor);
        return registerPluginCommand(pluginCommand);
    }

    public PluginCommand newPluginCommand(String name,Plugin plugin)
    {
        try
        {
            return constructor.invoke(name,plugin);
        }
        catch (Throwable e)
        {
            return null;
        }
    }

    public boolean registerPluginCommand(PluginCommand command)
    {
        return registerCommand(command.getPlugin().getName(),command);
    }

    public boolean registerCommand(String fallbackPrefix,Command command)
    {
        return getCommandMap().register(fallbackPrefix,command);
    }

    public SimpleCommandMap getCommandMap()
    {
        return commandMap;
    }

    public boolean registerPluginCommand(JavaPlugin plugin,Class<?> clazz)
    {
        return registerPluginCommand(createCommand(plugin,clazz));
    }

    public PluginCommand createCommand(JavaPlugin plugin,Class<?> clazz)
    {
        return CommandHelper.fastCreateCommand(plugin,clazz);
    }

    public boolean registerPluginCommand(JavaPlugin plugin,Object object)
    {
        return registerPluginCommand(createCommand(plugin,object));
    }

    public PluginCommand createCommand(JavaPlugin plugin,Object object)
    {
        return CommandHelper.fastCreateCommand(plugin,object);
    }
}
