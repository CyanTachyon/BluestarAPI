package me.lanzhi.bluestarapi.api.net.verification;

import me.lanzhi.bluestarapi.api.config.YamlFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;

public abstract class VerificationPlugin extends JavaPlugin
{
    protected boolean isSuccess=true;

    public final boolean isSuccess()
    {
        return isSuccess;
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
        YamlFile yamlFile=YamlFile.loadYamlFile(file);
        UUID uuid;
        try
        {
            uuid=UUID.fromString(yamlFile.getString("key"));
        }
        catch (Exception e)
        {
            uuid=null;
        }
        new Verification(this,uuid).start();
        onStart();
    }

    public abstract void onStart();
}
