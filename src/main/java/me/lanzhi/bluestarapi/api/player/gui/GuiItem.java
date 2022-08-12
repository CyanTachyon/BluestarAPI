package me.lanzhi.bluestarapi.api.player.gui;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class GuiItem implements Cloneable
{
    private ItemStack item;
    private BiFunction<ChestGui,ClickType,ChestGui.Response> onClick;

    public ItemStack getItem()
    {
        return item;
    }

    public GuiItem setItem(ItemStack item)
    {
        this.item=item;
        return this;
    }

    public BiFunction<ChestGui,ClickType,ChestGui.Response> getOnClick()
    {
        return onClick;
    }

    public GuiItem setOnClick(BiFunction<ChestGui,ClickType,ChestGui.Response> onClick)
    {
        this.onClick=onClick;
        return this;
    }

    @Override
    public GuiItem clone()
    {
        try
        {
            GuiItem clone=(GuiItem) super.clone();
            return clone;
        }
        catch (CloneNotSupportedException e)
        {
            throw new AssertionError();
        }
    }
}
