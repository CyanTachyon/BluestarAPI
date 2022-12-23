package me.lanzhi.api.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 序列化后的名称,在一个类上添加此注解来标记序列化后的名称
 * @author Lanzhi
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SerializeAs
{
    /**
     * 序列化后的名称
     * @return 序列化后的名称
     */
    String value();
}
