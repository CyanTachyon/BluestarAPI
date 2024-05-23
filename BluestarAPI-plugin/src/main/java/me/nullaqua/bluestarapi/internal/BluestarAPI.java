package me.nullaqua.bluestarapi.internal;

import org.bukkit.plugin.java.JavaPlugin;

public final class BluestarAPI extends JavaPlugin
{
    @Override
    public void onEnable()
    {
        this.getLogger().info("BluestarAPI is enabled!");
    }

    @Override
    public void onDisable()
    {
        this.getLogger().info("BluestarAPI is disabled!");
    }
}
