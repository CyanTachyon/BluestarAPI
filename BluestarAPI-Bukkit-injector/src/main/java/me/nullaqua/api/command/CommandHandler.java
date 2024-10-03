package me.nullaqua.api.command;

import me.nullaqua.api.util.StringsUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class CommandHandler implements CommandExecutor, TabExecutor
{
    private final ArgsMap commandMap=new ArgsMap();
    private final ArgsMap tabMap=new ArgsMap();

    private void add(ArgsMap.Run[] run)
    {
        for (var r: run)
        {
            if (r.isTab)
            {
                tabMap.add(r);
            }
            else
            {
                commandMap.add(r);
            }
        }
    }

    void add(Object instance)
    {
        add(instance,instance.getClass());
    }

    private void add(@NotNull Object instance,@NotNull Class<?> c)
    {
        SubCommand[] subCommands=c.getAnnotationsByType(SubCommand.class);
        for (var subCommand1: subCommands)
        {
            add(instance,c,subCommand1.value());
        }
        if (subCommands.length==0)
        {
            add(instance,c,"");
        }
    }

    private void add(@NotNull Object instance,@NotNull Class<?> c,String head)
    {
        Objects.requireNonNull(c);
        Objects.requireNonNull(instance);
        for (var method: c.getDeclaredMethods())
        {
            ParseCommand[] parseCommand=method.getDeclaredAnnotationsByType(ParseCommand.class);
            ParseTab[] parseTab=method.getDeclaredAnnotationsByType(ParseTab.class);
            if (parseCommand!=null) add(ArgsMap.Run.of(method,parseCommand,instance,head));
            if (parseTab!=null) add(ArgsMap.Run.of(method,parseTab,instance,head));
        }
        for (var field: c.getDeclaredFields())
        {
            ParseCommand[] parseCommand=field.getDeclaredAnnotationsByType(ParseCommand.class);
            ParseTab[] parseTab=field.getDeclaredAnnotationsByType(ParseTab.class);
            if (parseCommand!=null) add(ArgsMap.Run.of(field,parseCommand,instance,head));
            if (parseTab!=null) add(ArgsMap.Run.of(field,parseTab,instance,head));
        }
        if (c.getSuperclass()!=Object.class&&c.getSuperclass()!=null) add(instance,c.getSuperclass());
    }

    public boolean onCommand(@NotNull CommandSender sender,@NotNull Command command,@NotNull String label,
                             @NotNull String[] args)
    {
        try
        {
            Object res=commandMap.run(sender,command,label,args);
            if (res instanceof Boolean) return (boolean) res;
            if (res==null) return true;
            StringsUtils.toStrings(res).forEach(sender::sendMessage);
            return true;
        }
        catch (ArgsMap.CommandException e)
        {
            sender.sendMessage(ChatColor.RED+"Unknown command or has an error when running command.");
            return e.toCommandExceptions().print();
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,@NotNull Command command,@NotNull String alias,
                                      @NotNull String[] args)
    {
        try
        {
            Object res=tabMap.run(sender,command,alias,args);
            if (res==null) return Collections.emptyList();
            return StringsUtils.toStrings(res);
        }
        catch (Throwable e)
        {
            //e.printStackTrace();
            return Collections.emptyList();
        }
    }
}