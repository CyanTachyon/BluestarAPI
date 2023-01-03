package me.lanzhi.api.reflect.classedit;

import javassist.*;
import me.lanzhi.api.reflect.ClassLoaderAccessor;
import me.lanzhi.api.reflect.ReflectAccessor;

import java.util.UUID;

public class ClassBuilder
{
    private final CtClass ctClass;
    private static final String tempClassesPackage=ClassBuilder.class.getPackageName()+".temp";
    private Class<?> toClass;

    static
    {
        ClassPool.getDefault().appendClassPath(new LoaderClassPath(ClassBuilder.class.getClassLoader()));
        ClassPool.getDefault().appendClassPath(new LoaderClassPath(System.class.getClassLoader()));
    }

    public ClassBuilder(ClassType type,String className)
    {
        final var pool=ClassPool.getDefault();
        switch (type)
        {
            case INTERFACE:
            {
                ctClass=pool.makeInterface(className);
                break;
            }
            case ANNOTATION:
            {
                ctClass=pool.makeAnnotation(className);
                break;
            }
            case TYPE:
            default:
            {
                ctClass=pool.makeClass(className);
            }
        }
    }

    public static ClassBuilder create(ClassType type,String className)
    {
        return new ClassBuilder(type,className);
    }

    public ClassBuilder modifier(int modifier)
    {
        ctClass.setModifiers(modifier);
        return this;
    }

    public int modifier()
    {
        return ctClass.getModifiers();

    }

    public ClassBuilder addModifier(int modifier)
    {
        ctClass.setModifiers(ctClass.getModifiers()|modifier);
        return this;
    }

    public ClassBuilder removeModifier(int modifier)
    {
        ctClass.setModifiers(ctClass.getModifiers()&~modifier);
        return this;
    }

    public static void runCode(String code) throws Throwable
    {
        var x=TempClass.create(code);
        x.run();
    }

    public static void runCode(String code,ClassLoader classLoader) throws Throwable
    {
        var x=TempClass.create(code);
        x.run(classLoader);
    }

    private static String randomClassName()
    {
        return UUID.randomUUID().toString().replace("-","");
    }

    private static String createTempClassName(String name)
    {
        return tempClassesPackage+"."+name;
    }

    public ClassBuilder superClass(Class<?> superClass) throws CannotCompileException
    {
        ctClass.setSuperclass(toCtClass(superClass));
        return this;
    }

    protected static CtClass toCtClass(Class<?> type)
    {
        if (type==null||type==void.class)
        {
            return CtClass.voidType;
        }
        CtClass ctClass=ClassPool.getDefault().getOrNull(type.getName());
        if (ctClass==null)
        {
            ClassPool.getDefault().appendClassPath(new ClassClassPath(type));
            ctClass=ClassPool.getDefault().getOrNull(type.getName());
        }
        return ctClass;
    }

    public ClassBuilder superClass(CtClass superClass) throws CannotCompileException
    {
        ctClass.setSuperclass(superClass);
        return this;
    }

    public ClassBuilder clearImplement()
    {
        ctClass.setInterfaces(new CtClass[0]);
        return this;
    }

    public ClassBuilder addImplement(Object... interfaces)
    {
        for (var i: toCtClass(interfaces))
        {
            ctClass.addInterface(i);
        }
        return this;
    }

    protected static CtClass[] toCtClass(Object... parameters)
    {
        CtClass[] ctClasses=new CtClass[parameters.length];
        for (int i=0;i<parameters.length;i++)
        {
            ctClasses[i]=toCtClass(parameters[i]);
        }
        return ctClasses;
    }

    protected static CtClass toCtClass(Object type)
    {
        if (type instanceof CtClass)
        {
            return (CtClass) type;
        }
        else if (type instanceof Class)
        {
            return toCtClass((Class<?>) type);
        }
        else if (type instanceof ClassBuilder)
        {
            return ((ClassBuilder) type).ctClass;
        }
        else if (type instanceof String)
        {
            CtClass ctClass1=ClassPool.getDefault().getOrNull((String) type);
            if (ctClass1!=null)
            {
                return ctClass1;
            }
            Class<?> c=ReflectAccessor.getClass((String) type);
            if (c!=null)
            {
                return toCtClass(c);
            }
        }
        return CtClass.voidType;
    }

    public ClassBuilder implement(Object... interfaces)
    {
        ctClass.setInterfaces(toCtClass(interfaces));
        return this;
    }

    public ConstructorBuilder createConstructor(Object... parameters)
    {
        return createConstructor(null,parameters);
    }

    public ConstructorBuilder createConstructor(String body,Object... parameters)
    {
        CtClass[] ctClasses=toCtClass(parameters);
        try
        {
            CtConstructor ctConstructor=new CtConstructor(ctClasses,ctClass);
            ctClass.addConstructor(ctConstructor);
            if (body!=null)
            {
                ctConstructor.setBody(body);
            }
            return new ConstructorBuilder(ctConstructor);
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    public Class<?> build()
    {
        return build(this.getClass().getClassLoader());
    }

    public FieldBuilder createField(String name,Object type) throws CannotCompileException
    {
        if (type==null||type==void.class)
        {
            throw new IllegalArgumentException("type cannot be null or void");
        }
        CtClass ctClass1=toCtClass(type);
        CtField ctField=new CtField(ctClass1,name,ctClass);
        ctClass.addField(ctField);
        return new FieldBuilder(ctField);
    }

    public MethodBuilder createMethod(Object returnType,String name,Object... parameterTypes) throws CannotCompileException
    {
        return createMethod(returnType,name,";",parameterTypes);
    }

    public MethodBuilder createMethod(Object returnType,String name,String body,Object... parameters) throws CannotCompileException
    {
        var ctClasses=toCtClass(parameters);
        try
        {
            CtMethod ctMethod=new CtMethod(toCtClass(returnType),name,ctClasses,ctClass);
            if (body!=null)
            {
                ctMethod.setBody(body);
            }
            ctClass.addMethod(ctMethod);
            return new MethodBuilder(ctMethod);
        }
        catch (CannotCompileException e)
        {
            throw e;
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    public MethodBuilder createAbstractMethod(Object returnType,String name,Object... parameters)
    {
        var ctClasses=toCtClass(parameters);
        try
        {
            CtMethod ctMethod=new CtMethod(toCtClass(returnType),name,ctClasses,ctClass);
            ctClass.addMethod(ctMethod);
            return new MethodBuilder(ctMethod).addModifier(Modifier.ABSTRACT);
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    public Class<?> build(ClassLoader loader)
    {
        if (toClass!=null)
        {
            return toClass;
        }
        try
        {
            var code=ctClass.toBytecode();
            var access=ClassLoaderAccessor.of(loader);
            return toClass=access.defineClass(ctClass.getName(),code,0,code.length);
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    public enum ClassType
    {
        TYPE,
        INTERFACE,
        ANNOTATION
    }

    public interface TempClass
    {
        public static TempClass create(String code) throws Throwable
        {
            return create(randomClassName(),code);
        }

        public static TempClass create(String name,String code) throws Throwable
        {
            if (name==null)
            {
                return create(code);
            }
            var builder=new ClassBuilder(ClassType.TYPE,createTempClassName(name)).implement(TempClass.class);
            builder.createMethod(void.class,"run","{"+code+"}");
            return builder.build(new ClassLoader(){})
                          .asSubclass(TempClass.class)
                          .getDeclaredConstructor()
                          .newInstance();
        }

        default void run(ClassLoader classLoader)
        {
            if (classLoader!=null)
            {
                Thread.currentThread().setContextClassLoader(classLoader);
            }
            run();
        }

        void run();
    }
}
