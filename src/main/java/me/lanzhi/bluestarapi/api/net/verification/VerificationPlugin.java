package me.lanzhi.bluestarapi.api.net.verification;

import me.lanzhi.bluestarapi.api.config.YamlFile;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.UUID;

public class VerificationPlugin extends JavaPlugin
{
    protected boolean isSuccess=true;

    public boolean isSuccess()
    {
        return isSuccess;
    }

    @Override
    public final void onLoad()
    {
        //saveResource("key.yml", false);
        YamlFile file=YamlFile.loadYamlFile(new File(getDataFolder(),"key.yml"));
        file.set("key","授权秘钥");
        UUID uuid;
        try
        {
            uuid=UUID.fromString(file.getString("key"));
        }
        catch (Exception e)
        {
            uuid=null;
        }
        new Verification(this,uuid);
    }
}
