package me.nullaqua.api.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static me.nullaqua.api.reflect.ReflectionAccessor.*;

public final class FieldAccessor
{
    private final static MethodAccessor getFields = MethodAccessor.getMethod(Class.class, "getDeclaredFields0", boolean.class);
    private final Field field;
    private final boolean staticField;
    private final MethodHandle setter;
    private final MethodHandle getter;

    public FieldAccessor(Field field) throws NoSuchFieldException
    {
        if (field==null)
        {
            this.field=null;
            this.setter=null;
            this.getter=null;
            this.staticField=false;
            check();
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
        check();
    }

    private void check() throws NoSuchFieldException
    {
        if (field == null && getter == null && setter == null)
        {
            throw new NoSuchFieldException("FieldAccessor is invalid");
        }
    }

    public static FieldAccessor getField(Class<?> c,String field)
    {
        if (c==null||field==null)
        {
            return null;
        }
        try
        {
            final var fields = (Field[]) getFields.invokeMethod(c,false);
            assert fields != null;
            for (Field f: fields)
            {
                if (f.getName().equals(field))
                {
                    return new FieldAccessor(f);
                }
            }
            return null;
        }
        catch (Throwable e)
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
                return getField(clazz,field);
            }
            catch (Throwable ignored)
            {
            }
        }
        return null;
    }

    public static List<FieldAccessor> getFieldsInSuperClasses(Object o) throws Throwable
    {
        if (o==null)
        {
            return new ArrayList<>();
        }
        return getFieldsInSuperClasses(o.getClass());
    }

    public static List<FieldAccessor> getFieldsInSuperClasses(Class<?> type) throws Throwable
    {
        List<FieldAccessor> fields=new ArrayList<>();
        for (Class<?> c: getAllSuperClass(type))
        {
            fields.addAll(getFields(c));
        }
        return fields;
    }
    
    public static List<FieldAccessor> getFields(Class<?> type) throws Throwable
    {
        List<FieldAccessor> fields=new ArrayList<>();
        for (var field: (Field[]) Objects.requireNonNull(getFields.invokeMethod(type, false)))
        {
            fields.add(new FieldAccessor(field));
        }
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
        if (getter==null && UNSAFE == null)
        {
            ReflectionAccessor.checkVisibility(field.getDeclaringClass());
            field.setAccessible(true);
            Object o=field.get(instance);
            field.setAccessible(false);
            return o;
        }
        if (getter==null)
        {
            if (this.staticField)
            {
                if (field.getType() == boolean.class) return UNSAFE.getBoolean(null, UNSAFE.staticFieldOffset(field));
                if (field.getType() == byte.class) return UNSAFE.getByte(null, UNSAFE.staticFieldOffset(field));
                if (field.getType() == char.class) return UNSAFE.getChar(null, UNSAFE.staticFieldOffset(field));
                if (field.getType() == short.class) return UNSAFE.getShort(null, UNSAFE.staticFieldOffset(field));
                if (field.getType() == int.class) return UNSAFE.getInt(null, UNSAFE.staticFieldOffset(field));
                if (field.getType() == long.class) return UNSAFE.getLong(null, UNSAFE.staticFieldOffset(field));
                if (field.getType() == float.class) return UNSAFE.getFloat(null, UNSAFE.staticFieldOffset(field));
                if (field.getType() == double.class) return UNSAFE.getDouble(null, UNSAFE.staticFieldOffset(field));
                return UNSAFE.getObject(null, UNSAFE.staticFieldOffset(field));
            }
            else
            {
                if (field.getType() == boolean.class) return UNSAFE.getBoolean(instance, UNSAFE.objectFieldOffset(field));
                if (field.getType() == byte.class) return UNSAFE.getByte(instance, UNSAFE.objectFieldOffset(field));
                if (field.getType() == char.class) return UNSAFE.getChar(instance, UNSAFE.objectFieldOffset(field));
                if (field.getType() == short.class) return UNSAFE.getShort(instance, UNSAFE.objectFieldOffset(field));
                if (field.getType() == int.class) return UNSAFE.getInt(instance, UNSAFE.objectFieldOffset(field));
                if (field.getType() == long.class) return UNSAFE.getLong(instance, UNSAFE.objectFieldOffset(field));
                if (field.getType() == float.class) return UNSAFE.getFloat(instance, UNSAFE.objectFieldOffset(field));
                if (field.getType() == double.class) return UNSAFE.getDouble(instance, UNSAFE.objectFieldOffset(field));
                return UNSAFE.getObject(instance, UNSAFE.objectFieldOffset(field));
            }
        }
        if (this.staticField) return this.getter.invokeExact();
        else return this.getter.invokeExact(instance);
    }

    public void set(Object instance,Object value) throws Throwable
    {
        if (setter==null && UNSAFE == null)
        {
            ReflectionAccessor.checkVisibility(field.getDeclaringClass());
            field.setAccessible(true);
            field.set(instance,value);
            field.setAccessible(false);
            return;
        }
        if (setter == null)
        {
            if (this.staticField)
            {
                if (field.getType() == boolean.class) UNSAFE.putBoolean(null, UNSAFE.staticFieldOffset(field), (boolean) value);
                else if (field.getType() == byte.class) UNSAFE.putByte(null, UNSAFE.staticFieldOffset(field), (byte) value);
                else if (field.getType() == char.class) UNSAFE.putChar(null, UNSAFE.staticFieldOffset(field), (char) value);
                else if (field.getType() == short.class) UNSAFE.putShort(null, UNSAFE.staticFieldOffset(field), (short) value);
                else if (field.getType() == int.class) UNSAFE.putInt(null, UNSAFE.staticFieldOffset(field), (int) value);
                else if (field.getType() == long.class) UNSAFE.putLong(null, UNSAFE.staticFieldOffset(field), (long) value);
                else if (field.getType() == float.class) UNSAFE.putFloat(null, UNSAFE.staticFieldOffset(field), (float) value);
                else if (field.getType() == double.class) UNSAFE.putDouble(null, UNSAFE.staticFieldOffset(field), (double) value);
                else UNSAFE.putObject(null, UNSAFE.staticFieldOffset(field), value);
            }
            else
            {
                if (field.getType() == boolean.class) UNSAFE.putBoolean(instance, UNSAFE.objectFieldOffset(field), (boolean) value);
                else if (field.getType() == byte.class) UNSAFE.putByte(instance, UNSAFE.objectFieldOffset(field), (byte) value);
                else if (field.getType() == char.class) UNSAFE.putChar(instance, UNSAFE.objectFieldOffset(field), (char) value);
                else if (field.getType() == short.class) UNSAFE.putShort(instance, UNSAFE.objectFieldOffset(field), (short) value);
                else if (field.getType() == int.class) UNSAFE.putInt(instance, UNSAFE.objectFieldOffset(field), (int) value);
                else if (field.getType() == long.class) UNSAFE.putLong(instance, UNSAFE.objectFieldOffset(field), (long) value);
                else if (field.getType() == float.class) UNSAFE.putFloat(instance, UNSAFE.objectFieldOffset(field), (float) value);
                else if (field.getType() == double.class) UNSAFE.putDouble(instance, UNSAFE.objectFieldOffset(field), (double) value);
                else UNSAFE.putObject(instance, UNSAFE.objectFieldOffset(field), value);
            }
            return;
        }
        if (this.staticField) this.setter.invokeExact(value);
        else this.setter.invokeExact(instance,value);
    }
}
