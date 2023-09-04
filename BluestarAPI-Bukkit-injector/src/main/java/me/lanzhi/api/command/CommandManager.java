package me.lanzhi.api.command;

import me.lanzhi.api.reflect.ConstructorAccessor;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class CommandManager
{
    private static final ConstructorAccessor<PluginCommand> constructor;
    private static final SimpleCommandMap commandMap;

    static
    {
        constructor=ConstructorAccessor.getDeclaredConstructor(PluginCommand.class,String.class,Plugin.class);
        var server=Bukkit.getServer();
        SimpleCommandMap cmdMap=null;
        try
        {
            var method=server.getClass().getDeclaredMethod("getCommandMap");
            cmdMap=(SimpleCommandMap) method.invoke(server);
        }
        catch (Throwable ignored)
        {
        }
        commandMap=cmdMap;
    }

    public static void useCommand(CommandSender sender,String cmd,Plugin plugin)
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

    public static boolean registerPluginCommand(String command,Plugin plugin,CommandExecutor executor,String... alias)
    {
        PluginCommand pluginCommand=newPluginCommand(command,plugin);
        assert pluginCommand!=null;
        pluginCommand.setExecutor(executor);
        pluginCommand.setAliases(java.util.Arrays.asList(alias));
        return registerPluginCommand(pluginCommand);
    }

    public static boolean registerPluginCommand(String command,Plugin plugin,CommandExecutor executor)
    {
        PluginCommand pluginCommand=newPluginCommand(command,plugin);
        assert pluginCommand!=null;
        pluginCommand.setExecutor(executor);
        return registerPluginCommand(pluginCommand);
    }

    public static PluginCommand newPluginCommand(String name,Plugin plugin)
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

    public static boolean registerPluginCommand(PluginCommand command)
    {
        JavaPlugin.getProvidingPlugin(CommandManager.class)
                  .getLogger()
                  .info("Register command "+command.getPlugin().getName()+":"+command.getName());
        return registerCommand(command.getPlugin().getName(),command);
    }

    public static boolean registerCommand(String fallbackPrefix,Command command)
    {
        command.unregister(getCommandMap());
        return commandMap.register(fallbackPrefix,command);
    }

    public static SimpleCommandMap getCommandMap()
    {
        return commandMap;
    }

    public static boolean registerPluginCommand(JavaPlugin plugin,Class<?> clazz)
    {
        return registerPluginCommand(createCommand(plugin,clazz));
    }

    public static PluginCommand createCommand(JavaPlugin plugin,Class<?> clazz)
    {
        return CommandHelper.fastCreateCommand(plugin,clazz);
    }

    public static boolean registerPluginCommand(JavaPlugin plugin,Object object)
    {
        return registerPluginCommand(createCommand(plugin,object));
    }

    public static PluginCommand createCommand(JavaPlugin plugin,Object object)
    {
        return CommandHelper.fastCreateCommand(plugin,object);
    }
}
