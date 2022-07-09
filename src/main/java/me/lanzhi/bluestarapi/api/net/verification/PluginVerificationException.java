package me.lanzhi.bluestarapi.api.net.verification;

/**
 * @author Lanzhi
 */
public class PluginVerificationException extends Exception
{
    public static final PluginVerificationException EXCEPTION= new PluginVerificationException();
    private PluginVerificationException()
    {
        super("联网验证状态错误");
    }
}
