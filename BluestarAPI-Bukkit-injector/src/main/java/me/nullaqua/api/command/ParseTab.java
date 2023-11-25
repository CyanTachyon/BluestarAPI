package me.nullaqua.api.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记这个方法处理一个Tab补全
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.FIELD})
public @interface ParseTab
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