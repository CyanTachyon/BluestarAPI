package me.lanzhi.api.net.verification;

import java.io.Serializable;
import java.util.UUID;

public final class VerificationRequest implements Serializable
{
    private static final long serialVersionUID=1L;
    private final String plugin;
    private final UUID key;

    public VerificationRequest(String plugin,UUID key)
    {
        this.plugin=plugin;
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
