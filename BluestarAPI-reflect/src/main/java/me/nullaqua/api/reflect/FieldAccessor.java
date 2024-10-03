package me.nullaqua.api.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static me.nullaqua.api.reflect.ReflectionAccessor.*;

public final class FieldAccessor
{
    private final Field field;
    private final boolean staticField;
    private final MethodHandle setter;
    private final MethodHandle getter;

    public static List<FieldAccessor> getFieldsInSuperClasses(Object o)
    {
        if (o==null)
        {
            return new ArrayList<>();
        }
        return getFieldsInSuperClasses(o.getClass());
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
        ReflectionAccessor.checkVisibility(field.getDeclaringClass());
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
        catch (Exception ignored)
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

    /**
     * 在一个类及其父类中查找字段, 若有同名字段, 则子类的字段优先
     * @param c 类
     * @param field 字段名
     * @return 字段访问器, 若不存在则返回null
     */
    public static FieldAccessor getFieldInSuperClasses(Class<?> c,String field)
    {
        if (c==null||field==null)
        {
            return null;
        }
        for (Class<?> clazz: ReflectionAccessor.getAllSuperClass(c))
        {
            try
            {
                return new FieldAccessor(clazz.getDeclaredField(field));
            }
            catch (Exception ignored)
            {
            }
        }
        return null;
    }

    public static List<FieldAccessor> getFieldsInSuperClasses(Class<?> type)
    {
        List<FieldAccessor> fields=new ArrayList<>();
        for (Class<?> c: getAllSuperClass(type))
        {
            fields.addAll(getFields(c));
        }
        return fields;
    }
    
    public static List<FieldAccessor> getFields(Class<?> type)
    {
        List<FieldAccessor> fields=new ArrayList<>();
        List.of(type.getDeclaredFields()).forEach(field -> fields.add(new FieldAccessor(field)));
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
            ReflectionAccessor.checkVisibility(field.getDeclaringClass());
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
            ReflectionAccessor.checkVisibility(field.getDeclaringClass());
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
