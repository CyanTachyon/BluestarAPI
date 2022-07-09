package me.lanzhi.bluestarapi.api.net.verification;

import me.lanzhi.bluestarapi.api.Bluestar;
import me.lanzhi.bluestarapi.api.config.YamlFile;
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
    private UUID key;
    public final boolean isSuccess()
    {
        return verification.isSuccess();
    }

    public YamlFile getKeyFile()
    {
        return keyFile;
    }

    public UUID getKey()
    {
        return key;
    }

    public void setKey(UUID key)
    {
        this.key=key;
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
        try
        {
            key=UUID.fromString(keyFile.getString("key"));
        }
        catch (Exception e)
        {
            key=null;
        }
        verification=new Verification(this,key).start();
        onStart();
        PluginCommand pluginCommand=Bluestar.newPluginCommand(getName()+"key",this);
        pluginCommand.setExecutor(new VerificationCommand(this));
        Bluestar.registerPluginCommand(pluginCommand);
    }

    public void onStart(){}
}
