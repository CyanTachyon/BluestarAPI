package me.lanzhi.bluestarapi.api.net.verification;

import java.io.Serializable;

public final class VerificationReceive implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final boolean success;
    private final long time;

    public VerificationReceive(final boolean success,final long time)
    {
        this.success=success;
        this.time=time;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public long getTime()
    {
        return time;
    }
}
