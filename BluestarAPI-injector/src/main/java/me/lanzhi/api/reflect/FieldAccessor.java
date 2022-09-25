package me.lanzhi.api.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static me.lanzhi.api.reflect.Accessor.*;

public final class FieldAccessor
{
    private final Field field;
    private final boolean staticField;
    private final MethodHandle setter;
    private final MethodHandle getter;

    public FieldAccessor(Field field)
    {
        if (field==null)
        {
            this.field=field;
            this.setter=null;
            this.getter=null;
            this.staticField=false;
            return;
        }
        boolean staticField=Modifier.isStatic(field.getModifiers());
        MethodHandle getter=null;
        MethodHandle setter=null;
        if (field.getDeclaringClass().equals(Accessor.class)||field.getDeclaringClass().equals(FieldAccessor.class))
        {
            this.field=field;
            this.setter=null;
            this.getter=null;
            this.staticField=staticField;
            return;
        }
        try
        {
            if (staticField)
            {
                getter=LOOKUP.findStaticGetter(field.getDeclaringClass(),field.getName(),field.getType());
                setter=LOOKUP.findStaticSetter(field.getDeclaringClass(),field.getName(),field.getType());
            }
            else
            {
                getter=LOOKUP.findGetter(field.getDeclaringClass(),field.getName(),field.getType());
                setter=LOOKUP.findSetter(field.getDeclaringClass(),field.getName(),field.getType());
            }

            if (staticField)
            {
                getter=getter.asType(STATIC_FIELD_GETTER);
                setter=setter.asType(STATIC_FIELD_SETTER);
            }
            else
            {
                getter=getter.asType(VIRTUAL_FIELD_GETTER);
                setter=setter.asType(VIRTUAL_FIELD_SETTER);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        this.field=field;
        this.staticField=staticField;
        this.getter=getter;
        this.setter=setter;
    }


    public Object get(Object instance)
    {
        if (getter==null)
        {
            return null;
        }
        try
        {
            if (this.staticField)
            {
                return this.setter.invokeExact();
            }
            else
            {
                return this.setter.invokeExact(instance);
            }
        }
        catch (Throwable e)
        {
            throw new AssertionError(e);
        }
    }

    public void set(Object instance,Object value)
    {
        if (setter==null)
        {
            return;
        }
        try
        {
            if (this.staticField)
            {
                this.setter.invokeExact(value);
            }
            else
            {
                this.setter.invokeExact(instance,value);
            }

        }
        catch (Throwable e)
        {
            throw new AssertionError(e);
        }
    }

    public Field getField()
    {
        return field;
    }

    public boolean isStaticField()
    {
        return staticField;
    }
}
