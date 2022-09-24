package me.lanzhi.api.net.verification;

/**
 * @author Lanzhi
 */
public final class PluginVerificationException extends Exception
{
    public static final PluginVerificationException EXCEPTION= new PluginVerificationException();
    private PluginVerificationException()
    {
        super("联网验证状态错误");
    }
}
