package me.lanzhi.api.net.verification;

import me.lanzhi.api.Bluestar;
import me.lanzhi.api.config.YamlFile;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;

public class VerificationPlugin extends JavaPlugin
{
    private Verification verification;
    private YamlFile keyFile;
    public final boolean isSuccess()
    {
        return verification.isSuccess();
    }

    public final YamlFile getKeyFile()
    {
        return keyFile;
    }

    public final UUID getKey()
    {
        return verification.getKey();
    }

    public final void setKey(UUID key)
    {
        verification.setKey(key);
    }

    public final void checkVerification() throws PluginVerificationException
    {
        if (!verification.isSuccess())
        {
            throw new PluginVerificationException();
        }
    }

    @Override
    public final void onEnable()
    {
        getDataFolder().mkdirs();
        File file=new File(getDataFolder(),"key.yml");
        try
        {
            if (file.createNewFile())
            {
                PrintStream printStream=new PrintStream(file);
                printStream.println("key: \"授权秘钥\"");
                printStream.close();
            }
        }
        catch (IOException e)
        {
            System.out.println("创建配置文件失败");
        }
        keyFile=YamlFile.loadYamlFile(file);
        UUID key;
        try
        {
            key=UUID.fromString(keyFile.getString("key"));
        }
        catch (Exception e)
        {
            key=null;
        }
        verification=new Verification(this,key).start();
        PluginCommand pluginCommand=Bluestar.getCommandManager().newPluginCommand(getName()+"key",this);
        pluginCommand.setExecutor(new VerificationCommand(this));
        Bluestar.getCommandManager().registerPluginCommand(pluginCommand);
        onStart();
    }

    public void onStart(){}
}
