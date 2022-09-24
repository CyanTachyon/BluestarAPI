package me.lanzhi.api.player.gui;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiFunction;

public class GuiItem implements Cloneable
{
    public static final GuiItem EMPTY;

    static
    {
        EMPTY=new GuiItem()
        {
            @Override
            public int hashCode()
            {
                return 0;
            }

            {
                super.setItem(new ItemStack(Material.AIR));
                super.setOnClick((gui,type)->ChestGui.Response.nothing());
            }

            @Override
            public GuiItem setItem(ItemStack item)
            {
                return this;
            }

            @Override
            public GuiItem setOnClick(BiFunction<ChestGui, ClickType, ChestGui.Response> onClick)
            {
                return this;
            }

            @Override
            public boolean equals(Object obj)
            {
                return obj==this;
            }

            @Override
            public String toString()
            {
                return "GuiItem{EMPTY}";
            }

            @Override
            public GuiItem clone()
            {
                return this;
            }
        };
    }

    private ItemStack item;
    private BiFunction<ChestGui, ClickType, ChestGui.Response> onClick;

    public GuiItem()
    {
        item=new ItemStack(Material.AIR);
        onClick=(gui,type)->ChestGui.Response.nothing();
    }

    public ItemStack getItem()
    {
        return item;
    }

    public GuiItem setItem(ItemStack item)
    {
        this.item=item;
        return this;
    }

    public BiFunction<ChestGui, ClickType, ChestGui.Response> getOnClick()
    {
        return onClick;
    }

    public GuiItem setOnClick(BiFunction<ChestGui, ClickType, ChestGui.Response> onClick)
    {
        this.onClick=onClick;
        return this;
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof GuiItem)
        {
            return item.equals(((GuiItem) obj).item)&&onClick.equals(((GuiItem) obj).onClick);
        }
        return false;
    }

    @Override
    public GuiItem clone()
    {
        try
        {
            GuiItem clone=(GuiItem) super.clone();
            clone.item=item.clone();
            return clone;
        }
        catch (CloneNotSupportedException e)
        {
            throw new AssertionError();
        }
    }

    @Override
    public String toString()
    {
        return "GuiItem { Item:"+item+", OnClick:"+onClick+" }";
    }
}
