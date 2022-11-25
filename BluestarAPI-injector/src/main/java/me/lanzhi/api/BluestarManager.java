package me.lanzhi.api;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public final class BluestarManager
{
    private final Random random=new Random();

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
        }.runTaskAsynchronously(JavaPlugin.getProvidingPlugin(BluestarManager.class));
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
