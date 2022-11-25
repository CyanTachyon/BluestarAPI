package me.lanzhi.api;

import me.lanzhi.api.reflect.FieldAccessor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import java.util.Map;

public final class EnchantmentManager
{
    public static void upData()
    {
        Bluestar.setEnchantmentManager(new EnchantmentManager());
    }

    private final FieldAccessor acceptRegisterEnchantment;
    private final Map<NamespacedKey,Enchantment> enchantmentByKey;
    private final Map<String, Enchantment> enchantmentByName;

    public EnchantmentManager()
    {
        try
        {
            acceptRegisterEnchantment=FieldAccessor.getDeclaredField(Enchantment.class,"acceptingNew");
            FieldAccessor byKey=FieldAccessor.getDeclaredField(Enchantment.class,"byKey");
            enchantmentByKey=(Map<NamespacedKey,Enchantment>) byKey.get(null);

            FieldAccessor byName=FieldAccessor.getDeclaredField(Enchantment.class,"byName");
            enchantmentByName=(Map<String,Enchantment>) byName.get(null);
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    public boolean openEnchantmentRegistrations()
    {
        if (acceptRegisterEnchantment==null)
        {
            return false;
        }
        try
        {
            acceptRegisterEnchantment.set(null,true);
            return true;
        }
        catch (Throwable e)
        {
            return false;
        }
    }

    public void closeEnchantmentRegistrations()
    {
        Enchantment.stopAcceptingRegistrations();
    }

    public boolean registerEnchantment(Enchantment enchantment)
    {
        if (!openEnchantmentRegistrations())
        {
            return false;
        }
        Enchantment.registerEnchantment(enchantment);
        closeEnchantmentRegistrations();
        return true;
    }

    public Enchantment removeEnchantment(NamespacedKey key)
    {
        Enchantment enchantment=enchantmentByKey.remove(key);
        enchantmentByName.values().remove(enchantment);
        return enchantment;
    }

    public Enchantment removeEnchantment(String name)
    {
        Enchantment enchantment=enchantmentByName.remove(name);
        enchantmentByKey.values().remove(enchantment);
        return enchantment;
    }

    public Enchantment getEnchantment(NamespacedKey key)
    {
        return enchantmentByKey.get(key);
    }

    public Enchantment getEnchantment(String name)
    {
        return enchantmentByName.get(name);
    }

    public Enchantment[] getEnchantments()
    {
        return Enchantment.values();
    }
}
