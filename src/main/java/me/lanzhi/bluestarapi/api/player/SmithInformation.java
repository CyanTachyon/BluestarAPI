package me.lanzhi.bluestarapi.api.player;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;

public interface SmithInformation extends InformationGetter
{
    public void smith(String str);

    public void setSmithUI(SmithingInventory smith);

    public SmithingInventory getSmithUI();

    public String getTitle();

    public ItemStack getItem();
}
