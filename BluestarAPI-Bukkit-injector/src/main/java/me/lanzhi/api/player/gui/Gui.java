package me.lanzhi.api.player.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.function.BiConsumer;

public interface Gui<T extends Gui<T>>
{
    Plugin getPlugin();

    String getTitle();

    void setTitle(String title);

    Player getPlayer();

    boolean isProhibitAnyClick();

    void setProhibitAnyClick(boolean b);

    boolean isPreventClose();

    void setPreventClose(boolean b);

    BiConsumer<T,GuiCloseAction> getOnClose();

    void setOnClose(BiConsumer<T,GuiCloseAction> onClose);

    boolean isOpen();

    Inventory getInventory();

    Listener getGuiListener();

    void open();

    void close();
}
