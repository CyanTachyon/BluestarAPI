package me.nullaqua.api.command;

import me.nullaqua.api.command.cmdinfo.Args;
import me.nullaqua.api.command.cmdinfo.Cmd;
import me.nullaqua.api.command.cmdinfo.Label;
import me.nullaqua.api.command.cmdinfo.Sender;
import me.nullaqua.api.reflect.FieldAccessor;
import me.nullaqua.api.reflect.MethodAccessor;
import org.apache.commons.lang.ClassUtils;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

class ArgsMap
{
    private final Map<String,ArgsMap> commandMap=new HashMap<>();
    private ArgsMap commandAny;
    private Run res;
    private String[] resParams;
    private Run any;
    private String[] anyParams;

    public ArgsMap()
    {
    }

    private static CommandExceptions merge(CommandException e1,CommandException e2)
    {
        CommandExceptions e=new CommandExceptions();
        if (e1 instanceof CommandExceptions)
        {
            e.exceptions.addAll(((CommandExceptions) e1).exceptions);
        }
        else if (e1!=null)
        {
            e.exceptions.add(e1);
        }
        if (e2 instanceof CommandExceptions)
        {
            e.exceptions.addAll(((CommandExceptions) e2).exceptions);
        }
        else if (e2!=null)
        {
            e.exceptions.add(e2);
        }
        return e;
    }

    private ArgsMap commandAny()
    {
        if (commandAny==null)
        {
            commandAny=new ArgsMap();
        }
        return commandAny;
    }

    void add(Run run)
    {
        //若干个连续的空格分割
        add(run.parse().split(" +"),run);
    }

    void add(String[] args,Run run)
    {
        add(args,run,0);
    }

    void add(String[] args,Run run,int index)
    {
        //System.err.println("add "+Arrays.toString(args)+" "+run+" "+index);
        if (args.length==index)
        {
            this.res=run;
            this.resParams=args;
            return;
        }
        String arg=args[index];
        if (arg.equals("..."))
        {
            this.any=run;
            this.anyParams=Arrays.copyOfRange(args,0,index);
            return;
        }
        if (arg.startsWith("<")&&arg.endsWith(">")||arg.startsWith("[")&&arg.endsWith("]"))
        {
            commandAny().add(args,run,index+1);
        }
        else
        {
            var map=this.commandMap.get(arg);
            if (map==null)
            {
                map=new ArgsMap();
                this.commandMap.put(arg.toLowerCase(Locale.ENGLISH),map);
            }
            map.add(args,run,index+1);
        }
        if (arg.startsWith("[")&&arg.endsWith("]"))
        {
            int i;
            for (i=index+1;i<args.length;i++)
            {
                if (!args[i].startsWith("[")||!args[i].endsWith("]")) break;
            }
            //新args为0到index-1的参数加上i到args.length的参数
            String[] newArgs=new String[index+args.length-i];
            System.arraycopy(args,0,newArgs,0,index);
            System.arraycopy(args,i,newArgs,index,args.length-i);
            this.add(newArgs,run,index);
        }
    }

    Object run(@NotNull CommandSender sender,@NotNull Command command,@NotNull String label,@NotNull String[] args) throws
            CommandException
    {
        return run(sender,command,label,args,0);
    }

    Object run(@NotNull CommandSender sender,@NotNull Command command,@NotNull String label,@NotNull String[] args,
               int index) throws
            CommandException
    {
        CommandException exceptions=null;
        if (args.length==index)
        {
            if (res!=null)
            {
                try
                {
                    return tryRun(sender,command,label,args,res,resParams);
                }
                catch (CommandException e)
                {
                    exceptions=e;
                }
            }
            if (any!=null)
            {
                try
                {
                    return tryRun(sender,command,label,args,any,anyParams);
                }
                catch (CommandException e)
                {
                    exceptions=merge(exceptions,e);
                }
            }
            if (exceptions==null) exceptions=new CommandCanNotMatchException();
            throw exceptions;
        }
        var map=this.commandMap.get(args[index].toLowerCase(Locale.ENGLISH));
        if (map!=null)
        {
            try
            {
                return map.run(sender,command,label,args,index+1);
            }
            catch (CommandException e)
            {
                exceptions=merge(exceptions,e);
            }
        }
        try
        {
            return commandAny().run(sender,command,label,args,index+1);
        }
        catch (CommandException e)
        {
            exceptions=merge(exceptions,e);
        }
        if (any!=null)
        {
            try
            {
                return tryRun(sender,command,label,args,any,anyParams);
            }
            catch (CommandException e)
            {
                exceptions=merge(exceptions,e);
            }
        }
        if (exceptions==null) exceptions=new CommandCanNotMatchException();
        throw exceptions;
    }

    Object tryRun(@NotNull CommandSender sender,@NotNull Command command,@NotNull String label,@NotNull String[] args
            ,@NotNull Run run,@NotNull String[] format) throws
            CommandException
    {
        Map<String,String> map=new HashMap<>();
        for (int i=0;i<Math.min(args.length,format.length);i++)
        {
            if (format[i].startsWith("<")&&format[i].endsWith(">")||format[i].startsWith("[")&&format[i].endsWith("]"))
            {
                //key为<>或[]中间的内容,去除第一个字符和最后一个字符
                String key=format[i].substring(1,format[i].length()-1);
                if (!key.isEmpty()) map.put(key,args[i]);
            }
            else if (format[i].equals("..."))
            {
                map.put("...",String.join(" ",Arrays.copyOfRange(args,i,args.length)));
            }
            else if (!format[i].equals(args[i])) throw new CommandCanNotMatchException();
        }
        if (run.permission!=null&&!sender.hasPermission(run.permission))
        {
            throw new CommandException(run,"The sender doesn't has permission");
        }
        if (run.only!=null&&run.only.length!=0)
        {
            boolean flag=false;
            for (Class<?> c: run.only)
            {
                if (c.isInstance(sender))
                {
                    flag=true;
                    break;
                }
            }
            if (!flag) throw new CommandException(run,"The sender type does not meet the requirements");
        }
        Object[] params=getArgs(sender,command,label,args,run,map);
        return run.invoke(params);
    }

    //get args

    private Object[] getArgs(CommandSender sender,Command command,String label,String[] args,Run r,
                             Map<String,String> map)
    {
        Object[] params=new Object[r.getParameters().length];
        int i=0;
        for (var param: r.getParameters())
        {
            var fromCommand=param.getAnnotation(Get.class);
            String key=fromCommand!=null?fromCommand.value():"";
            //System.err.println("getArgs "+param+" "+key+" "+map.get(key));
            if (param.getAnnotation(Sender.class)!=null)
            {
                var o=saveCase(param.getType(),sender);
                if (o!=null)
                {
                    params[i]=o;
                }
                else
                {
                    params[i]=getArg(sender.toString(),param.getType());
                }
            }
            else if (param.getAnnotation(Cmd.class)!=null)
            {
                var o=saveCase(param.getType(),command);
                if (o!=null)
                {
                    params[i]=o;
                }
                else
                {
                    params[i]=getArg(command.toString(),param.getType());
                }
            }
            else if (param.getAnnotation(Label.class)!=null)
            {
                var o=saveCase(param.getType(),label);
                if (o!=null)
                {
                    params[i]=o;
                }
                else
                {
                    params[i]=getArg(label,param.getType());
                }
            }
            else if (param.getAnnotation(Args.class)!=null)
            {
                if (param.getType().isArray())
                {
                    var arr=Array.newInstance(param.getType().getComponentType(),args.length);
                    for (int j=0;j<args.length;j++)
                    {
                        Array.set(arr,j,getArg(args[j],param.getType().getComponentType()));
                    }
                    params[i]=arr;
                }
                else if (param.getType().isAssignableFrom(List.class))
                {
                    params[i]=Arrays.asList(args);
                }
                else if (param.getType().isAssignableFrom(Set.class))
                {
                    params[i]=new HashSet<>(Arrays.asList(args));
                }
                else
                {
                    params[i]=getArg(String.join(" ",args),param.getType());
                }
            }
            else if (fromCommand!=null)
            {
                if (key.equals("..."))
                {
                    var s=map.get("...").split(" ");
                    if (param.getType().isArray())
                    {
                        var arr=Array.newInstance(param.getType().getComponentType(),s.length);
                        for (int j=0;j<s.length;j++)
                        {
                            Array.set(arr,j,getArg(s[j],param.getType().getComponentType()));
                        }
                        params[i]=arr;
                    }
                    else if (param.getType().isAssignableFrom(List.class))
                    {
                        params[i]=Arrays.asList(s);
                    }
                    else if (param.getType().isAssignableFrom(Set.class))
                    {
                        params[i]=new HashSet<>(Arrays.asList(s));
                    }
                    else
                    {
                        params[i]=getArg(map.get("..."),param.getType());
                    }
                }
                else
                {
                    params[i]=getArg(map.get(key),param.getType());
                }
            }
            else
            {
                params[i]=getArg(null,param.getType());
            }
            ++i;
        }
        return params;
    }

    private <T> T saveCase(Class<T> c,Object o)
    {
        try
        {
            return c.cast(o);
        }
        catch (ClassCastException e)
        {
            return null;
        }
    }

    private Object getArg(String arg,Class<?> c)
    {
        if (arg==null)
        {
            //如果c为基本类型，那么就返回-1
            if (c.isPrimitive())
            {
                if (c==int.class)
                {
                    return -1;
                }
                else if (c==long.class)
                {
                    return -1L;
                }
                else if (c==short.class)
                {
                    return (short) -1;
                }
                else if (c==byte.class)
                {
                    return (byte) -1;
                }
                else if (c==float.class)
                {
                    return -1F;
                }
                else if (c==double.class)
                {
                    return -1D;
                }
                else if (c==boolean.class)
                {
                    return false;
                }
                else if (c==char.class) return '\0';
            }
            return null;
        }
        if (c.isArray())
        {
            var args=arg.split("/");
            var array=Array.newInstance(c.getComponentType(),args.length);
            for (int i=0;i<args.length;++i)
            {
                Array.set(array,i,getArg(args[i],c.getComponentType()));
            }
            return array;
        }
        else if (c.isAssignableFrom(List.class))
        {
            var args=arg.split("/");
            var list=new ArrayList<>();
            for (var s: args)
            {
                list.add(getArg(s,c.getComponentType()));
            }
            return list;
        }
        else if (c.isAssignableFrom(Set.class))
        {
            var args=arg.split("/");
            var set=new HashSet<>();
            for (var s: args)
            {
                set.add(getArg(s,c.getComponentType()));
            }
            return set;
        }
        //如果是基本类型，那么就转换为包装类型
        if (c.isPrimitive())
        {
            Object o=getArg(arg,ClassUtils.primitiveToWrapper(c));
            if (o==null) return getArg(null,c);
            return o;
        }
        if (c==String.class)
        {
            return arg;
        }
        //如果是基础类型的包装类型,从字符串中解析,并防止抛出错误
        else if (c==Integer.class)
        {
            try
            {
                return Integer.parseInt(arg);
            }
            catch (NumberFormatException e)
            {
                return null;
            }
        }
        else if (c==Long.class)
        {
            try
            {
                return Long.parseLong(arg);
            }
            catch (NumberFormatException e)
            {
                return null;
            }
        }
        else if (c==Short.class)
        {
            try
            {
                return Short.parseShort(arg);
            }
            catch (NumberFormatException e)
            {
                return null;
            }
        }
        else if (c==Byte.class)
        {
            try
            {
                return Byte.parseByte(arg);
            }
            catch (NumberFormatException e)
            {
                return null;
            }
        }
        else if (c==Float.class)
        {
            try
            {
                return Float.parseFloat(arg);
            }
            catch (NumberFormatException e)
            {
                return null;
            }
        }
        else if (c==Double.class)
        {
            try
            {
                return Double.parseDouble(arg);
            }
            catch (NumberFormatException e)
            {
                return null;
            }
        }
        else if (c==Boolean.class)
        {
            return "true".equalsIgnoreCase(arg);
        }
        else if (c==Character.class)
        {
            return arg.charAt(0);
        }
        else if (c==Player.class||c==OfflinePlayer.class)
        {
            OfflinePlayer offlinePlayer;
            try
            {
                UUID uuid=UUID.fromString(arg);
                offlinePlayer=Bukkit.getOfflinePlayer(uuid);
            }
            catch (IllegalArgumentException e)
            {
                offlinePlayer=Bukkit.getOfflinePlayer(arg);
            }
            Player player=offlinePlayer.getPlayer();
            return player!=null?player:(c==Player.class?null:offlinePlayer);
        }
        else if (World.class.isAssignableFrom(c))
        {
            try
            {
                return saveCase(c,Bukkit.getWorld(UUID.fromString(arg)));
            }
            catch (IllegalArgumentException e)
            {
                return saveCase(c,Bukkit.getWorld(arg));
            }
        }
        else if (Entity.class.isAssignableFrom(c))
        {
            try
            {
                return saveCase(c,Bukkit.getEntity(UUID.fromString(arg)));
            }
            catch (IllegalArgumentException e)
            {
                return null;
            }
        }
        else if (c==UUID.class)
        {
            try
            {
                return UUID.fromString(arg);
            }
            catch (IllegalArgumentException e)
            {
                return null;
            }
        }
        else if (c==Material.class)
        {
            return Material.matchMaterial(arg);
        }
        else if (c==Location.class)
        {
            String[] split=arg.split("/");
            World world=null;
            if (split.length==4)
            {
                try
                {
                    world=Bukkit.getWorld(UUID.fromString(split[0]));
                    if (world==null) throw new IllegalArgumentException();
                }
                catch (IllegalArgumentException e)
                {
                    world=Bukkit.getWorld(split[0]);
                }
            }
            else if (split.length!=3) return null;
            try
            {
                double x=Double.parseDouble(split[1]);
                double y=Double.parseDouble(split[2]);
                double z=Double.parseDouble(split[3]);
                return new Location(world,x,y,z);
            }
            catch (NumberFormatException e)
            {
                return null;
            }
        }
        else if (c==EntityType.class)
        {
            return EntityType.fromName(arg);
        }
        else if (c==Enchantment.class)
        {
            return Enchantment.getByName(arg);
        }
        else if (c==PotionEffectType.class)
        {
            return PotionEffectType.getByName(arg);
        }
        else if (c==Color.class)
        {
            String[] split=arg.split("/");
            if (split.length!=3)
            {
                return null;
            }
            try
            {
                int r=Integer.parseInt(split[0]);
                int g=Integer.parseInt(split[1]);
                int b=Integer.parseInt(split[2]);
                return Color.fromRGB(r,g,b);
            }
            catch (NumberFormatException e)
            {
                return null;
            }
        }
        else if (c==Particle.class)
        {
            return Particle.valueOf(arg);
        }
        else if (c==Sound.class)
        {
            return Sound.valueOf(arg);
        }
        else if (c==BlockFace.class)
        {
            return BlockFace.valueOf(arg);
        }
        else if (c==DyeColor.class)
        {
            return DyeColor.valueOf(arg);
        }
        else if (c.isEnum())
        {
            for (Object o: c.getEnumConstants())
            {
                if (o.toString().equalsIgnoreCase(arg)) return o;
            }
            return null;
        }
        else
        {
            return null;
        }
    }

    protected static class Run
    {
        final MethodAccessor method;
        final FieldAccessor field;
        final String format;
        final String permission;
        final Class<?>[] only;
        final boolean isTab;
        final Object instance;

        public Run(Method method,String format,String permission,Class<?>[] only,boolean isTab,Object instance)
        {
            this.method=new MethodAccessor(method);
            this.instance=instance;
            this.field=null;
            this.format=parseFormat(format);
            this.permission=permission;
            this.only=only;
            this.isTab=isTab;
        }

        public Run(Field field,String format,String permission,Class<?>[] only,boolean isTab,Object instance)
        {
            this.instance=instance;
            this.method=null;
            this.field=new FieldAccessor(field);
            this.format=parseFormat(format);
            this.permission=permission;
            this.only=only;
            this.isTab=isTab;
        }

        static String parseFormat(String s)
        {
            return String.join(" ",s.trim().split(" +"));
        }

        public static Run[] of(Method method,ParseCommand[] x,Object instance,String p)
        {
            var res=new Run[x.length];
            for (int i=0;i<x.length;i++)
            {
                res[i]=new Run(method,p+" "+x[i].value(),x[i].permission(),x[i].only(),false,instance);
            }
            return res;
        }

        public static Run[] of(Method field,ParseTab[] x,Object instance,String p)
        {
            var res=new Run[x.length];
            for (int i=0;i<x.length;i++)
            {
                res[i]=new Run(field,p+" "+x[i].value(),x[i].permission(),x[i].only(),true,instance);
            }
            return res;
        }

        public static Run[] of(Field field,ParseTab[] x,Object instance,String p)
        {
            var res=new Run[x.length];
            for (int i=0;i<x.length;i++)
            {
                res[i]=new Run(field,p+" "+x[i].value(),x[i].permission(),x[i].only(),true,instance);
            }
            return res;
        }

        public static Run[] of(Field field,ParseCommand[] x,Object instance,String p)
        {
            var res=new Run[x.length];
            for (int i=0;i<x.length;i++)
            {
                res[i]=new Run(field,p+" "+x[i].value(),x[i].permission(),x[i].only(),false,instance);
            }
            return res;
        }

        public Parameter[] getParameters()
        {
            if (method!=null)
            {
                return method.getMethod().getParameters();
            }
            else
            {
                return new Parameter[0];
            }
        }

        public Object invoke(Object... args) throws CommandException
        {
            try
            {
                if (method!=null)
                {
                    return method.invoke(instance,args);
                }
                else if (field!=null) return field.get(instance);
                return null;
            }
            catch (Throwable e)
            {
                throw new CommandException(this,e);
            }
        }

        public boolean isMethod()
        {
            return method!=null;
        }

        public boolean isField()
        {
            return field!=null;
        }

        @Override
        public int hashCode()
        {
            return method!=null?method.hashCode():(field!=null?field.hashCode():0);
        }

        @Override
        public String toString()
        {
            return method!=null?method.getMethod().toString():(field!=null?field.getField().toString():"");
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof Run)
            {
                Run run=(Run) obj;
                return method!=null?method.equals(run.method):(field!=null&&field.equals(run.field));
            }
            return false;
        }

        public String parse()
        {
            return format;
        }

        public String permission()
        {
            return permission;
        }

        public Class<?>[] only()
        {
            return only;
        }
    }

    static class CommandException extends Exception
    {
        public CommandException(Run run,Throwable cause)
        {
            super(run!=null?run.toString():"global",cause);
        }

        public CommandException(Run run,String message)
        {
            super(run!=null?run.toString():"global"+": "+message);
        }

        public CommandException(String message)
        {
            this(null,message);
        }

        public CommandException(Throwable cause)
        {
            this(null,cause);
        }

        public CommandExceptions toCommandExceptions()
        {
            if (this instanceof CommandExceptions)
            {
                return (CommandExceptions) this;
            }
            return merge(null,this);
        }
    }

    static class CommandCanNotMatchException extends CommandException
    {
        public CommandCanNotMatchException()
        {
            super("Can't match command");
        }
    }

    //由诸多命令中的错误构成的异常
    static class CommandExceptions extends CommandException
    {
        Set<CommandException> exceptions=new HashSet<>();

        public CommandExceptions()
        {
            super("There are some errors in the command");
        }

        @Override
        public void printStackTrace()
        {
            printStackTrace(System.err);
        }

        @Override
        public void printStackTrace(PrintWriter s)
        {
            s.println("There are "+exceptions.size()+" errors in the command");
            for (var e: exceptions)
            {
                s.println("Caused by: "+e.getMessage());
            }
        }

        @Override
        public void printStackTrace(PrintStream s)
        {
            printStackTrace(new PrintWriter(s));
        }

        public boolean print()
        {
            if (exceptions.isEmpty())
            {
                System.err.println("Unknown error");
                return false;
            }
            System.err.println("There are "+exceptions.size()+" errors in the command");
            for (var e: exceptions)
            {
                System.err.println("Caused by: "+e.getMessage());
            }
            return true;
        }
    }
}