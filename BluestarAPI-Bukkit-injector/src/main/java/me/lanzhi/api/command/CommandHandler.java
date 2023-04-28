package me.lanzhi.api.command;

import me.lanzhi.api.util.StringsUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class CommandHandler implements CommandExecutor, TabExecutor {
    private final ArgsMap commandMap = new ArgsMap();
    private final ArgsMap tabMap = new ArgsMap();

    private void add(ArgsMap.Run[] run) {
        for (var r : run) {
            if (r.isTab)
                tabMap.add(r);
            else
                commandMap.add(r);
        }
    }

    void add(Object instance) {
        add(instance, instance.getClass());
    }

    private void add(@NotNull Object instance, @NotNull Class<?> c) {
        SubCommand subCommand = c.getAnnotation(SubCommand.class);
        SubCommands subCommands = c.getAnnotation(SubCommands.class);
        if (subCommand != null)
            add(instance, c, subCommand.value());
        if (subCommands != null)
            for (var subCommand1 : subCommands.value())
                add(instance, c, subCommand1.value());
    }

    private void add(@NotNull Object instance, @NotNull Class<?> c, String head) {
        Objects.requireNonNull(c);
        Objects.requireNonNull(instance);
        for (var method : c.getDeclaredMethods()) {
            ParseCommands parseCommand = method.getAnnotation(ParseCommands.class);
            ParseTabs parseTab = method.getAnnotation(ParseTabs.class);
            ParseCommand parseCommand1 = method.getAnnotation(ParseCommand.class);
            ParseTab parseTab1 = method.getAnnotation(ParseTab.class);
            if (parseCommand != null)
                add(ArgsMap.Run.of(method, parseCommand.value(), instance, head));
            if (parseTab != null)
                add(ArgsMap.Run.of(method, parseTab.value(), instance, head));
            if (parseCommand1 != null)
                add(ArgsMap.Run.of(method, new ParseCommand[]{parseCommand1}, instance, head));
            if (parseTab1 != null)
                add(ArgsMap.Run.of(method, new ParseTab[]{parseTab1}, instance, head));
        }
        for (var field : c.getDeclaredFields()) {
            ParseCommands parseCommand = field.getAnnotation(ParseCommands.class);
            ParseTabs parseTab = field.getAnnotation(ParseTabs.class);
            ParseCommand parseCommand1 = field.getAnnotation(ParseCommand.class);
            ParseTab parseTab1 = field.getAnnotation(ParseTab.class);
            if (parseCommand != null)
                add(ArgsMap.Run.of(field, parseCommand.value(), instance, head));
            if (parseTab != null)
                add(ArgsMap.Run.of(field, parseTab.value(), instance, head));
            if (parseCommand1 != null)
                add(ArgsMap.Run.of(field, new ParseCommand[]{parseCommand1}, instance, head));
            if (parseTab1 != null)
                add(ArgsMap.Run.of(field, new ParseTab[]{parseTab1}, instance, head));
        }
        if (c.getSuperclass() != Object.class && c.getSuperclass() != null)
            add(instance, c.getSuperclass());
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                             @NotNull String[] args) {
        try {
            Object res = commandMap.run(sender, command, label, args);
            if (res instanceof Boolean) return (boolean) res;
            if (res == null) return true;
            StringsUtils.toStrings(res).forEach(sender::sendMessage);
            return true;
        } catch (Throwable e) {
            sender.sendMessage(ChatColor.RED + "Unknown command or has an error when running command.");
            //e.printStackTrace();
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        try {
            Object res = tabMap.run(sender, command, alias, args);
            if (res == null) return Collections.emptyList();
            return StringsUtils.toStrings(res);
        } catch (Throwable e) {
            //e.printStackTrace();
            return Collections.emptyList();
        }
    }
}