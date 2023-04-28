package me.lanzhi.api.command;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Objects;

public final class CommandHelper {
    private final CommandHandler executor = new CommandHandler();
    private String permission = null;

    public CommandHelper() {
    }

    public static PluginCommand fastCreateCommand(JavaPlugin plugin, Class<?> c) {
        CommandHelper helper = new CommandHelper();
        helper.add(c);
        var commandName = c.getAnnotation(CommandName.class);
        var aliases = c.getAnnotation(CommandAlias.class);
        if (commandName == null) {
            throw new IllegalArgumentException("Class " +
                    c.getName() +
                    " don't have annotation " +
                    CommandName.class.getName());
        }
        return helper.createCommand(plugin,
                commandName.value(),
                aliases == null ? new String[0] : Arrays.stream(aliases.value())
                        .map(CommandAlia::value)
                        .toArray(String[]::new),
                commandName.permission());
    }

    public static PluginCommand fastCreateCommand(JavaPlugin plugin,Object o)
    {
        if (o instanceof Class)
        {
            return fastCreateCommand(plugin,(Class<?>) o);
        }
        CommandHelper helper=new CommandHelper();
        helper.add(o);
        var commandName = o.getClass().getAnnotation(CommandName.class);
        var aliases = o.getClass().getAnnotation(CommandAlias.class);
        if (commandName == null) {
            throw new IllegalArgumentException("Class " +
                    o.getClass().getName() +
                    " don't have annotation " +
                    CommandName.class.getName());
        }
        return helper.createCommand(plugin,
                commandName.value(),
                aliases == null ? new String[0] : Arrays.stream(aliases.value())
                        .map(CommandAlia::value)
                        .toArray(String[]::new),
                commandName.permission());
    }

    public static CommandExecutor fastCreateCommandExecutor(Class<?> c)
    {
        return new CommandHelper().add(c).toCommandExecutor();
    }

    public CommandExecutor toCommandExecutor()
    {
        return executor;
    }

    public CommandHelper add(Object instance)
    {
        executor.add(instance);
        return this;
    }

    public static CommandExecutor fastCreateCommandExecutor(Object o) {
        return new CommandHelper().add(o).toCommandExecutor();
    }

    public PluginCommand createCommand(JavaPlugin plugin, String name, String[] aliases, String permission) {
        Objects.requireNonNull(plugin);
        Objects.requireNonNull(name);
        if (permission.isEmpty())
            permission = null;
        if (aliases == null)
            aliases = new String[0];
        if (name.isEmpty())
            throw new IllegalArgumentException("Command name can't be empty");
        var command = plugin.getCommand(name);
        if (command == null) {
            command = CommandManager.newPluginCommand(name, plugin);
        }
        if (command == null) {
            throw new IllegalArgumentException("Plugin don't have command " + name + ",and can't create it");
        }
        command.setAliases(Arrays.asList(aliases));
        command.setExecutor(toCommandExecutor());
        command.setPermission(permission);
        return command;
    }

    public TabExecutor toTabExecutor() {
        return executor;
    }

    public CommandExecutor toExecutor() {
        return executor;
    }

    public CommandHelper permission(String permission) {
        this.permission = permission;
        return this;
    }

    public String permission() {
        return permission;
    }
}