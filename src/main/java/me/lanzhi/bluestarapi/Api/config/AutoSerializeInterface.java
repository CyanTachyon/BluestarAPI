package me.lanzhi.bluestarapi.Api.config;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.Map;

public interface AutoSerializeInterface extends ConfigurationSerializable
{
    @Override
    default Map<String, Object> serialize()
    {
        Class<? extends AutoSerializeInterface> clazz=this.getClass();
        AutoSerialize.registerClass(clazz);
        return AutoSerialize.serialize(this,clazz);
    }
}
