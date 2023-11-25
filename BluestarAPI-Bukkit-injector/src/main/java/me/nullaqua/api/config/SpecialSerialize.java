package me.nullaqua.api.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 进行特殊的序列化/不进行序列化
 * @author Lanzhi
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SpecialSerialize
{
    /**
     * 用于进行序列化的方法,要求此方法的传参仅有一个,且与被注解的字段类型相同
     * 留空表示不序列化此字段
     * @return 序列化处理方法的名称
     */
    String serialize() default "";

    /**
     * 用于反序列化的方法,传参应该与{@link #serialize()}的返回值相同
     * 返回值应该与字段类型相同
     * @return 反序列化方法的名称
     */
    String deserialize() default "";
}
