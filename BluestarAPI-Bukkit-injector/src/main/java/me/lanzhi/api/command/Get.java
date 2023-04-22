package me.lanzhi.api.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在方法的参数中使用，表示这个参数是从命令中解析出来的
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Get
{
    /**
     * 参数的名称
     */
    String value();
}