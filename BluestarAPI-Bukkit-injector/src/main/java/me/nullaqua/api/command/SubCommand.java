package me.nullaqua.api.command;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.FIELD})
@Repeatable(SubCommands.class)
public @interface SubCommand
{
    /**
     * 处理的命令的传参
     */
    String value();

    /**
     * 权限
     */
    String permission() default "";

    /**
     * 特定的命令发送者才能执行
     */
    Class<?>[] only() default {};
}