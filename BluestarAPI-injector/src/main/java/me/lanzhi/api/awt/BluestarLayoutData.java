package me.lanzhi.api.awt;

import java.awt.*;

public class BluestarLayoutData
{
    public static final int FILL=0;
    public static final int CENTER=1;
    public static final int FRONT=2;
    public static final int BACK=3;
    private Insets insets;
    private int totalWidth;
    private int totalHeight;
    private int x;
    private int y;
    private int width;
    private int height;
    private int portraitAlignment=0;
    private int transverseAlignment=0;

    public BluestarLayoutData()
    {
        this(1,1);
    }

    public BluestarLayoutData(int totalWidth,int totalHeight)
    {
        this(totalWidth,totalHeight,0,0);
    }

    public BluestarLayoutData(int totalWidth,int totalHeight,int x,int y)
    {
        this(totalWidth,totalHeight,x,y,1,1);
    }

    public BluestarLayoutData(int totalWidth,int totalHeight,int x,int y,int width,int height)
    {
        this(totalWidth,totalHeight,x,y,width,height,new Insets(0,0,0,0));
    }

    public BluestarLayoutData(int totalWidth,int totalHeight,int x,int y,int width,int height,Insets insets)
    {
        this.totalWidth=totalWidth;
        this.totalHeight=totalHeight;
        this.x=x;
        this.y=y;
        this.width=width;
        this.height=height;
        this.insets=insets;
    }

    public BluestarLayoutData(int totalWidth,int totalHeight,Insets insets)
    {
        this(totalWidth,totalHeight,0,0,insets);
    }

    public BluestarLayoutData(int totalWidth,int totalHeight,int x,int y,Insets insets)
    {
        this(totalWidth,totalHeight,x,y,1,1,insets);
    }

    public Insets getInsets()
    {
        return insets;
    }

    public BluestarLayoutData setInsets(Insets insets)
    {
        this.insets=insets;
        return this;
    }

    public int getHeight()
    {
        return height;
    }

    public BluestarLayoutData setHeight(int height)
    {
        this.height=height;
        return this;
    }

    public int getTotalHeight()
    {
        return totalHeight;
    }

    public BluestarLayoutData setTotalHeight(int totalHeight)
    {
        this.totalHeight=totalHeight;
        return this;
    }

    public int getTotalWidth()
    {
        return totalWidth;
    }

    public BluestarLayoutData setTotalWidth(int totalWidth)
    {
        this.totalWidth=totalWidth;
        return this;
    }

    public int getWidth()
    {
        return width;
    }

    public BluestarLayoutData setWidth(int width)
    {
        this.width=width;
        return this;
    }

    public int getX()
    {
        return x;
    }

    public BluestarLayoutData setX(int x)
    {
        this.x=x;
        return this;
    }

    public int getY()
    {
        return y;
    }

    public BluestarLayoutData setY(int y)
    {
        this.y=y;
        return this;
    }

    public int getPortraitAlignment()
    {
        return portraitAlignment;
    }

    public BluestarLayoutData setPortraitAlignment(int portraitAlignment)
    {
        this.portraitAlignment=portraitAlignment;
        return this;
    }

    public int getTransverseAlignment()
    {
        return transverseAlignment;
    }

    public BluestarLayoutData setTransverseAlignment(int transverseAlignment)
    {
        this.transverseAlignment=transverseAlignment;
        return this;
    }

    public BluestarLayoutData setAlignment(int alignment)
    {
        this.transverseAlignment=alignment;
        this.portraitAlignment=alignment;
        return this;
    }
}
