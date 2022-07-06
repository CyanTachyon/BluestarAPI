package me.lanzhi.bluestarapi.api.net.verification;

import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class VerificationRequest
{
    private final String plugin;
    private final UUID key;

    public VerificationRequest(Plugin plugin,UUID key)
    {
        this.plugin=plugin.getName();
        this.key=key;
    }

    public String getPlugin()
    {
        return plugin;
    }

    public UUID getKey()
    {
        return key;
    }
}
