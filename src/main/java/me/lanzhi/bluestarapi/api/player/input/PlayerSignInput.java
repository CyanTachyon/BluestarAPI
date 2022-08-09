package me.lanzhi.bluestarapi.api.player.input;

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
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PlayerSignInput
{
    private final InputListener action;
    private final List<String> lines;
    private final Plugin plugin;
    private final UUID uuid;
    private PacketAdapter packetListener;
    private LeaveListener listener;
    private Sign sign;

    public PlayerSignInput(InputListener action,List<String> withLines,UUID uuid,Plugin plugin)
    {
        this.lines=withLines;
        this.plugin=plugin;
        this.action=action;
        this.uuid=uuid;
    }

    public void open()
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
        while (!player.getWorld().getBlockAt(x_start,
                                             y_start,
                                             z_start).getType().equals(Material.AIR)&&!player.getWorld().getBlockAt(
                x_start,
                y_start,
                z_start).getType().equals(material))
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


        PacketContainer openSign=ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.OPEN_SIGN_EDITOR);

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

                        PlayerSignInput.this.action.onSignClose(new InputEvent(event.getPlayer(),lines));
                    });
                }
            }
        };
        manager.addPacketListener(this.packetListener);
    }

    private String getLine(PacketEvent event,int line)
    {
        return Bukkit.getVersion().contains("1.8")?((WrappedChatComponent[]) event.getPacket().getChatComponentArrays().read(
                0))[line].getJson().replaceAll("\"",""):((String[]) event.getPacket().getStringArrays().read(0))[line];
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
    }

    public static final class Builder
    {
        private InputListener action=event -> {};
        private List<String> lines=new ArrayList<>(Arrays.asList("","","",""));
        private UUID uuid=null;
        private Plugin plugin;

        public Builder action(InputListener listener)
        {
            if (listener!=null)
            {
                action=listener;
            }
            return this;
        }

        public Builder lines(List<String> list)
        {
            if (list!=null)
            {
                lines=list;
            }
            return this;
        }

        public Builder player(Player player)
        {
            this.uuid=player.getUniqueId();
            return this;
        }

        public Builder player(UUID uuid)
        {
            this.uuid=uuid;
            return this;
        }

        public Builder plugin(Plugin plugin)
        {
            this.plugin=plugin;
            return this;
        }

        public PlayerSignInput build()
        {
            return new PlayerSignInput(action,lines,uuid,plugin);
        }
    }

    public static final class InputEvent
    {
        private final Player player;
        private final List<String> lines;

        private InputEvent(Player player,List<String> lines)
        {
            this.player=player;
            this.lines=lines;
        }

        public Player getPlayer()
        {
            return player;
        }

        public List<String> getLines()
        {
            return lines;
        }
    }

    public static interface InputListener
    {
        void onSignClose(InputEvent event);
    }

}
