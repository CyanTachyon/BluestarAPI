package me.lanzhi.bluestarapi.Api.config;

import java.lang.annotation.*;

/**
 * @author Lanzhi
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DoNotSerialize
{
}
