package me.lanzhi.api.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static me.lanzhi.api.reflect.ReflectAccessor.*;

public final class FieldAccessor
{
    private final Field field;
    private final boolean staticField;
    private final MethodHandle setter;
    private final MethodHandle getter;

    public static List<FieldAccessor> getDeclaredFields(Object o)
    {
        if (o==null)
        {
            return new ArrayList<>();
        }
        return getDeclaredFields(o.getClass());
    }

    public FieldAccessor(Field field)
    {
        if (field==null)
        {
            this.field=null;
            this.setter=null;
            this.getter=null;
            this.staticField=false;
            return;
        }
        boolean staticField=Modifier.isStatic(field.getModifiers());
        MethodHandle getter=null;
        MethodHandle setter=null;
        if (field.getDeclaringClass().getPackage().equals(this.getClass().getPackage()))
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
        }
        this.field=field;
        this.staticField=staticField;
        this.getter=getter;
        this.setter=setter;
    }

    public static FieldAccessor getField(Class<?> c,String field)
    {
        if (c==null||field==null)
        {
            return null;
        }
        try
        {
            return new FieldAccessor(c.getDeclaredField(field));
        }
        catch (NoSuchFieldException e)
        {
            return null;
        }
    }

    public static FieldAccessor getDeclaredField(Class<?> c,String field)
    {
        if (c==null||field==null)
        {
            return null;
        }
        for (Class<?> clazz: ReflectAccessor.getAllSuperClass(c))
        {
            try
            {
                return new FieldAccessor(clazz.getDeclaredField(field));
            }
            catch (Exception e)
            {
                continue;
            }
        }
        return null;
    }

    public static List<FieldAccessor> getDeclaredFields(Class<?> type)
    {
        List<FieldAccessor> fields=new ArrayList<>();
        for (Class<?> c: getAllSuperClass(type))
        {
            fields.addAll(getFields(c));
        }
        return fields;
    }

    public static List<FieldAccessor> getFields(Object o)
    {
        if (o==null)
        {
            return new ArrayList<>();
        }
        return getFields(o.getClass());
    }

    public static List<FieldAccessor> getFields(Class<?> type)
    {
        List<FieldAccessor> fields=new ArrayList<>();
        List.of(type.getDeclaredFields()).forEach(field ->
                                                  {
                                                      fields.add(new FieldAccessor(field));
                                                  });
        return fields;
    }

    @Override
    public String toString()
    {
        return "FieldAccessor{"+field+"}";
    }


    public Field getField()
    {
        return field;
    }

    public boolean isStaticField()
    {
        return staticField;
    }

    public Object get(Object instance) throws Throwable
    {
        if (getter==null)
        {
            if (!ReflectAccessor.isVisibility(field.getDeclaringClass()))
            {
                return null;
            }
            field.setAccessible(true);
            Object o=field.get(instance);
            field.setAccessible(false);
            return o;
        }
        if (this.staticField)
        {
            return this.getter.invokeExact();
        }
        else
        {
            return this.getter.invokeExact(instance);
        }
    }

    public void set(Object instance,Object value) throws Throwable
    {
        if (setter==null)
        {
            if (!ReflectAccessor.isVisibility(field.getDeclaringClass()))
            {
                return;
            }
            field.setAccessible(true);
            field.set(instance,value);
            field.setAccessible(false);
            return;
        }
        if (this.staticField)
        {
            this.setter.invokeExact(value);
        }
        else
        {
            this.setter.invokeExact(instance,value);
        }
    }
}
