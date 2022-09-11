package me.lanzhi.api;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class AbstractQQCommandSender implements ConsoleCommandSender
{
    private final Spigot spigot;
    private final Plugin plugin;
    private final List<String>messages=new ArrayList<>();

    public AbstractQQCommandSender(Plugin plugin)
    {
        this.plugin=plugin;
        spigot=new Spigot();
    }

    public final void runCommand(String command)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Bukkit.dispatchCommand(AbstractQQCommandSender.this,command);
                pushMessages(messages);
            }
        }.runTask(plugin);
    }

    public abstract void pushMessages(List<String> messages);

    @Override
    public final void sendMessage(@NotNull String message)
    {
        synchronized (messages)
        {
            messages.add(message);
        }
    }

    @Override
    public final void sendMessage(@NotNull String[] messages)
    {
        for (String message: messages)
        {
            sendMessage(message);
        }
    }

    @Override
    public final void sendMessage(@Nullable UUID sender,@NotNull String message)
    {
        sendMessage(message);
    }

    @Override
    public final void sendMessage(@Nullable UUID sender,@NotNull String[] messages)
    {
        for (String message: messages)
        {
            sendMessage(message);
        }
    }

    @NotNull
    @Override
    public final Server getServer()
    {
        return Bukkit.getConsoleSender().getServer();
    }

    @NotNull
    @Override
    public final String getName()
    {
        return "QQConsole";
    }

    @NotNull
    @Override
    public final Spigot spigot()
    {
        return spigot;
    }

    @Override
    public final boolean isConversing()
    {
        throw new UnsupportedOperationException("插件试图对QQ管理员进行不可行的操作");
    }

    @Override
    public final void acceptConversationInput(@NotNull String input)
    {
        throw new UnsupportedOperationException("插件试图对QQ管理员进行不可行的操作");
    }

    @Override
    public final boolean beginConversation(@NotNull Conversation conversation)
    {
        throw new UnsupportedOperationException("插件试图对QQ管理员进行不可行的操作");
    }

    @Override
    public final void abandonConversation(@NotNull Conversation conversation)
    {
        throw new UnsupportedOperationException("插件试图对QQ管理员进行不可行的操作");
    }

    @Override
    public final void abandonConversation(@NotNull Conversation conversation,@NotNull ConversationAbandonedEvent details)
    {
        throw new UnsupportedOperationException("插件试图对QQ管理员进行不可行的操作");
    }

    @Override
    public final void sendRawMessage(@NotNull String message)
    {
        sendMessage(message);
    }

    @Override
    public final void sendRawMessage(@Nullable UUID sender,@NotNull String message)
    {
        sendRawMessage(message);
    }

    @Override
    public final boolean isPermissionSet(@NotNull String name)
    {
        return Bukkit.getConsoleSender().isPermissionSet(name);
    }

    @Override
    public final boolean isPermissionSet(@NotNull Permission perm)
    {
        return Bukkit.getConsoleSender().isPermissionSet(perm);
    }

    @Override
    public final boolean hasPermission(@NotNull String name)
    {
        return Bukkit.getConsoleSender().hasPermission(name);
    }

    @Override
    public final boolean hasPermission(@NotNull Permission perm)
    {
        return Bukkit.getConsoleSender().hasPermission(perm);
    }

    @NotNull
    @Override
    public final PermissionAttachment addAttachment(@NotNull Plugin plugin,@NotNull String name,boolean value)
    {
        throw new UnsupportedOperationException("插件试图对QQ管理员进行不可行的操作");
    }

    @NotNull
    @Override
    public final PermissionAttachment addAttachment(@NotNull Plugin plugin)
    {
        throw new UnsupportedOperationException("插件试图对QQ管理员进行不可行的操作");
    }

    @Nullable
    @Override
    public final PermissionAttachment addAttachment(@NotNull Plugin plugin,@NotNull String name,boolean value,int ticks)
    {
        throw new UnsupportedOperationException("插件试图对QQ管理员进行不可行的操作");
    }

    @Nullable
    @Override
    public final PermissionAttachment addAttachment(@NotNull Plugin plugin,int ticks)
    {
        throw new UnsupportedOperationException("插件试图对QQ管理员进行不可行的操作");
    }

    @Override
    public final void removeAttachment(@NotNull PermissionAttachment attachment)
    {
        throw new UnsupportedOperationException("插件试图对QQ管理员进行不可行的操作");
    }

    @Override
    public final void recalculatePermissions()
    {
        throw new UnsupportedOperationException("插件试图对QQ管理员进行不可行的操作");
    }

    @NotNull
    @Override
    public final Set<PermissionAttachmentInfo> getEffectivePermissions()
    {
        throw new UnsupportedOperationException("插件试图对QQ管理员进行不可行的操作");
    }

    @Override
    public final boolean isOp()
    {
        return Bukkit.getConsoleSender().isOp();
    }

    @Override
    public final void setOp(boolean value)
    {
        throw new UnsupportedOperationException("插件试图对QQ管理员进行不可行的操作");
    }

    public final class Spigot extends CommandSender.Spigot
    {
        private Spigot()
        {
            super();
        }

        @Override
        public void sendMessage(@NotNull BaseComponent component)
        {
            AbstractQQCommandSender.this.sendMessage(component.toPlainText());
        }

        @Override
        public void sendMessage(@NotNull BaseComponent... components)
        {
            for (BaseComponent component: components)
            {
                AbstractQQCommandSender.this.sendMessage(component.toPlainText());
            }
        }

        @Override
        public void sendMessage(@Nullable UUID sender,@NotNull BaseComponent component)
        {
            AbstractQQCommandSender.this.sendMessage(component.toPlainText());
        }

        @Override
        public void sendMessage(@Nullable UUID sender,@NotNull BaseComponent... components)
        {
            for (BaseComponent component: components)
            {
                AbstractQQCommandSender.this.sendMessage(component.toPlainText());
            }
        }
    }
}
