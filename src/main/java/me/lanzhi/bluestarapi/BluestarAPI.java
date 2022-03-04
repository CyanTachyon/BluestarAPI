package me.lanzhi.bluestarapi;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class BluestarAPI extends JavaPlugin
{
    public static Plugin thisPlugin;
    @Override
    public void onEnable()
    {
        thisPlugin=BluestarAPI.getProvidingPlugin(BluestarAPI.class);
        System.out.println("BluestarAPI已加载");
    }
    @Override
    public void onDisable()
    {
        System.out.println("BluestarAPI已卸载");
    }
}
