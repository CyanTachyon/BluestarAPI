package me.lanzhi.api.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public final class YamlFile extends YamlConfiguration
{
    private final File file;

    public YamlFile(@NotNull File file)
    {
        this.file=file;
        try
        {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        catch (IOException e)
        {
            System.out.println("§4[BluestarAPI]创建文件时出错:"+file.getName());
        }
    }

    @NotNull
    public static YamlFile loadYamlFile(@NotNull File file)
    {
        YamlFile yamlFile=new YamlFile(file);
        yamlFile.reload();
        return yamlFile;
    }

    @NotNull
    public YamlFile reload()
    {
        try
        {
            file.getParentFile().mkdirs();
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
            System.out.println("§4[BluestarAPI]加载文件时出错:"+file.getName());
        }
        catch (org.bukkit.configuration.InvalidConfigurationException e)
        {
            System.out.println("§4[BluestarAPI]加载文件时出错:"+file.getName());
        }
        return this;
    }

    @NotNull
    public YamlFile save()
    {
        try
        {
            file.createNewFile();
            this.save(file);
        }
        catch (IOException e)
        {
            System.out.println("§4[BluestarAPI]无法保存文件:"+file.getName());
        }
        return this;
    }
}