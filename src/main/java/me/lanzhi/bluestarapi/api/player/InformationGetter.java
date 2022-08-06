package me.lanzhi.bluestarapi.api.player;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;

import java.util.List;

public interface InformationGetter extends Cloneable
{
    public Player getPlayer();

    public void quit(boolean b);

    public List<BaseComponent> getTips();

}
