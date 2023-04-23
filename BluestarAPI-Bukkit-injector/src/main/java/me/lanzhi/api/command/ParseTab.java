package me.lanzhi.api.command;

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
    String[] value();

    /**
     * 权限
     */
    String permission() default "";

    /**
     * 是否只有玩家才能执行
     */
    boolean onlyPlayer() default false;
}