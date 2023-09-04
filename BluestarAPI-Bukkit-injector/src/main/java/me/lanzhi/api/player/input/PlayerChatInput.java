package me.lanzhi.api.player.input;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * 用聊天框让玩家输入
 *
 * @param <T> 输入的类型
 */
public final class PlayerChatInput<T>
{

    private static final List<UUID> players=new ArrayList<>();
    private final EnumMap<EndReason,PlayerChatInput<?>> chainAfter;
    private final BiFunction<Player,String,Boolean> onInvalidInput;
    private final BiFunction<Player,String,Boolean> isValidInput;
    private final BiFunction<Player,String,T> setValue;
    private final BiConsumer<Player,T> onFinish;
    private final Consumer<Player> onCancel;
    private final Consumer<Player> onExpire;
    private final Runnable onDisconnect;
    private final Player player;
    private final String invalidInputMessgae;
    private final String sendValueMessage;
    private final String onExpireMessage;
    private final String cancel;
    private final Plugin main;
    private final int expiresAfter;
    private final InputListener listener=new InputListener();
    private boolean started;
    private final boolean repeat;
    private T value;
    private BukkitTask task;
    private EndReason end;


    private PlayerChatInput(@NotNull Plugin plugin,@NotNull Player player,@Nullable T startOn,
                            @Nullable String invalidInputMessgae,@Nullable String sendValueMessage,
                            @NotNull BiFunction<Player,String,Boolean> isValidInput,@NotNull BiFunction<Player,String
            ,T> setValue,@NotNull BiConsumer<Player,T> onFinish,@NotNull Consumer<Player> onCancel,
                            @NotNull String cancel,@NotNull BiFunction<Player,String,Boolean> onInvalidInput,
                            boolean repeat,@Nullable EnumMap<EndReason,PlayerChatInput<?>> chainAfter,
                            int expiresAfter,@NotNull Consumer<Player> onExpire,@Nullable String whenExpireMessage,
                            @NotNull Runnable onDisconnect)
    {
        Objects.requireNonNull(plugin,"main can't be null");
        Objects.requireNonNull(player,"player can't be null");
        Objects.requireNonNull(invalidInputMessgae,"isValidInput can't be null");
        Objects.requireNonNull(sendValueMessage,"isValidInput can't be null");
        Objects.requireNonNull(isValidInput,"isValidInput can't be null");
        Objects.requireNonNull(setValue,"setValue can't be null");
        Objects.requireNonNull(onFinish,"onFinish can't be null");
        Objects.requireNonNull(onFinish,"onCancel can't be null");
        Objects.requireNonNull(onInvalidInput,"onInvalidInput can't be null");
        Objects.requireNonNull(cancel,"cancel can't be null");
        Objects.requireNonNull(onExpire,"onExpire can't be null");
        Objects.requireNonNull(onDisconnect,"onDisconnect can't be null");
        this.main=plugin;
        this.player=player;
        this.invalidInputMessgae=invalidInputMessgae;
        this.sendValueMessage=sendValueMessage;
        this.isValidInput=isValidInput;
        this.setValue=setValue;
        this.onFinish=onFinish;
        this.onCancel=onCancel;
        this.cancel=cancel;
        this.onInvalidInput=onInvalidInput;
        this.value=startOn;
        this.repeat=repeat;
        this.chainAfter=chainAfter;
        this.expiresAfter=expiresAfter;
        this.onExpire=onExpire;
        this.onExpireMessage=whenExpireMessage;
        this.onDisconnect=onDisconnect;

        this.start();
    }

    /**
     * 获取一个新的Builder
     *
     * @param <U> 输入的类型
     * @return Builder
     */
    public static <U> Builder<U> builder()
    {
        return new Builder<>();
    }

    private static void addPlayer(UUID player)
    {
        players.add(player);
    }

    private static void removePlayer(UUID player)
    {
        players.remove(player);
    }

    /**
     * 一个玩家是否正在被一个PlayerChatInput监听(如果是,则不能再次监听)
     *
     * @param player 玩家
     * @return 是否正在被监听
     */
    public static boolean isInputing(UUID player)
    {
        return players.contains(player);
    }


    private void runEventOnMainThread(String message)
    {
        if (message.equalsIgnoreCase(cancel))
        {
            onCancel.accept(player);
            end(EndReason.PLAYER_CANCELLS);
            return;
        }
        if (isValidInput.apply(player,message))
        {
            value=setValue.apply(player,message);
            onFinish.accept(player,value);
            end(EndReason.FINISH);
        }
        else
        {
            if (onInvalidInput.apply(player,message))
            {
                if (invalidInputMessgae!=null)
                {
                    player.sendMessage(invalidInputMessgae);
                }
                if (sendValueMessage!=null&&repeat)
                {
                    player.sendMessage(sendValueMessage);
                }
            }
            if (!repeat)
            {
                onExpire.accept(player);
                end(EndReason.INVALID_INPUT);
            }
        }
    }

    @Nullable
    public T getValue()
    {
        return value;
    }

    private void start()
    {
        if (isInputing(player.getUniqueId()))
        {
            throw new IllegalAccessError("Can't ask for input to a player that is already inputing");
        }
        addPlayer(player.getUniqueId());


        main.getServer().getPluginManager().registerEvents(listener,this.main);


        if (expiresAfter>0)
        {
            task=Bukkit.getScheduler().runTaskLater(main,() ->
            {
                if (!isStarted())
                {
                    return;
                }
                onExpire.accept(player);
                if (onExpireMessage!=null)
                {
                    player.sendMessage(onExpireMessage);
                }
                end(EndReason.RUN_OUT_OF_TIME);
            },expiresAfter);
        }
        if (sendValueMessage!=null)
        {
            player.sendMessage(sendValueMessage);
        }
        started=true;
        end=null;
    }

    private void unregister()
    {

        if (task!=null)
        {
            task.cancel();
        }

        removePlayer(player.getUniqueId());

        HandlerList.unregisterAll(listener);
    }

    /**
     * 关闭这个监听
     *
     * @param reason 关闭的原因
     */
    public void end(EndReason reason)
    {
        started=false;
        end=reason;
        unregister();

        if (chainAfter!=null)
        {
            if (chainAfter.get(end)!=null)

            {
                chainAfter.get(end).start();
            }
        }
    }

    public boolean isStarted()
    {
        return started;
    }


    /**
     * 关闭的原因
     */
    public enum EndReason
    {
        PLAYER_CANCELLS,
        FINISH,
        RUN_OUT_OF_TIME,
        PLAYER_DISCONECTS,
        INVALID_INPUT,
        CUSTOM
    }

    public static class Builder<U>
    {

        private EnumMap<EndReason,PlayerChatInput<?>> chainAfter;
        private BiFunction<Player,String,Boolean> onInvalidInput;
        private BiFunction<Player,String,Boolean> isValidInput;
        private BiFunction<Player,String,U> setValue;
        private BiConsumer<Player,U> onFinish;
        private Consumer<Player> onCancel;
        private Consumer<Player> onExpire;
        private Runnable onDisconnect;

        private String invalidInputMessage;
        private String sendValueMessage;
        private String whenExpire;
        private String cancel;

        private U value;

        private int expiresAfter;
        private boolean repeat;

        private Plugin plugin;

        private Builder()
        {
            invalidInputMessage="That is not a valid input";
            sendValueMessage="Send in the chat the value";
            whenExpire="You ran out of time to answer";
            cancel="cancel";

            onInvalidInput=(p,mes) -> true;
            isValidInput=(p,mes) -> true;
            setValue=(p,mes) -> value;
            onFinish=(p,val) ->
            {
            };
            onCancel=(p) ->
            {
            };
            onExpire=(p) ->
            {
            };
            onDisconnect=() ->
            {
            };

            expiresAfter=-1;
            repeat=true;
        }

        /**
         * 设置当前调用插件
         * @param plugin 当前调用插件
         * @return 这个构造器
         */
        public Builder<U> plugin(@NotNull Plugin plugin)
        {
            this.plugin=plugin;
            return this;
        }

        /**
         * 当输入无效时执行
         *
         * @param onInvalidInput 当输入无效时执行,第一个参数是玩家,第二个参数是玩家输入的信息,返回值是是否继续等待输入
         * @return 这个构造器
         */
        public Builder<U> onInvalidInput(@NotNull BiFunction<Player,String,Boolean> onInvalidInput)
        {
            this.onInvalidInput=onInvalidInput;
            return this;
        }


        /**
         * 当输入有效时执行
         *
         * @param isValidInput 当输入有效时执行,第一个参数是玩家,第二个参数是玩家输入的信息,返回值是是否继续等待输入
         * @return 这个构造器
         */
        public Builder<U> isValidInput(@NotNull BiFunction<Player,String,Boolean> isValidInput)
        {
            this.isValidInput=isValidInput;
            return this;
        }


        /**
         * 将有效的输入转换到对应的值
         *
         * @param setValue
         * @return
         */
        public Builder<U> setValue(@NotNull BiFunction<Player,String,U> setValue)
        {
            this.setValue=setValue;
            return this;
        }


        public Builder<U> onFinish(@NotNull BiConsumer<Player,U> onFinish)
        {
            this.onFinish=onFinish;
            return this;
        }


        public Builder<U> onCancel(@NotNull Consumer<Player> onCancel)
        {
            this.onCancel=onCancel;
            return this;
        }


        public Builder<U> invalidInputMessage(@Nullable String invalidInputMessage)
        {
            this.invalidInputMessage=invalidInputMessage;
            return this;
        }


        public Builder<U> sendValueMessage(@Nullable String sendValueMessage)
        {
            this.sendValueMessage=sendValueMessage;
            return this;
        }


        public Builder<U> toCancel(@NotNull String cancel)
        {
            this.cancel=cancel;
            return this;
        }


        public Builder<U> defaultValue(@Nullable U def)
        {
            this.value=def;
            return this;
        }


        public Builder<U> repeat(boolean repeat)
        {
            this.repeat=repeat;
            return this;
        }


        public Builder<U> chainAfter(@NotNull PlayerChatInput<?> toChain,@NotNull EndReason... after)
        {
            if (this.chainAfter==null)
            {
                chainAfter=new EnumMap<>(EndReason.class);
            }
            for (EndReason cm: after)
            {
                if (cm==EndReason.PLAYER_DISCONECTS)
                {
                    continue;
                }
                this.chainAfter.put(cm,toChain);
            }
            return this;
        }


        public Builder<U> onExpire(@NotNull Consumer<Player> onExpire)
        {
            this.onExpire=onExpire;
            return this;
        }


        public Builder<U> onExpireMessage(@Nullable String message)
        {
            this.whenExpire=message;
            return this;
        }


        public Builder<U> expiresAfter(int ticks)
        {
            if (ticks>0)
            {
                this.expiresAfter=ticks;
            }
            return this;
        }


        public Builder<U> onPlayerDiconnect(@NotNull Runnable onDisconnect)
        {
            this.onDisconnect=onDisconnect;
            return this;
        }


        public PlayerChatInput<U> open(@NotNull Player player)
        {

            Validate.notNull(plugin,"Plugin cannot be null");
            Validate.notNull(player,"Player cannot be null");
            return new PlayerChatInput<>(plugin,
                                         player,
                                         value,
                                         invalidInputMessage,
                                         sendValueMessage,
                                         isValidInput,
                                         setValue,
                                         onFinish,
                                         onCancel,
                                         cancel,
                                         onInvalidInput,
                                         repeat,
                                         chainAfter,
                                         expiresAfter,
                                         onExpire,
                                         whenExpire,
                                         onDisconnect);
        }
    }

    private class InputListener implements Listener
    {
        @EventHandler
        public void onPlayerDisconnect(PlayerQuitEvent e)
        {
            if (e.getPlayer().getUniqueId().equals(player.getUniqueId()))
            {
                if (!isStarted())
                {
                    return;
                }
                onDisconnect.run();
                end(EndReason.PLAYER_DISCONECTS);
            }
        }

        @EventHandler
        public void onPlayerChatEvent(AsyncPlayerChatEvent e)
        {
            if (!player.getUniqueId().equals(e.getPlayer().getUniqueId()))
            {
                return;
            }
            if (!isStarted())
            {
                return;
            }
            e.setCancelled(true);
            Bukkit.getScheduler().runTask(main,() -> runEventOnMainThread(e.getMessage()));
        }

        @EventHandler
        public void onPluginDisable(PluginDisableEvent event)
        {
            if (event.getPlugin()==PlayerChatInput.this.main)
            {
                end(EndReason.CUSTOM);
            }
        }
    }

}