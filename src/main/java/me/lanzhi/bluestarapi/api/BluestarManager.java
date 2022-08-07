package me.lanzhi.bluestarapi.api;

import me.lanzhi.bluestarapi.BluestarAPI;
import me.lanzhi.bluestarapi.api.player.InformationGetter;
import me.lanzhi.bluestarapi.api.player.AnvilInformation;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BluestarManager
{
    private final Random random=new Random();

    private final Map<Player,InformationGetter>getters=new HashMap<>();
    private final ReadWriteLock gettersLock=new ReentrantReadWriteLock();

    private CoreProtectAPI coreProtect=null;

    BluestarManager()
    {
    }

    public static void upData()
    {
        Bluestar.setMainManager(new BluestarManager());
    }

    public void CoreLogRemoval(String playerName,Location location,Material type,BlockData data)
    {
        if (coreProtect==null)
        {
            coreProtect=getCoreProtect();
        }
        if (coreProtect!=null)
        {
            coreProtect.logRemoval(playerName,location,type,data);
        }
    }

    public void CoreLogPlacement(String playerName,Location location,Material type,BlockData data)
    {
        if (coreProtect==null)
        {
            coreProtect=getCoreProtect();
        }
        if (coreProtect!=null)
        {
            coreProtect.logPlacement(playerName,location,type,data);
        }
    }

    public void setBlock(Location location,Material block,String playerName)
    {
        Material type=location.getBlock().getType();
        if (type==block)
        {
            return;
        }
        BlockData blockData=location.getBlock().getBlockData();
        location.getBlock().setType(block);
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (coreProtect==null)
                {
                    coreProtect=getCoreProtect();
                }
                if (coreProtect!=null)
                {
                    if (type==Material.AIR)
                    {
                        coreProtect.logPlacement(playerName,location,block,null);
                    }
                    else if (block==Material.AIR)
                    {
                        coreProtect.logRemoval(playerName,location,type,blockData);
                    }
                    else
                    {
                        coreProtect.logRemoval(playerName,location,type,blockData);
                        coreProtect.logPlacement(playerName,location,block,null);
                    }
                }
            }
        }.runTaskAsynchronously(BluestarAPI.thisPlugin);
    }

    public <T extends Event> T callEvent(T event)
    {
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public int randomInt(int bound)
    {
        return random.nextInt(bound);
    }

    public int randomInt()
    {
        return random.nextInt();
    }

    public long randomLong()
    {
        return random.nextLong();
    }

    public double randomDouble()
    {
        return random.nextDouble();
    }

    public boolean addInformationGetter(InformationGetter getter)
    {
        try
        {
            gettersLock.readLock().lock();
            if (getters.containsKey(getter.getPlayer()))
            {
                return false;
            }
        }
        finally
        {
            gettersLock.readLock().unlock();
        }
        try
        {
            gettersLock.writeLock().lock();
            getters.put(getter.getPlayer(),getter);
            getter.getPlayer().spigot().sendMessage(getter.getTips().toArray(new BaseComponent[0]));
            if (getter instanceof AnvilInformation)
            {
                AnvilInformation smith=(AnvilInformation)getter;
                Player player=smith.getPlayer();
                Inventory inventory=Bukkit.createInventory(player,InventoryType.ANVIL,smith.getTitle());
                ItemStack itemStack=smith.getItem();
                if (itemStack==null||itemStack.getType().isAir())
                {
                    itemStack=new ItemStack(Material.PAPER);
                }
                inventory.setItem(0,itemStack);
                smith.setAnvilUI(inventory);
                player.openInventory(inventory);
            }
        }
        finally
        {
            gettersLock.writeLock().unlock();
        }
        return true;
    }

    public boolean removeInformationGetter(InformationGetter getter)
    {
        try
        {
            gettersLock.readLock().lock();
            if (!getters.containsKey(getter.getPlayer()))
            {
                return false;
            }
        }
        finally
        {
            gettersLock.readLock().unlock();
        }
        try
        {
            gettersLock.writeLock().lock();
            getters.remove(getter.getPlayer());
        }
        finally
        {
            gettersLock.writeLock().unlock();
        }
        return true;
    }

    public InformationGetter getInformationGetter(Player player)
    {
        try
        {
            gettersLock.readLock().lock();
            return getters.get(player);
        }
        finally
        {
            gettersLock.readLock().unlock();
        }
    }

    private CoreProtectAPI getCoreProtect()
    {
        Plugin plugin=Bukkit.getServer().getPluginManager().getPlugin("CoreProtect");
        if (!(plugin instanceof CoreProtect))
        {
            return null;
        }
        CoreProtectAPI CoreProtect=((CoreProtect) plugin).getAPI();
        if (!CoreProtect.isEnabled())
        {
            return null;
        }
        if (CoreProtect.APIVersion()<7)
        {
            return null;
        }
        return CoreProtect;
    }
}
