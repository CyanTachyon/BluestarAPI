package me.lanzhi.api.command;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(CommandAlias.class)
public @interface CommandAlia
{
    /**
     * 命令的别名
     */
    String value();
}