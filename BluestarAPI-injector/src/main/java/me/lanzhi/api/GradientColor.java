package me.lanzhi.api;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GradientColor
{
    private static final Pattern gradient=Pattern.compile("#([0-9A-Fa-f]{6})-([0-9A-Fa-f]{6})<(.*?)>");
    private RGBColor start;
    private RGBColor end;

    public GradientColor(RGBColor start,RGBColor end)
    {
        this.start=start;
        this.end=end;
    }

    public GradientColor(String start,String end)
    {
        this(new RGBColor(start),new RGBColor(end));
    }

    public static String colorText(RGBColor start,RGBColor end,String message)
    {
        if (message==null||message.isEmpty())
        {
            return "";
        }
        StringBuilder builder=new StringBuilder();
        int r, g, b;
        r=end.getRed()-start.getRed();
        g=end.getGreen()-start.getGreen();
        b=end.getBlue()-start.getBlue();
        message=ChatColor.translateAlternateColorCodes('&',message);
        double len=ChatColor.stripColor(message).length()-1;
        StringBuilder p=new StringBuilder();
        for (int i=0,j=0;i<message.length();i++)
        {
            if (message.charAt(i)==ChatColor.COLOR_CHAR&&i!=message.length()-1)
            {
                ChatColor c=ChatColor.getByChar(message.charAt(i+1));
                if (c==ChatColor.RESET)
                {
                    p=new StringBuilder();
                    i++;
                    continue;
                }
                else if (c!=null)
                {
                    if (c.isFormat())
                    {
                        p.append(c);
                    }
                    i++;
                    continue;
                }
            }
            double x=j/len;
            RGBColor color=new RGBColor(start.getRed()+(int) (r*x),start.getGreen()+(int) (g*x),start.getBlue()+(int) (b*x));
            builder.append(color).append(p).append(message.charAt(i));
            j++;
        }
        return builder.toString();
    }

    public static String colorText(String start,String end,String message)
    {
        return colorText(new RGBColor(start),new RGBColor(end),message);
    }

    public String colorText(String message)
    {
        return colorText(start,end,message);
    }

    public RGBColor getStart()
    {
        return start;
    }

    public GradientColor setStart(RGBColor start)
    {
        this.start=start;
        return this;
    }

    public RGBColor getEnd()
    {
        return end;
    }

    public GradientColor setEnd(RGBColor end)
    {
        this.end=end;
        return this;
    }

    public static String setColor(String message)
    {
        message=RGBColor.setRandomColor(message);
        Matcher matcher=gradient.matcher(message);
        StringBuilder builder=new StringBuilder();
        while (matcher.find())
        {
            matcher.appendReplacement(builder,setGradient(matcher));
        }
        matcher.appendTail(builder);
        return RGBColor.setColor(builder.toString());
    }

    private static String setGradient(Matcher matcher)
    {
        String s=matcher.group(1),e=matcher.group(2),message=matcher.group(3);
        RGBColor start=new RGBColor(Integer.parseInt(s,16));
        RGBColor end=new RGBColor(Integer.parseInt(e,16));
        return colorText(start,end,message);
    }
}
