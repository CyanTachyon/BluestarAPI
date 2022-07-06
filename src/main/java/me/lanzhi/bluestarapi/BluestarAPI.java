package me.lanzhi.bluestarapi;

import me.lanzhi.bluestarapi.api.config.AutoSerialize;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import static me.lanzhi.bluestarapi.api.config.AutoSerialize.nameOfAutoSerialize;

public final class BluestarAPI extends JavaPlugin
{
    public static Plugin thisPlugin;

    @Override
    public void onEnable()
    {
        thisPlugin=this;
        ConfigurationSerialization.registerClass(AutoSerialize.class,nameOfAutoSerialize);
        System.out.println("BluestarAPI已加载");
    }

    @Override
    public void onDisable()
    {
        System.out.println("BluestarAPI已卸载");
    }
}
