package org.bukkit.command;

import org.bukkit.plugin.Plugin;

public class BluestarCommandManager
{
    private BluestarCommandManager()
    {
    }
    public static PluginCommand newPluginCommand(String name,Plugin owningPlugin)
    {
        return new PluginCommand(name,owningPlugin);
    }
}
