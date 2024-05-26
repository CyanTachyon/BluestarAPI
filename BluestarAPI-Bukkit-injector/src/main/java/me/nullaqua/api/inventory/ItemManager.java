package me.nullaqua.api.inventory;

import com.google.common.collect.Multimap;
import me.nullaqua.api.config.AutoSerialize;
import me.nullaqua.api.reflect.FieldAccessor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.material.MaterialData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemManager implements Cloneable, AutoSerialize
{
    private static final FieldAccessor meta=FieldAccessor.getField(ItemStack.class,"meta");
    private final ItemStack itemStack;

    public ItemManager(@NotNull Material type)
    {
        this(new ItemStack(type));
    }

    public ItemManager(ItemStack itemStack)
    {
        this.itemStack=itemStack;
        itemStack.setItemMeta(Bukkit.getItemFactory().getItemMeta(itemStack.getType()));
    }

    public ItemManager(@NotNull Material type,int amount)
    {
        this(new ItemStack(type,amount));
    }

    public ItemManager(@NotNull Material type,int amount,short damage)
    {
        this(new ItemStack(type,amount,damage));
    }

    public ItemManager(@NotNull Material type,int amount,short damage,@Nullable Byte data)
    {
        this(new ItemStack(type,amount,damage,data));
    }

    @NotNull
    public Material getType()
    {
        return itemStack.getType();
    }

    public ItemManager setType(@NotNull Material type)
    {
        itemStack.setType(type);
        return this;
    }

    public int getAmount()
    {
        return itemStack.getAmount();
    }

    public ItemManager setAmount(int amount)
    {
        itemStack.setAmount(amount);
        return this;
    }

    @Nullable
    public MaterialData getData()
    {
        return itemStack.getData();
    }

    public ItemManager setData(@Nullable MaterialData data)
    {
        itemStack.setData(data);
        return this;
    }

    public short getDurability()
    {
        return itemStack.getDurability();
    }

    public ItemManager setDurability(short durability)
    {
        itemStack.setDurability(durability);
        return this;
    }

    public int getMaxStackSize()
    {
        return itemStack.getMaxStackSize();
    }

    public boolean isSimilar(@Nullable ItemStack stack)
    {
        return itemStack.isSimilar(stack);
    }

    public int hashCode()
    {
        return itemStack.hashCode();
    }

    public boolean equals(Object obj)
    {
        return itemStack.equals(obj);
    }

    public ItemManager clone()
    {
        return new ItemManager(itemStack.clone());
    }

    public String toString()
    {
        return itemStack.toString();
    }

    public boolean containsEnchantment(@NotNull Enchantment ench)
    {
        return itemStack.containsEnchantment(ench);
    }

    public int getEnchantmentLevel(@NotNull Enchantment ench)
    {
        return itemStack.getEnchantmentLevel(ench);
    }

    @NotNull
    public Map<Enchantment,Integer> getEnchantments()
    {
        return itemStack.getEnchantments();
    }

    public ItemManager addEnchantments(@NotNull Map<Enchantment,Integer> enchantments)
    {
        itemStack.addEnchantments(enchantments);
        return this;
    }

    public ItemManager addEnchantment(@NotNull Enchantment ench,int level)
    {
        itemStack.addEnchantment(ench,level);
        return this;
    }

    public ItemManager addUnsafeEnchantments(@NotNull Map<Enchantment,Integer> enchantments)
    {
        itemStack.addUnsafeEnchantments(enchantments);
        return this;
    }

    public ItemManager addUnsafeEnchantment(@NotNull Enchantment ench,int level)
    {
        itemStack.addUnsafeEnchantment(ench,level);
        return this;
    }

    public int removeEnchantment(@NotNull Enchantment ench)
    {
        return itemStack.removeEnchantment(ench);
    }

    @NotNull
    public Map<String,Object> serialize()
    {
        return itemStack.serialize();
    }

    public boolean hasItemMeta()
    {
        return itemStack.hasItemMeta();
    }

    public boolean hasDisplayName()
    {
        return getItemMeta().hasDisplayName();
    }

    @NotNull
    public ItemMeta getItemMeta()
    {
        try
        {
            ItemMeta itemMeta=(ItemMeta) meta.get(itemStack);
            if (itemMeta==null)
            {
                itemMeta=Bukkit.getItemFactory().getItemMeta(itemStack.getType());
                itemStack.setItemMeta(itemMeta);
            }
            return itemMeta;
        }
        catch (Throwable e)
        {
            return null;
        }
    }

    @NotNull
    public String getDisplayName()
    {
        return getItemMeta().getDisplayName();
    }

    public ItemManager setDisplayName(@Nullable String name)
    {
        ItemMeta itemMeta=getItemMeta();
        itemMeta.setDisplayName(name);
        setItemMeta(itemMeta);
        return this;
    }

    public boolean setItemMeta(@Nullable ItemMeta itemMeta)
    {
        if (itemMeta==null)
        {
            itemStack.setItemMeta(Bukkit.getItemFactory().getItemMeta(itemStack.getType()));
            return true;
        }
        return itemStack.setItemMeta(itemMeta);
    }

    public boolean hasLocalizedName()
    {
        return getItemMeta().hasLocalizedName();
    }

    @NotNull
    public String getLocalizedName()
    {
        return getItemMeta().getLocalizedName();
    }

    public ItemManager setLocalizedName(@Nullable String name)
    {
        ItemMeta itemMeta=getItemMeta();
        itemMeta.setLocalizedName(name);
        setItemMeta(itemMeta);
        return this;
    }

    public boolean hasLore()
    {
        return getItemMeta().hasLore();
    }

    @Nullable
    public List<String> getLore()
    {
        return getItemMeta().getLore();
    }

    public ItemManager setLore(@Nullable List<String> lore)
    {
        ItemMeta itemMeta=getItemMeta();
        itemMeta.setLore(lore);
        setItemMeta(itemMeta);
        return this;
    }

    public boolean hasCustomModelData()
    {
        return getItemMeta().hasCustomModelData();
    }

    public int getCustomModelData()
    {
        return getItemMeta().getCustomModelData();
    }

    public ItemManager setCustomModelData(@Nullable Integer data)
    {
        ItemMeta itemMeta=getItemMeta();
        itemMeta.setCustomModelData(data);
        setItemMeta(itemMeta);
        return this;
    }

    public boolean hasEnchants()
    {
        return getItemMeta().hasEnchants();
    }

    public boolean hasEnchant(@NotNull Enchantment ench)
    {
        return getItemMeta().hasEnchant(ench);
    }

    public int getEnchantLevel(@NotNull Enchantment ench)
    {
        return getItemMeta().getEnchantLevel(ench);
    }

    @NotNull
    public Map<Enchantment,Integer> getEnchants()
    {
        return getItemMeta().getEnchants();
    }

    public boolean addEnchant(@NotNull Enchantment ench,int level,boolean ignoreLevelRestriction)
    {
        ItemMeta itemMeta=getItemMeta();
        boolean b=itemMeta.addEnchant(ench,level,ignoreLevelRestriction);
        setItemMeta(itemMeta);
        return b;
    }

    public boolean removeEnchant(@NotNull Enchantment ench)
    {
        ItemMeta itemMeta=getItemMeta();
        boolean b=itemMeta.removeEnchant(ench);
        setItemMeta(itemMeta);
        return b;
    }

    public boolean hasConflictingEnchant(@NotNull Enchantment ench)
    {
        ItemMeta itemMeta=getItemMeta();
        boolean b=itemMeta.hasConflictingEnchant(ench);
        setItemMeta(itemMeta);
        return b;
    }

    public ItemManager addItemFlags(@NotNull ItemFlag... itemFlags)
    {
        ItemMeta itemMeta=getItemMeta();
        itemMeta.addItemFlags(itemFlags);
        setItemMeta(itemMeta);
        return this;
    }

    public ItemManager removeItemFlags(@NotNull ItemFlag... itemFlags)
    {
        ItemMeta itemMeta=getItemMeta();
        itemMeta.removeItemFlags(itemFlags);
        setItemMeta(itemMeta);
        return this;
    }

    @NotNull
    public Set<ItemFlag> getItemFlags()
    {
        return getItemMeta().getItemFlags();
    }

    public boolean hasItemFlag(@NotNull ItemFlag flag)
    {
        return getItemMeta().hasItemFlag(flag);
    }

    public boolean isUnbreakable()
    {
        return getItemMeta().isUnbreakable();
    }

    public ItemManager setUnbreakable(boolean unbreakable)
    {
        ItemMeta itemMeta=getItemMeta();
        itemMeta.setUnbreakable(unbreakable);
        setItemMeta(itemMeta);
        return this;
    }

    public boolean hasAttributeModifiers()
    {
        return getItemMeta().hasAttributeModifiers();
    }

    @Nullable
    public Multimap<Attribute,AttributeModifier> getAttributeModifiers()
    {
        return getItemMeta().getAttributeModifiers();
    }

    public ItemManager setAttributeModifiers(@Nullable Multimap<Attribute,AttributeModifier> attributeModifiers)
    {
        ItemMeta itemMeta=getItemMeta();
        itemMeta.setAttributeModifiers(attributeModifiers);
        setItemMeta(itemMeta);
        return this;
    }

    @NotNull
    public Multimap<Attribute,AttributeModifier> getAttributeModifiers(@NotNull EquipmentSlot slot)
    {
        return getItemMeta().getAttributeModifiers(slot);
    }

    @Nullable
    public Collection<AttributeModifier> getAttributeModifiers(@NotNull Attribute attribute)
    {
        return getItemMeta().getAttributeModifiers(attribute);
    }

    public boolean addAttributeModifier(@NotNull Attribute attribute,@NotNull AttributeModifier modifier)
    {
        ItemMeta itemMeta=getItemMeta();
        boolean b=itemMeta.addAttributeModifier(attribute,modifier);
        setItemMeta(itemMeta);
        return b;
    }

    public boolean removeAttributeModifier(@NotNull Attribute attribute)
    {
        ItemMeta itemMeta=getItemMeta();
        boolean b=itemMeta.removeAttributeModifier(attribute);
        setItemMeta(itemMeta);
        return b;
    }

    public boolean removeAttributeModifier(@NotNull EquipmentSlot slot)
    {
        ItemMeta itemMeta=getItemMeta();
        boolean b=itemMeta.removeAttributeModifier(slot);
        setItemMeta(itemMeta);
        return b;
    }

    public boolean removeAttributeModifier(@NotNull Attribute attribute,@NotNull AttributeModifier modifier)
    {
        ItemMeta itemMeta=getItemMeta();
        boolean b=itemMeta.removeAttributeModifier(attribute,modifier);
        setItemMeta(itemMeta);
        return b;
    }

    @NotNull
    public CustomItemTagContainer getCustomTagContainer()
    {
        return getItemMeta().getCustomTagContainer();
    }

    public ItemManager setVersion(int version)
    {
        ItemMeta itemMeta=getItemMeta();
        itemMeta.setVersion(version);
        setItemMeta(itemMeta);
        return this;
    }
}
