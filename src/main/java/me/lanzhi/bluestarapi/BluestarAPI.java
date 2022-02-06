package me.lanzhi.bluestarapi;

import org.bukkit.plugin.java.JavaPlugin;

public final class BluestarAPI extends JavaPlugin
{
    @Override
    public void onEnable()
    {
        System.out.println("BluestarAPI已加载");
    }
    @Override
    public void onDisable()
    {
        System.out.println("BluestarAPI已卸载");
    }
}
