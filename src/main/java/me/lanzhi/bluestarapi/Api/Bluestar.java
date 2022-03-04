package me.lanzhi.bluestarapi.Api;

import me.lanzhi.bluestarapi.BluestarAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import net.coreprotect.CoreProtectAPI;
import net.coreprotect.CoreProtect;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Bluestar
{
    public static void useCommand(CommandSender sender, String cmd,Plugin plugin)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Bukkit.getServer().dispatchCommand(sender,cmd);
            }
        }.runTask(plugin);
    }
    public static void setBlock(Location location,Material block,String playerName)
    {
        if(location.getBlock().getType() == block)
        {
            return;
        }
        Material type = location.getBlock().getType();
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
                if(coreProtect!=null)
                {
                    if(type==Material.AIR&&block!=Material.AIR)
                    {
                        coreProtect.logPlacement(playerName,location,block,null);
                    }
                    else if(type!=Material.AIR&&block==Material.AIR)
                    {
                        coreProtect.logRemoval(playerName,location,type,null);
                    }
                    else if(type!=Material.AIR)
                    {
                        coreProtect.logRemoval(playerName,location,type,null);
                        coreProtect.logPlacement(playerName,location,block,null);
                    }
                }
            }
        }.runTaskAsynchronously(BluestarAPI.thisPlugin);
    }
    private static CoreProtectAPI coreProtect=null;
    private static CoreProtectAPI getCoreProtect()
    {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("CoreProtect");
        if(!(plugin instanceof CoreProtect))
        {
            return null;
        }
        CoreProtectAPI CoreProtect = ((CoreProtect) plugin).getAPI();
        if (!CoreProtect.isEnabled())
        {
            return null;
        }
        if (CoreProtect.APIVersion() < 7)
        {
            return null;
        }
        return CoreProtect;
    }
}
