package me.lanzhi.bluestarapi.Api;

import me.lanzhi.bluestarapi.BluestarAPI;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Random;

final public class Bluestar
{
    private static final Random random=new Random();
    private static final Field acceptRegisterEnchantment;
    private static final Map<NamespacedKey, Enchantment> enchantmentByKey;
    private static final Map<String, Enchantment> enchantmentByName;
    private static CoreProtectAPI coreProtect=null;

    static
    {
        try
        {
            acceptRegisterEnchantment=Enchantment.class.getDeclaredField("acceptingNew");
            Field byKey=Enchantment.class.getDeclaredField("byKey");
            byKey.setAccessible(true);
            enchantmentByKey=(Map<NamespacedKey, Enchantment>) byKey.get(null);
            byKey.setAccessible(false);

            Field byName=Enchantment.class.getDeclaredField("byName");
            byName.setAccessible(true);
            enchantmentByName=(Map<String, Enchantment>) byName.get(null);
            byName.setAccessible(false);
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void useCommand(CommandSender sender,String cmd,Plugin plugin)
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

    public static <T extends Event> T callEvent(T event)
    {
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static int randomInt(int bound)
    {
        return random.nextInt(bound);
    }

    public static int randomInt()
    {
        return random.nextInt();
    }

    public static long randomLong()
    {
        return random.nextLong();
    }

    public static double randomDouble()
    {
        return random.nextDouble();
    }

    private static CoreProtectAPI getCoreProtect()
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

    public static boolean openEnchantmentRegistrations()
    {
        if (acceptRegisterEnchantment==null)
        {
            return false;
        }
        try
        {
            acceptRegisterEnchantment.setAccessible(true);
            acceptRegisterEnchantment.set(null,true);
            acceptRegisterEnchantment.setAccessible(false);
            return true;
        }
        catch (Throwable e)
        {
            return false;
        }
    }

    public static boolean closeEnchantmentRegistrations()
    {
        try
        {
            Enchantment.stopAcceptingRegistrations();
            return true;
        }
        catch (Throwable e)
        {
            return false;
        }
    }

    public static boolean registerEnchantment(Enchantment enchantment)
    {
        if (!openEnchantmentRegistrations())
        {
            return false;
        }
        Enchantment.registerEnchantment(enchantment);
        closeEnchantmentRegistrations();
        return true;
    }

    public static Enchantment removeEnchantment(NamespacedKey key)
    {
        return enchantmentByKey.remove(key);
    }

    public static Enchantment removeEnchantment(String name)
    {
        return enchantmentByName.remove(name);
    }

    public static Enchantment getEnchantment(NamespacedKey key)
    {
        return enchantmentByKey.get(key);
    }

    public static Enchantment getEnchantment(String name)
    {
        return enchantmentByName.get(name);
    }

    public static Enchantment[] getEnchantments()
    {
        return Enchantment.values();
    }
}
