package me.lanzhi.api.player.chat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RGBColor
{
    private final static int MASK=0xff;
    private static final Pattern hex=Pattern.compile("#([0-9A-Fa-f]{6})");
    private static final Pattern random=Pattern.compile("<random>");
    private static final Map<RGBColor, ChatColor> toChatColor=new HashMap<>();
    private static final Map<net.md_5.bungee.api.ChatColor, RGBColor> fromChatColor=new HashMap<>();

    static
    {
        toChatColor.put(new RGBColor(0x000000),ChatColor.BLACK);
        toChatColor.put(new RGBColor(0x0000AA),ChatColor.DARK_BLUE);
        toChatColor.put(new RGBColor(0x00AA00),ChatColor.DARK_GREEN);
        toChatColor.put(new RGBColor(0x00AAAA),ChatColor.DARK_AQUA);
        toChatColor.put(new RGBColor(0xAA0000),ChatColor.DARK_RED);
        toChatColor.put(new RGBColor(0xAA00AA),ChatColor.DARK_PURPLE);
        toChatColor.put(new RGBColor(0xFFAA00),ChatColor.GOLD);
        toChatColor.put(new RGBColor(0xAAAAAA),ChatColor.GRAY);
        toChatColor.put(new RGBColor(0x555555),ChatColor.DARK_GRAY);
        toChatColor.put(new RGBColor(0x5555FF),ChatColor.BLUE);
        toChatColor.put(new RGBColor(0x55FF55),ChatColor.GREEN);
        toChatColor.put(new RGBColor(0x55FFFF),ChatColor.AQUA);
        toChatColor.put(new RGBColor(0xFF5555),ChatColor.RED);
        toChatColor.put(new RGBColor(0xFF55FF),ChatColor.LIGHT_PURPLE);
        toChatColor.put(new RGBColor(0xFFFF55),ChatColor.YELLOW);
        toChatColor.put(new RGBColor(0xFFFFFF),ChatColor.WHITE);
        for (Map.Entry<RGBColor, ChatColor> entry: toChatColor.entrySet())
        {
            fromChatColor.put(entry.getValue().asBungee(),entry.getKey());
        }
    }

    private final int r;
    private final int g;
    private final int b;

    /**
     * 通过颜色数字获得RGBChat类对象
     * rgb即3种颜色,范围是0-255
     */
    public RGBColor(int r,int g,int b)
    {
        this.r=r&MASK;
        this.g=g&MASK;
        this.b=b&MASK;
    }

    public RGBColor(ChatColor color)
    {
        this(fromChatColor.get(color.asBungee()).getColor());
    }

    /**
     * 通过颜色数字获得RGBChat类对象(其实是比如说0xffffff转10进制的那个数)
     *
     * @param color 颜色
     */
    public RGBColor(int color)
    {
        if (color<0)
        {
            color=0;
        }
        this.r=getRed(color)&MASK;
        this.g=getGreen(color)&MASK;
        this.b=getBlue(color)&MASK;
    }

    /**
     * 转换为颜色数字
     * @return 颜色
     */
    public int getColor()
    {
        return getColor(r,g,b);
    }

    /**
     * 通过颜色数值获取其中的红色数值
     * @param color 一个颜色,同第一个构造函数
     * @return 获取的红色
     */
    public static int getRed(int color)
    {
        return (color >> 16)&MASK;
    }

    /**
     * 和获取红色差不多
     */
    public static int getGreen(int color)
    {
        return (color >> 8)&MASK;
    }

    /**
     * 和获取红色差不多
     */
    public static int getBlue(int color)
    {
        return color&MASK;
    }

    /**
     * 通过rgb的数值获取颜色数值
     * @param r 红色
     * @param g 绿色
     * @param b 蓝色
     * @return 颜色
     */
    public static int getColor(int r,int g,int b)
    {
        return (r<<16)+(g<<8)+b;
    }

    public RGBColor(net.md_5.bungee.api.ChatColor chatColor)
    {
        this(fromChatColor.get(chatColor).getColor());
    }

    public RGBColor(Color color)
    {
        if (color==null)
        {
            r=g=b=0;
            return;
        }
        r=color.getRed();
        g=color.getGreen();
        b=color.getBlue();
    }

    public RGBColor(java.awt.Color color)
    {
        if (color==null)
        {
            r=g=b=0;
            return;
        }
        r=color.getRed();
        g=color.getGreen();
        b=color.getBlue();
    }

    public RGBColor(String color)
    {
        this(Integer.parseInt(color,16));
    }

    /**
     * 将一整个文字转换成颜色文字,比如说"&a你好,#123456 hello world"(RGB颜色用#abcdef这样表示)
     * @param message 文字内容
     * @return 按照符号染色后的内容
     */
    public static String setColor(String message)
    {
        message=setRandomColor(message);
        StringBuilder stringBuilder=new StringBuilder();
        Matcher matcher=hex.matcher(message);
        while (matcher.find())
        {
            matcher.appendReplacement(stringBuilder,toColorCode(matcher.group()));
        }
        matcher.appendTail(stringBuilder);
        return ChatColor.translateAlternateColorCodes('&',stringBuilder.toString());
    }

    /**
     * 加上随机颜色
     * @param message 原信息
     * @return 加上随机颜色后的
     */
    public static String setRandomColor(String message)
    {
        StringBuilder stringBuilder=new StringBuilder();
        Matcher matcher=random.matcher(message);
        while (matcher.find())
        {
            matcher.appendReplacement(stringBuilder,random().getHexColor());
        }
        matcher.appendTail(stringBuilder);
        return stringBuilder.toString();
    }

    /**
     * 用16进制的颜色获取彩色文本比如说传入#ffffff或ffffff,可以获取到的内容将会让后面显示白色
     * @param hexColor 16进制的颜色
     * @return 颜色代码
     */
    public static String toColorCode(String hexColor)
    {
        StringBuilder builder=new StringBuilder("§x");
        for (char i: hexColor.toCharArray())
        {
            if ("0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(i)<=-1)
            {
                continue;
            }
            builder.append("§").append(i);
        }
        return builder.toString();
    }

    /**
     * 转换为16进制的颜色
     * @return 16进制的颜色
     */
    public String getHexColor()
    {
        return getHexColor(r,g,b);
    }

    /**
     * 随机颜色
     * @return 一个随机的RGB颜色
     */
    public static RGBColor random()
    {
        return new RGBColor(new Random().nextInt(0xffffff));
    }

    /**
     * 通过RGB获取16进制的颜色例如255,255,255获取的是ffffff
     * @param r 红
     * @param g 绿
     * @param b 蓝
     * @return 16进制的颜色
     */
    public static String getHexColor(int r,int g,int b)
    {
        return getHexColor(getColor(r,g,b));
    }

    /**
     * 通过颜色获取16进制的颜色,例如11259375是ABCDEF
     */
    public static String getHexColor(int color)
    {
        return getHexRed(color)+getHexGreen(color)+getHexBlue(color);
    }

    /**
     * 通过颜色获取16进制下的红色(例如0x123456的红色是12)
     */
    public static String getHexRed(int color)
    {
        StringBuilder builder=new StringBuilder(Integer.toHexString(getRed(color)));
        //补全为2位
        if (builder.length()==1)
        {
            builder.insert(0,'0');
        }
        return builder.toString();
    }

    /**
     * 通过颜色获取16进制下的绿色(例如0x123456的绿色是34)
     */
    public static String getHexGreen(int color)
    {
        StringBuilder builder=new StringBuilder(Integer.toHexString(getGreen(color)));
        //补全为2位
        if (builder.length()==1)
        {
            builder.insert(0,'0');
        }
        return builder.toString();
    }

    /**
     * 通过颜色获取16进制下的蓝色(例如0x123456的蓝色是56)
     */
    public static String getHexBlue(int color)
    {
        StringBuilder builder=new StringBuilder(Integer.toHexString(getBlue(color)));
        //补全为2位
        if (builder.length()==1)
        {
            builder.insert(0,'0');
        }
        return builder.toString();
    }

    /**
     * 获取颜色中的红色
     * @return 红色
     */
    public int getRed()
    {
        return r;
    }

    /**
     * 获取颜色中的绿色
     * @return 绿色
     */
    public int getGreen()
    {
        return g;
    }

    /**
     * 获取颜色中的蓝色
     * @return 蓝色
     */
    public int getBlue()
    {
        return b;
    }

    /**
     * 16进制的红色
     * @return 16进制的红色
     */
    public String getHexRed()
    {
        return getHexRed(getColor());
    }

    /**
     * 16进制的绿色
     * @return 16进制的绿色
     */
    public String getHexGreen()
    {
        return getHexGreen(getColor());
    }

    /**
     * 16进制的蓝色
     * @return 16进制的蓝色
     */
    public String getHexBlue()
    {
        return getHexBlue(getColor());
    }

    /**
     * 转换为颜色染色符号,方便使用,例如"hello "+new RGBColor(100,125,255)+"world"
     *
     * @return 颜色染色符号
     */
    @Override
    public String toString()
    {
        return toColorCode();
    }

    /**
     * 转换为颜色染色符号
     *
     * @return 颜色染色符号
     */
    public String toColorCode()
    {
        String[] p=Bukkit.getServer().getClass().getPackage().getName().split("\\.");
        int version=Integer.parseInt(p[p.length-1].split("_")[1]);
        if (version>=16)
        {
            return toColorCode(getHexColor());
        }
        RGBColor c=null;
        for (Map.Entry<RGBColor, ChatColor> entry: toChatColor.entrySet())
        {
            if (this.getDifference(entry.getKey())<this.getDifference(c))
            {
                c=entry.getKey();
            }
        }
        return ""+toChatColor.get(c);
    }

    public int getDifference(RGBColor other)
    {
        if (other==null)
        {
            return 3*0xFF;
        }
        return Math.abs(other.r-r)+Math.abs(other.g-g)+Math.abs(other.b-b);
    }
}
