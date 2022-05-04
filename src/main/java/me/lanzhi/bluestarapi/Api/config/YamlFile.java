package me.lanzhi.bluestarapi.Api.config;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

final public class YamlFile extends YamlConfiguration
{
    private File file;

    public YamlFile(File file)
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
    }

    public void reload()
    {
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
            file.createNewFile();
            this.save(file);
        }
        catch (IOException e)
        {
            System.out.println("§4[BluestarAPI]无法保存文件:" + file.getName());
        }
    }
}