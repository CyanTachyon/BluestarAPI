package me.lanzhi.bluestarapi.api.net.verification;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author Lanzhi
 */
public final class VerificationCommand implements CommandExecutor, TabExecutor
{
    private final VerificationPlugin plugin;

    public VerificationCommand(VerificationPlugin plugin)
    {
        this.plugin=plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,@NotNull Command command,@NotNull String label,@NotNull String[] args)
    {
        if (args.length<=0)
        {
            return true;
        }
        switch (args[0])
        {
            case "get":
            {
                sender.sendMessage("当前秘钥为"+plugin.getKey());
                sender.sendMessage("当前授权状况为:"+(plugin.isSuccess()?"已授权":"未授权"));
                return true;
            }
            case "set":
            {
                if (args.length<=1)
                {
                    sender.sendMessage("/"+plugin.getName()+"key set <key>");
                    return true;
                }
                try
                {
                    plugin.setKey(UUID.fromString(args[1]));
                    sender.sendMessage("秘钥已设置为"+args[1]);
                }
                catch (Exception e)
                {
                    sender.sendMessage("秘钥格式错误");
                }
                return true;
            }
            default:
            {
                sender.sendMessage("未知命令");
                return true;
            }
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,@NotNull Command command,@NotNull String alias,@NotNull String[] args)
    {
        if (args.length<=1)
        {
            return Arrays.asList("get","set");
        }
        return null;
    }
}
