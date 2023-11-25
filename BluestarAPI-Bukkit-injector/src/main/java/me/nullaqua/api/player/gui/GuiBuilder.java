package me.nullaqua.api.player.gui;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.function.BiConsumer;

public interface GuiBuilder<T extends Gui<T>>
{
    default T open(Player player)
    {
        T t=make(player);
        t.open();
        return t;
    }

    T make(Player player);

    GuiBuilder<T> title(String title);

    String title();

    GuiBuilder<T> prohibitAnyClick(boolean prohibitAnyClick);

    boolean prohibitAnyClick();

    GuiBuilder<T> preventClose(boolean preventClose);

    boolean preventClose();

    GuiBuilder<T> plugin(Plugin plugin);

    Plugin plugin();

    GuiBuilder<T> onClose(BiConsumer<T,GuiCloseAction> onClose);

    BiConsumer<T,GuiCloseAction> onClose();
}
