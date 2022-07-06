package me.lanzhi.bluestarapi.api.net.verification;

public class VerificationReceive
{
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
