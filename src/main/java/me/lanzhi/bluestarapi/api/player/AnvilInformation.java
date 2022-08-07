package me.lanzhi.bluestarapi.api.player;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface AnvilInformation extends InformationGetter
{
    public void anvil(String str);

    public Inventory getAnvilUI();

    public void setAnvilUI(Inventory smith);

    public String getTitle();

    public ItemStack getItem();
}
