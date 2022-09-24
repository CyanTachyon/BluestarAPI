package me.lanzhi.api.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.function.BiConsumer;

public final class YamlFile extends YamlConfiguration
{
    private final File file;
    private long time=0;
    private boolean exists=true;

    public YamlFile(@NotNull File file)
    {
        this(file,false,null,JavaPlugin.getProvidingPlugin(Thread.currentThread().getStackTrace()[1].getClass()));
    }

    public YamlFile(@NotNull File file,boolean autoReload)
    {
        this(file,autoReload,(yamlFile,event)->
        {
            if (event==Event.UPDATE)
            {
                yamlFile.reload();
            }
        },JavaPlugin.getProvidingPlugin(Thread.currentThread().getStackTrace()[1].getClass()));
    }

    public YamlFile(@NotNull File file,boolean autoReload,String message)
    {
        this(file,autoReload,(yamlFile,event)->
        {
            if (event==Event.UPDATE)
            {
                yamlFile.reload();
                Bukkit.getLogger().info(message);
            }
        },JavaPlugin.getProvidingPlugin(Thread.currentThread().getStackTrace()[1].getClass()));
    }

    public YamlFile(@NotNull File file,boolean autoReload,BiConsumer<YamlFile, Event> biConsumer)
    {
        this(file,
             autoReload,
             biConsumer,
             JavaPlugin.getProvidingPlugin(Thread.currentThread().getStackTrace()[1].getClass()));
    }

    private YamlFile(@NotNull File file,boolean autoReload,BiConsumer<YamlFile, Event> biConsumer,Plugin plugin)
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
        if (autoReload)
        {
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,()->
            {
                if (file.exists()&&!exists)
                {
                    exists=file.exists();
                    time=file.lastModified();
                    biConsumer.accept(YamlFile.this,Event.CREATE);
                }
                else if (!file.exists()&&exists)
                {
                    exists=file.exists();
                    time=file.lastModified();
                    biConsumer.accept(YamlFile.this,Event.DELETE);
                }
                else if (exists&&file.lastModified()!=time)
                {
                    time=file.lastModified();
                    biConsumer.accept(YamlFile.this,Event.UPDATE);
                }
            },0,20);
        }
    }

    @NotNull
    public static YamlFile loadYamlFile(@NotNull File file)
    {
        return new YamlFile(file,
                            false,
                            null,
                            JavaPlugin.getProvidingPlugin(Thread.currentThread()
                                                                .getStackTrace()[1].getClass())).reload();
    }

    @NotNull
    public static YamlFile loadYamlFile(@NotNull File file,boolean autoReload)
    {
        return new YamlFile(file,autoReload,(yamlFile,event)->
        {
            if (event==Event.UPDATE)
            {
                yamlFile.reload();
            }
        },JavaPlugin.getProvidingPlugin(Thread.currentThread().getStackTrace()[1].getClass())).reload();
    }

    @NotNull
    public static YamlFile loadYamlFile(@NotNull File file,boolean autoReload,String message)
    {
        return new YamlFile(file,autoReload,(yamlFile,event)->
        {
            if (event==Event.UPDATE)
            {
                yamlFile.reload();
                Bukkit.getLogger().info(message);
            }
        },JavaPlugin.getProvidingPlugin(Thread.currentThread().getStackTrace()[1].getClass())).reload();
    }

    @NotNull
    public static YamlFile loadYamlFile(@NotNull File file,boolean autoReload,BiConsumer<YamlFile, Event> biConsumer)
    {
        return new YamlFile(file,
                            autoReload,
                            biConsumer,
                            JavaPlugin.getProvidingPlugin(Thread.currentThread()
                                                                .getStackTrace()[1].getClass())).reload();
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
            e.printStackTrace();
        }
        try
        {
            this.load(file);
        }
        catch (IOException|InvalidConfigurationException e)
        {
            System.out.println("§4[BluestarAPI]加载文件时出错:"+file.getName());
            e.printStackTrace();
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

    public enum Event
    {
        UPDATE,DELETE,CREATE
    }
}