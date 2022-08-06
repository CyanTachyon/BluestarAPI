package me.lanzhi.bluestarapi;

import me.lanzhi.bluestarapi.api.Bluestar;
import me.lanzhi.bluestarapi.api.config.AutoSerialize;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import static me.lanzhi.bluestarapi.api.config.AutoSerialize.nameOfAutoSerialize;

public final class BluestarAPI extends JavaPlugin
{
    public static Plugin thisPlugin;
    public static boolean debug=false;

    @Override
    public void onEnable()
    {
        thisPlugin=this;
        ConfigurationSerialization.registerClass(AutoSerialize.class,nameOfAutoSerialize);
        System.out.println("BluestarAPI已加载");
        PluginCommand command=Bluestar.getCommandManager().newPluginCommand("bluestarapidebug",this);
        command.setExecutor(this);
        Bluestar.getCommandManager().registerPluginCommand(command);
    }

    @Override
    public void onDisable()
    {
        System.out.println("BluestarAPI已卸载");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,@NotNull Command command,@NotNull String label,@NotNull String[] args)
    {
        debug=!debug;
        return true;
    }
}
