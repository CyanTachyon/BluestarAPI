package me.lanzhi.api.command;

import me.lanzhi.api.Bluestar;
import org.apache.commons.lang.ClassUtils;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.*;

public final class CommandHelper
{
    private final CommandHelperExecutor executor=new CommandHelperExecutor();

    public CommandHelper()
    {
    }

    public static PluginCommand fastCreateCommand(JavaPlugin plugin,Class<?> c)
    {
        CommandHelper helper=new CommandHelper();
        helper.add(c);
        var commandName=c.getAnnotation(CommandName.class);
        var aliases=c.getAnnotation(CommandAlias.class);
        if (commandName==null)
        {
            throw new IllegalArgumentException("Class "+
                                               c.getName()+
                                               " don't have annotation "+
                                               CommandName.class.getName());
        }
        return helper.createCommand(plugin,commandName.value(),aliases==null?new String[0]:aliases.value());
    }

    public static PluginCommand fastCreateCommand(JavaPlugin plugin,Object o)
    {
        if (o instanceof Class)
        {
            return fastCreateCommand(plugin,(Class<?>) o);
        }
        CommandHelper helper=new CommandHelper();
        helper.add(o);
        var commandName=o.getClass().getAnnotation(CommandName.class);
        var aliases=o.getClass().getAnnotation(CommandAlias.class);
        if (commandName==null)
        {
            throw new IllegalArgumentException("Class "+
                                               o.getClass().getName()+
                                               " don't have annotation "+
                                               CommandName.class.getName());
        }
        return helper.createCommand(plugin,commandName.value(),aliases==null?new String[0]:aliases.value());
    }

    public static CommandExecutor fastCreateCommandExecutor(Class<?> c)
    {
        return new CommandHelper().add(c).toCommandExecutor();
    }

    public CommandExecutor toCommandExecutor()
    {
        return executor;
    }

    public CommandHelper add(Class<?> clazz)
    {
        executor.add(clazz);
        return this;
    }

    public static CommandExecutor fastCreateCommandExecutor(Object o)
    {
        return new CommandHelper().add(o).toCommandExecutor();
    }

    public CommandHelper add(Object instance)
    {
        executor.add(instance);
        return this;
    }

    private static String[] split(String s)
    {
        return split(s," ");
    }

    private static String[] split(String s,String regex)
    {
        if (s==null)
            return new String[0];
        Vector<String> v=new Vector<>();
        for (var ss: s.split(regex))
        {
            if (ss.isEmpty())
                continue;
            v.add(ss);
        }
        return v.toArray(new String[0]);
    }

    public PluginCommand createCommand(JavaPlugin plugin,String name)
    {
        return createCommand(plugin,name,new String[0]);
    }

    public PluginCommand createCommand(JavaPlugin plugin,String name,String[] aliases)
    {
        Objects.requireNonNull(plugin);
        Objects.requireNonNull(name);
        if (aliases==null)
            aliases=new String[0];
        if (name.isEmpty())
            throw new IllegalArgumentException("Command name can't be empty");
        var command=plugin.getCommand(name);
        if (command==null)
        {
            command=Bluestar.getCommandManager().newPluginCommand(name,plugin);
        }
        if (command==null)
        {
            throw new IllegalArgumentException("Plugin don't have command "+name+",and can't create it");
        }
        command.setAliases(Arrays.asList(aliases));
        command.setExecutor(toCommandExecutor());
        return command;
    }

    private static final class CommandHelperExecutor implements CommandExecutor, TabExecutor
    {
        private final ArgsMap argsMap=new ArgsMap();
        private final ArgsMap tabArgsMap=new ArgsMap();
        private final Map<Run,Object> instances=new HashMap<>();

        private static <T> T saveCase(Class<T> c,Object o)
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

        private void add(Class<?> clazz)
        {
            Objects.requireNonNull(clazz);
            for (var method: clazz.getDeclaredMethods())
            {
                //如果方法不是静态的，那么就不会被添加
                if (!Modifier.isStatic(method.getModifiers()))
                    continue;
                ParseCommand parseCommand=method.getAnnotation(ParseCommand.class);
                ParseTab parseTab=method.getAnnotation(ParseTab.class);
                if (parseCommand!=null)
                    add(Run.of(method,parseCommand),(Object) null);
                if (parseTab!=null)
                    add(Run.of(method,parseTab),(Object) null);
            }
            for (var field: clazz.getDeclaredFields())
            {
                if (!Modifier.isStatic(field.getModifiers()))
                    continue;
                ParseCommand parseCommand=field.getAnnotation(ParseCommand.class);
                ParseTab parseTab=field.getAnnotation(ParseTab.class);
                if (parseCommand!=null)
                    add(Run.of(field,parseCommand),(Object) null);
                if (parseTab!=null)
                    add(Run.of(field,parseTab),(Object) null);
            }
        }

        private void add(Run[] run,Object instance)
        {
            for (var r: run)
            {
                if (r.isTab)
                {
                    tabArgsMap.add(r);
                }
                else
                {
                    argsMap.add(r);
                }
                instances.put(r,instance);
            }
        }

        private void add(Object instance)
        {
            if (instance instanceof Class<?>)
            {
                add((Class<?>) instance);
                return;
            }
            Objects.requireNonNull(instance);
            add(instance,instance.getClass());
        }

        // ----- OnCommand  And  OnTabComplete ----- //

        private void add(Object instance,Class<?> c)
        {
            for (var method: c.getDeclaredMethods())
            {
                //如果方法是静态的，那么就不会被添加
                if (Modifier.isStatic(method.getModifiers()))
                    continue;
                ParseCommand parseCommand=method.getAnnotation(ParseCommand.class);
                ParseTab parseTab=method.getAnnotation(ParseTab.class);
                if (parseCommand!=null)
                    add(Run.of(method,parseCommand),instance);
                if (parseTab!=null)
                    add(Run.of(method,parseTab),instance);
            }
            for (var field: c.getDeclaredFields())
            {
                if (Modifier.isStatic(field.getModifiers()))
                    continue;
                ParseCommand parseCommand=field.getAnnotation(ParseCommand.class);
                ParseTab parseTab=field.getAnnotation(ParseTab.class);
                if (parseCommand!=null)
                    add(Run.of(field,parseCommand),instance);
                if (parseTab!=null)
                    add(Run.of(field,parseTab),instance);
            }
            if (c.getSuperclass()!=Object.class&&c.getSuperclass()!=null)
                add(instance,c.getSuperclass());
        }

        @Override
        public boolean onCommand(@NotNull CommandSender sender,@NotNull Command command,@NotNull String label,
                                 String[] args)
        {
            var run=argsMap.get(args,sender);
            if (run==null)
            {
                sender.sendMessage(ChatColor.RED+"Unknown command");
                return true;
            }
            String[] formArgs=split(run.parse());
            var map=ArgsMap.mapArgs(args,formArgs);
            var params=getArgs(sender,command,label,args,run,map);
            Object obj;
            try
            {
                obj=run.invoke(instances.get(run),params);
            }
            catch (IllegalAccessException|InvocationTargetException e)
            {
                sender.sendMessage(ChatColor.RED+"An error occurred while executing this command");
                e.printStackTrace();
                return true;
            }
            if (obj instanceof Boolean)
            {
                return (Boolean) obj;
            }
            else if (obj==null)
            {
                return true;
            }
            else
            {
                toStrings(obj).forEach(sender::sendMessage);
                return true;
            }
        }

        private List<String> toStrings(Object o)
        {
            if (o instanceof Collection)
            {
                List<String> list=new ArrayList<>();
                for (var obj: (Collection<?>) o)
                {
                    list.addAll(toStrings(obj));
                }
                return list;
            }
            else if (o!=null&&o.getClass().isArray())
            {
                List<String> list=new ArrayList<>();
                for (int i=0;i<Array.getLength(o);i++)
                {
                    list.addAll(toStrings(Array.get(o,i)));
                }
                return list;
            }
            else if (o!=null)
            {
                var list=new ArrayList<String>();
                list.add(o.toString());
                return list;
            }
            else
            {
                return new ArrayList<>();
            }
        }
        // --OnCommand And OnTabComplete--End-- //

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender,@NotNull Command command,
                                          @NotNull String alias,String[] args)
        {
            var run=tabArgsMap.get(args,sender);
            if (run==null)
            {
                return Collections.emptyList();
            }
            String[] formArgs=split(run.parse());
            var map=ArgsMap.mapArgs(args,formArgs);
            var params=getArgs(sender,command,alias,args,run,map);
            Object obj;
            try
            {
                obj=run.invoke(instances.get(run),params);
            }
            catch (IllegalAccessException|InvocationTargetException e)
            {
                e.printStackTrace();
                return Collections.emptyList();
            }
            return toStrings(obj);
        }

        private Object[] getArgs(CommandSender sender,Command command,String label,String[] args,Run method,
                                 Map<String,String> map)
        {
            Object[] params=new Object[method.getParameters().length];
            int i=0;
            for (var param: method.getParameters())
            {
                if (param.getType()==CommandSender.class)
                {
                    params[i]=sender;
                }
                else if (param.getType()==Command.class)
                {
                    params[i]=command;
                }
                else if (param.getType()==String[].class)
                {
                    params[i]=args;
                }
                else
                {
                    var fromCommand=param.getAnnotation(Get.class);
                    String form=fromCommand!=null?fromCommand.value():null;
                    params[i]=getArg(map.get(form),param.getType());
                }
                if (param.getType()==String.class&&params[i]==null&&param.getAnnotation(Get.class)==null)
                {
                    params[i]=label;
                }
                ++i;
            }
            return params;
        }

        private Object getArg(String arg,Class<?> c)
        {
            if (arg==null)
            {
                //如果c为基本类型，那么就返回-1
                if (c.isPrimitive())
                {
                    if (c==int.class)
                        return -1;
                    else if (c==long.class)
                        return -1L;
                    else if (c==short.class)
                        return (short) -1;
                    else if (c==byte.class)
                        return (byte) -1;
                    else if (c==float.class)
                        return -1F;
                    else if (c==double.class)
                        return -1D;
                    else if (c==boolean.class)
                        return false;
                    else if (c==char.class)
                        return '\0';
                }
                return null;
            }
            if (c.isArray())
            {
                var args=split(arg);
                if (args.length<=1)
                    args=split(arg,",");
                var array=Array.newInstance(c.getComponentType(),args.length);
                for (int i=0;i<args.length;++i)
                {
                    Array.set(array,i,getArg(args[i],c.getComponentType()));
                }
                return array;
            }
            else if (c.isAssignableFrom(List.class))
            {
                var args=split(arg);
                if (args.length<=1)
                    args=split(arg,",");
                var list=new ArrayList<>();
                for (var s: args)
                {
                    list.add(getArg(s,c.getComponentType()));
                }
                return list;
            }
            else if (c.isAssignableFrom(Set.class))
            {
                var args=split(arg);
                if (args.length<=1)
                    args=split(arg,",");
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
                if (o==null)
                    return getArg(null,c);
                return o;
            }
            if (c==String.class)
                return arg;
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
                return "true".equalsIgnoreCase(arg);
            else if (c==Character.class)
                return arg.charAt(0);
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
                try
                {
                    return saveCase(c,Bukkit.getWorld(UUID.fromString(arg)));
                }
                catch (IllegalArgumentException e)
                {
                    return saveCase(c,Bukkit.getWorld(arg));
                }
            else if (Entity.class.isAssignableFrom(c))
                try
                {
                    return saveCase(c,Bukkit.getEntity(UUID.fromString(arg)));
                }
                catch (IllegalArgumentException e)
                {
                    return null;
                }
            else if (c==UUID.class)
                try
                {
                    return UUID.fromString(arg);
                }
                catch (IllegalArgumentException e)
                {
                    return null;
                }
            else if (c==Material.class)
                return Material.matchMaterial(arg);
            else if (c==Location.class)
            {
                String[] split=split(arg,"/");
                World world=null;
                if (split.length==4)
                {
                    try
                    {
                        world=Bukkit.getWorld(UUID.fromString(split[0]));
                        if (world==null)
                            throw new IllegalArgumentException();
                    }
                    catch (IllegalArgumentException e)
                    {
                        world=Bukkit.getWorld(split[0]);
                    }
                }
                else if (split.length!=3)
                    return null;
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
                return EntityType.fromName(arg);
            else if (c==Enchantment.class)
                return Enchantment.getByName(arg);
            else if (c==PotionEffectType.class)
                return PotionEffectType.getByName(arg);
            else if (c==Color.class)
            {
                String[] split=split(arg,"/");
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
                return Particle.valueOf(arg);
            else if (c==Sound.class)
                return Sound.valueOf(arg);
            else if (c==BlockFace.class)
                return BlockFace.valueOf(arg);
            else if (c==DyeColor.class)
                return DyeColor.valueOf(arg);
            else if (c==Instrument.class)
                return Instrument.valueOf(arg);
            else if (c.isEnum())
            {
                for (Object o: c.getEnumConstants())
                    if (o.toString().equalsIgnoreCase(arg))
                        return o;
                return null;
            }
            else
                return null;
        }
    }

    private static class ArgsMap
    {
        private final Map<String,ArgsMap> commandMap=new HashMap<>();
        private ArgsMap commandAny;//=new ArgsMap();
        private Run res;
        private Run any;

        public ArgsMap()
        {
        }

        static Map<String,String> mapArgs(String[] args,String[] formArgs)
        {
            return mapArgs(args,formArgs,0,0);
        }

        static Map<String,String> mapArgs(String[] args,String[] formArgs,int indArg,int indForm)
        {
            if (args.length==indArg)//传参已结束
            {
                for (int i=indForm;i<formArgs.length;i++)//若剩余的形参为必填，则匹配失败,返回null
                {
                    if ((!formArgs[i].startsWith("[")||!formArgs[i].endsWith("]"))&&!formArgs[i].equals("..."))
                    {
                        return null;
                    }
                }
                return new HashMap<>();
            }
            if (formArgs.length==indForm)//模式串已结束,但传参未结束,匹配失败,返回null
            {
                return null;
            }
            String arg=args[indArg];
            String formArg=formArgs[indForm];
            if (formArg.startsWith("<")&&formArg.endsWith(">"))//若形参为必填,则匹配成功,尝试匹配下一个
            {
                var map=mapArgs(args,formArgs,indArg+1,indForm+1);
                if (map==null)
                {
                    return null;
                }
                map.put(formArg.substring(1,formArg.length()-1),arg);//若匹配成功,则将匹配成功的参数加入map
                return map;
            }
            else if (formArg.startsWith("[")&&formArg.endsWith("]"))//若形参为可选
            {
                var map=mapArgs(args,formArgs,indArg+1,indForm+1);//尝试匹配此参数
                if (map==null) //匹配此参数失败
                {
                    map=mapArgs(args,formArgs,indArg,indForm+1);//尝试不匹配此参数
                    return map;//匹配成功或失败,返回结果
                }
                map.put(formArg.substring(1,formArg.length()-1),arg);//匹配此参数成功,将参数加入map
                return map;
            }
            else if (formArg.equals("..."))//若形参为可变参数
            {
                //将剩余的参数合并为一个字符串,并将其加入map
                StringBuilder sb=new StringBuilder();
                for (int i=indArg;i<args.length;i++)
                {
                    sb.append(args[i]);
                    if (i!=args.length-1)
                    {
                        sb.append(" ");
                    }
                }
                Map<String,String> map=new HashMap<>();
                map.put(formArg,sb.toString());
                return map;
            }
            else if (formArg.equalsIgnoreCase(arg))//若参数为固定值且匹配,则尝试匹配下一个
            {
                return mapArgs(args,formArgs,indArg+1,indForm+1);
            }
            else//若参数为固定值且不匹配,则匹配失败,返回null
            {
                return null;
            }
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
            add(split(run.parse()),run);
        }

        void add(String[] args,Run run)
        {
            add(args,run,0);
        }

        void add(String[] args,Run run,int index)
        {
            if (args.length==index)
            {
                this.res=run;
                return;
            }
            String arg=args[index];
            if (arg.equals("..."))
            {
                this.any=run;
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
                this.add(args,run,index+1);
            }
        }

        private static boolean check(Run run,CommandSender sender)
        {
            return run!=null&&
                   (run.permission==null||run.permission.isEmpty()||sender.hasPermission(run.permission))&&
                   (!run.playerOnly||sender instanceof Player);
        }

        Run get(String[] args,CommandSender sender)
        {
            return get(args,0,sender);
        }

        Run get(String[] args,int index,CommandSender sender)
        {
            if (args.length==index)
            {
                if (check(res,sender))
                    return res;
                else if (check(any,sender))
                    return any;
                else
                    return null;
            }
            var map=this.commandMap.get(args[index].toLowerCase(Locale.ENGLISH));
            if (map!=null)
            {
                var m=map.get(args,index+1,sender);
                if (m!=null)
                    return m;
            }
            var m=commandAny().get(args,index+1,sender);
            if (m!=null)
                return m;
            if (check(any,sender))
                return any;
            return null;
        }
    }

    private static class Run
    {
        private final Method method;
        private final Field field;
        private final String format;
        private final String permission;
        private final boolean playerOnly;
        private final boolean isTab;

        public Run(Method method,String format,String permission,boolean playerOnly,boolean isTab)
        {
            this.method=method;
            this.field=null;
            this.format=format;
            this.permission=permission;
            this.playerOnly=playerOnly;
            this.isTab=isTab;
        }

        public Run(Field field,String format,String permission,boolean playerOnly,boolean isTab)
        {
            this.method=null;
            this.field=field;
            this.format=format;
            this.permission=permission;
            this.playerOnly=playerOnly;
            this.isTab=isTab;
        }

        public static Run[] of(Method method,ParseCommand parseCommand)
        {
            var format=parseCommand.value();
            var permission=parseCommand.permission();
            var playerOnly=parseCommand.onlyPlayer();
            var isTab=false;
            var res=new Run[format.length];
            for (int i=0;i<format.length;i++)
            {
                res[i]=new Run(method,format[i],permission,playerOnly,isTab);
            }
            return res;
        }

        public static Run[] of(Method field,ParseTab parseTab)
        {
            var format=parseTab.value();
            var permission=parseTab.permission();
            var playerOnly=parseTab.onlyPlayer();
            var isTab=true;
            var res=new Run[format.length];
            for (int i=0;i<format.length;i++)
            {
                res[i]=new Run(field,format[i],permission,playerOnly,isTab);
            }
            return res;
        }

        public static Run[] of(Field field,ParseTab parseTab)
        {
            var format=parseTab.value();
            var permission=parseTab.permission();
            var playerOnly=parseTab.onlyPlayer();
            var isTab=true;
            var res=new Run[format.length];
            for (int i=0;i<format.length;i++)
            {
                res[i]=new Run(field,format[i],permission,playerOnly,isTab);
            }
            return res;
        }

        public static Run[] of(Field field,ParseCommand parseCommand)
        {
            var format=parseCommand.value();
            var permission=parseCommand.permission();
            var playerOnly=parseCommand.onlyPlayer();
            var isTab=false;
            var res=new Run[format.length];
            for (int i=0;i<format.length;i++)
            {
                res[i]=new Run(field,format[i],permission,playerOnly,isTab);
            }
            return res;
        }

        public Parameter[] getParameters()
        {
            if (method!=null)
                return method.getParameters();
            else
                return new Parameter[0];
        }

        public Object invoke(Object obj,Object... args) throws InvocationTargetException, IllegalAccessException
        {
            if (method!=null)
                return method.invoke(obj,args);
            else if (field!=null)
                return field.get(obj);
            return null;
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

        public boolean isPlayerOnly()
        {
            return playerOnly;
        }
    }
}