package me.lanzhi.bluestarapi.listener;

import me.lanzhi.bluestarapi.api.Bluestar;
import me.lanzhi.bluestarapi.api.player.ChatInformation;
import me.lanzhi.bluestarapi.api.player.InformationGetter;
import me.lanzhi.bluestarapi.api.player.AnvilInformation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.SmithItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class InfoGetterListener implements Listener
{
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        InformationGetter getter=Bluestar.getMainManager().getInformationGetter(event.getPlayer());
        if(getter instanceof ChatInformation)
        {
            event.setCancelled(true);
            Bluestar.getMainManager().removeInformationGetter(getter);
            if ("exit".equals(event.getMessage()))
            {
                getter.quit(true);
                return;
            }
            ((ChatInformation) getter).chat(event.getMessage());
        }
    }

    @EventHandler
    public void onPlayerAnvil(InventoryClickEvent event)
    {
        if (!(event.getWhoClicked() instanceof Player))
        {
            return;
        }
        InformationGetter getter=Bluestar.getMainManager().getInformationGetter((Player) event.getWhoClicked());
        if(getter instanceof AnvilInformation&&event.getInventory()==((AnvilInformation) getter).getAnvilUI())
        {
            event.setCancelled(true);
            if (event.getSlot()==2)
            {
                Bluestar.getMainManager().removeInformationGetter(getter);
                ((AnvilInformation) getter).anvil(event.getInventory().getItem(2).getItemMeta().getDisplayName());
                event.getWhoClicked().closeInventory();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event)
    {
        if (!(event.getPlayer() instanceof Player))
        {
            return;
        }
        Player player=(Player) event.getPlayer();
        InformationGetter getter=Bluestar.getMainManager().getInformationGetter(player);
        if (getter instanceof AnvilInformation&&event.getInventory()==((AnvilInformation) getter).getAnvilUI())
        {
            Bluestar.getMainManager().removeInformationGetter(getter);
            getter.quit(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        InformationGetter getter=Bluestar.getMainManager().getInformationGetter(event.getPlayer());
        if(getter!=null)
        {
            Bluestar.getMainManager().removeInformationGetter(getter);
            getter.quit(false);
        }
    }
}
