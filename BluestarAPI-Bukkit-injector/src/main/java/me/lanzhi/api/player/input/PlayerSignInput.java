package me.lanzhi.api.player.input;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 用告示牌让玩家输入(弹出告示牌输入框)
 * 需要ProtocolLib
 */
public final class PlayerSignInput
{
    private final BiConsumer<Player, List<String>> action;
    private final List<String> lines;
    private final Plugin plugin;
    private final UUID uuid;
    private PacketAdapter packetListener;
    private LeaveListener listener;
    private Sign sign;

    private PlayerSignInput(BiConsumer<Player,List<String>> action,List<String> withLines,UUID uuid,Plugin plugin)
    {
        this.lines=withLines;
        this.plugin=plugin;
        this.action=action;
        this.uuid=uuid;

        this.open();
    }

    /**
     * 获取一个新的Builder
     *
     * @return
     */
    public static Builder builder()
    {
        return new Builder();
    }

    private void registerSignUpdateListener()
    {
        final ProtocolManager manager=ProtocolLibrary.getProtocolManager();
        this.packetListener=new PacketAdapter(plugin,PacketType.Play.Client.UPDATE_SIGN)
        {
            @Override
            public void onPacketReceiving(PacketEvent event)
            {
                if (event.getPlayer().getUniqueId().equals(PlayerSignInput.this.uuid))
                {
                    List<String> lines=Stream.of(0,1,2,3).map(line->getLine(event,line)).collect(Collectors.toList());

                    Bukkit.getScheduler().runTask(plugin,()->
                    {
                        manager.removePacketListener(this);

                        HandlerList.unregisterAll(PlayerSignInput.this.listener);

                        PlayerSignInput.this.sign.getBlock().setType(Material.AIR);

                        PlayerSignInput.this.action.accept(event.getPlayer(),lines);
                    });
                }
            }
        };
        manager.addPacketListener(this.packetListener);
    }

    private String getLine(PacketEvent event,int line)
    {
        return Bukkit.getVersion().contains("1.8")?((WrappedChatComponent[]) event.getPacket()
                                                                                  .getChatComponentArrays()
                                                                                  .read(0))[line].getJson()
                                                                                                 .replaceAll("\"",
                                                                                                             ""):((String[]) event.getPacket()
                                                                                                                                  .getStringArrays()
                                                                                                                                  .read(0))[line];
    }

    private void open()
    {
        Plugin plugin=Bukkit.getPluginManager().getPlugin("ProtocolLib");
        if (plugin==null||!plugin.isEnabled())
        {
            return;
        }

        Player player=Bukkit.getPlayer(uuid);

        if (player==null)
        {
            return;
        }

        this.listener=new LeaveListener();

        int x_start=player.getLocation().getBlockX();

        int y_start=255;

        int z_start=player.getLocation().getBlockZ();

        Material material=Material.getMaterial("WALL_SIGN");
        if (material==null)
        {
            material=Material.OAK_WALL_SIGN;
        }
        while (!player.getWorld().getBlockAt(x_start,y_start,z_start).getType().equals(Material.AIR)&&!player.getWorld()
                                                                                                             .getBlockAt(
                                                                                                                     x_start,
                                                                                                                     y_start,
                                                                                                                     z_start)
                                                                                                             .getType()
                                                                                                             .equals(material))
        {
            y_start--;
            if (y_start==1)
            {
                return;
            }
        }
        player.getWorld().getBlockAt(x_start,y_start,z_start).setType(material);

        this.sign=(Sign) player.getWorld().getBlockAt(x_start,y_start,z_start).getState();

        int i=0;
        for (String line: lines)
        {
            this.sign.setLine(i,line);
            i++;
        }

        this.sign.update(false,false);


        PacketContainer openSign=ProtocolLibrary.getProtocolManager()
                                                .createPacket(PacketType.Play.Server.OPEN_SIGN_EDITOR);

        BlockPosition position=new BlockPosition(x_start,y_start,z_start);

        openSign.getBlockPositionModifier().write(0,position);

        Bukkit.getScheduler().runTaskLater(plugin,()->
        {
            try
            {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player,openSign);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        },3L);

        Bukkit.getPluginManager().registerEvents(this.listener,plugin);
        registerSignUpdateListener();
    }

    /**
     * 用于构建PlayerSignInput
     */
    public static final class Builder
    {
        private BiConsumer<Player, List<String>> action=(player,list)->
        {
        };
        private List<String> lines=new ArrayList<>(Arrays.asList("","","",""));
        private Plugin plugin;

        private Builder()
        {
        }

        /**
         * 设置输入完成后的回调
         * @param listener 回调
         * @return Builder
         */
        public Builder action(BiConsumer<Player, List<String>> listener)
        {
            if (listener!=null)
            {
                action=listener;
            }
            return this;
        }

        /**
         * 设置初始输入框的内容
         * @param list 内容
         * @return Builder
         */
        public Builder lines(List<String> list)
        {
            if (list!=null)
            {
                lines=list;
            }
            return this;
        }

        /**
         * 设置当前调用的插件
         * @param plugin 插件
         * @return Builder
         */
        public Builder plugin(Plugin plugin)
        {
            this.plugin=plugin;
            return this;
        }

        /**
         * 构建PlayerSignInput,即向一个玩家打开一个输入框
         * @param player 玩家
         * @return PlayerSignInput
         */
        public PlayerSignInput open(@NotNull Player player)
        {
            return new PlayerSignInput(action,lines,player.getUniqueId(),plugin);
        }
    }

    private class LeaveListener implements Listener
    {
        @EventHandler
        public void onLeave(PlayerQuitEvent e)
        {
            if (e.getPlayer().getUniqueId().equals(PlayerSignInput.this.uuid))
            {
                ProtocolLibrary.getProtocolManager().removePacketListener(PlayerSignInput.this.packetListener);
                HandlerList.unregisterAll(this);
                PlayerSignInput.this.sign.getBlock().setType(Material.AIR);
            }
        }

        @EventHandler
        public void onPluginDisable(PluginDisableEvent event)
        {
            if (event.getPlugin()==PlayerSignInput.this.plugin)
            {
                ProtocolLibrary.getProtocolManager().removePacketListener(PlayerSignInput.this.packetListener);
                HandlerList.unregisterAll(this);
                PlayerSignInput.this.sign.getBlock().setType(Material.AIR);
            }
        }
    }
}
