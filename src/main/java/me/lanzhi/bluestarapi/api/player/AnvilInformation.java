package me.lanzhi.bluestarapi.api.player;

import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

public interface AnvilInformation extends InformationGetter
{
    public void anvil(String str);

    public void setAnvilUI(AnvilInventory smith);

    public AnvilInventory getAnvilUI();

    public String getTitle();

    public ItemStack getItem();
}
