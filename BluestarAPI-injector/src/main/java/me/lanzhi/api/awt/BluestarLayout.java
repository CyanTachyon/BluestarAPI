package me.lanzhi.api.awt;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class BluestarLayout implements LayoutManager2
{
    private final Map<Component,BluestarLayoutData> map=new HashMap<>();

    @Override
    @Deprecated
    public void addLayoutComponent(String name,Component comp)
    {
    }

    @Override
    public void removeLayoutComponent(Component comp)
    {
        synchronized (map)
        {
            map.remove(comp);
        }
    }

    @Override
    public Dimension preferredLayoutSize(Container parent)
    {
        //min和max的平均
        return new Dimension((minimumLayoutSize(parent).width+maximumLayoutSize(parent).width)/2,
                             (minimumLayoutSize(parent).height+maximumLayoutSize(parent).height)/2);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent)
    {
        //所有组件最小值的最大值
        int width=0;
        int height=0;
        synchronized (map)
        {
            for (var entry: map.entrySet())
            {
                var data=entry.getValue();
                var component=entry.getKey();
                var insets=data.getInsets();
                var componentWidth=component.getMinimumSize().width+insets.left+insets.right;
                var componentHeight=component.getMinimumSize().height+insets.top+insets.bottom;
                componentHeight*=data.getTotalHeight();
                componentWidth*=data.getTotalWidth();
                if (componentWidth>width)
                {
                    width=componentWidth;
                }
                if (componentHeight>height)
                {
                    height=componentHeight;
                }
            }
        }
        return new Dimension(width,height);
    }

    @Override
    public Dimension maximumLayoutSize(Container target)
    {
        //所有组件最大值的最小值
        int width=Integer.MAX_VALUE;
        int height=Integer.MAX_VALUE;
        synchronized (map)
        {
            for (var entry: map.entrySet())
            {
                var data=entry.getValue();
                var component=entry.getKey();
                var insets=data.getInsets();
                var componentWidth=component.getMaximumSize().width+insets.left+insets.right;
                var componentHeight=component.getMaximumSize().height+insets.top+insets.bottom;
                componentHeight*=data.getTotalHeight();
                componentWidth*=data.getTotalWidth();
                if (componentWidth<width)
                {
                    width=componentWidth;
                }
                if (componentHeight<height)
                {
                    height=componentHeight;
                }
            }
        }
        return new Dimension(width,height);
    }

    @Override
    public void layoutContainer(Container parent)
    {
        int width=parent.getWidth();
        int height=parent.getHeight();
        for (Component c: parent.getComponents())
        {
            if (map.containsKey(c))
            {
                BluestarLayoutData data=map.get(c);
                double dx=((double) width)/data.getTotalWidth();
                double dy=((double) height)/data.getTotalHeight();
                double sx;//=data.isChangeSize()?((dx*(data.getX()+data.getWidth()))-dx*data.getX())-data.getInsets()
                // .right-data.getInsets().left:c.getPreferredSize().getWidth();
                double sy;//=data.isChangeSize()?((dy*(data.getY()+data.getHeight()))-dy*data.getY())-data.getInsets
                // ().bottom-data.getInsets().top:c.getPreferredSize().getHeight();
                double lx;
                double ly;
                //处理横向
                switch (data.getTransverseAlignment())
                {
                    case BluestarLayoutData.FRONT:
                    {
                        sx=c.getPreferredSize().getWidth();
                        lx=dx*data.getX()+data.getInsets().left;
                        break;
                    }
                    case BluestarLayoutData.BACK:
                    {
                        sx=c.getPreferredSize().getWidth();
                        lx=dx*(data.getX()+data.getWidth())-data.getInsets().right-sx;
                        break;
                    }
                    case BluestarLayoutData.CENTER:
                    {
                        sx=c.getPreferredSize().getWidth();
                        lx=(dx*data.getWidth()+data.getInsets().left-data.getInsets().right-sx)/2;
                        lx+=dx*data.getX();
                        break;
                    }
                    default:
                    {
                        sx=((dx*(data.getX()+data.getWidth()))-dx*data.getX())-
                           data.getInsets().right-
                           data.getInsets().left;
                        lx=dx*data.getX()+data.getInsets().left;
                    }
                }
                //处理纵向
                switch (data.getPortraitAlignment())
                {
                    case BluestarLayoutData.FRONT:
                    {
                        sy=c.getPreferredSize().getHeight();
                        ly=dy*data.getY()+data.getInsets().top;
                        break;
                    }
                    case BluestarLayoutData.BACK:
                    {
                        sy=c.getPreferredSize().getHeight();
                        //在底部
                        ly=dy*(data.getY()+data.getHeight())-data.getInsets().bottom-sy;
                        break;
                    }
                    case BluestarLayoutData.CENTER:
                    {
                        sy=c.getPreferredSize().getHeight();
                        //在中间
                        ly=(dy*data.getHeight()+data.getInsets().top-data.getInsets().bottom-sy)/2;
                        ly+=dy*data.getY();
                        break;
                    }
                    default:
                    {
                        sy=((dy*(data.getY()+data.getHeight()))-dy*data.getY())-
                           data.getInsets().bottom-
                           data.getInsets().top;
                        ly=dy*data.getY()+data.getInsets().top;
                    }
                }
                c.setBounds((int) lx,(int) ly,(int) sx,(int) sy);
            }
        }
    }

    @Override
    public void addLayoutComponent(Component comp,Object constraints)
    {
        if (!(constraints instanceof BluestarLayoutData))
        {
            return;
        }
        synchronized (map)
        {
            map.put(comp,(BluestarLayoutData) constraints);
        }
    }

    @Override
    public float getLayoutAlignmentX(Container target)
    {
        return 0;
    }

    @Override
    public float getLayoutAlignmentY(Container target)
    {
        return 0;
    }

    @Override
    public void invalidateLayout(Container target)
    {
    }
}
