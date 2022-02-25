package me.lanzhi.bluestarapi.Api;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import java.io.File;
import java.io.IOException;

public class yamlConfig extends FileConfiguration
{
    private File file;

    public yamlConfig(File file)
    {
        this.file = file;
        try
        {
            file.createNewFile();
        }
        catch (IOException e)
        {
            System.out.println("§4[BluestarAPI]创建文件时出错:"+file.getName());
        }
        try
        {
            this.load(file);
        }
        catch (IOException e)
        {
            System.out.println("§4[BluestarAPI]加载文件时出错:" + file.getName());
        }
        catch (org.bukkit.configuration.InvalidConfigurationException e)
        {
            System.out.println("§4[BluestarAPI]加载文件时出错:" + file.getName());
        }
    }

    public void save()
    {
        try
        {
            this.save(file);
        }
        catch (IOException e)
        {
            System.out.println("§4[BluestarAPI]无法保存文件:" + file.getName());
        }
    }

    @Override
    public String saveToString() {return file.getPath();}

    @Override
    public void loadFromString(String contents) throws InvalidConfigurationException {}
}