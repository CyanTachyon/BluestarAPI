package me.lanzhi.api.player.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public class PageGui implements Gui<PageGui>
{
    private final ChestGui chestGui;
    private final int maxPage;
    private final GuiItem bottomItem;
    private final GuiItem closeButton;
    private final GuiItem turnLeftButton;
    private final GuiItem turnRightButton;
    private final Map<Integer,GuiItem> items;
    private boolean prohibitAnyClick;
    private boolean preventClose;
    private BiConsumer<PageGui,GuiCloseAction> onClose;
    private int page;

    private PageGui(Plugin plugin,int size,String title,Player player,GuiItem emptyItem,ItemStack bottomItem,ItemStack closeButton,ItemStack turnLeftButton,ItemStack turnRightButton,Map<Integer,GuiItem> items,boolean prohibitAnyClick,boolean preventClose,BiConsumer<PageGui,GuiCloseAction> onClose)
    {
        chestGui=ChestGui.builder(plugin)
                .setSize(size)
                .title(title)
                .emptyItem(emptyItem)
                .prohibitAnyClick(prohibitAnyClick)
                .preventClose(preventClose)
                .onClose((chestGui1,guiCloseAction)->PageGui.this.getOnClose().accept(this,guiCloseAction))
                .make(player);
        this.items=items;
        this.onClose=onClose;
        this.page=0;
        this.bottomItem=new GuiItem().setItem(bottomItem);
        this.closeButton=new GuiItem().setItem(closeButton);
        this.closeButton.setOnClick((gui,clickType)->GuiItem.Response.close());
        this.turnLeftButton=new GuiItem().setItem(turnLeftButton);
        this.turnLeftButton.setOnClick((gui,clickType)->
                                       {
                                           setPage(page-1);
                                           return GuiItem.Response.nothing();
                                       });
        this.turnRightButton=new GuiItem().setItem(turnRightButton);
        this.turnRightButton.setOnClick((gui,clickType)->
                                        {
                                            setPage(page+1);
                                            return GuiItem.Response.nothing();
                                        });
        Map.Entry<Integer,GuiItem> maxEntry=null;
        for (Map.Entry<Integer,GuiItem> entry: this.items.entrySet())
        {
            if (maxEntry==null||entry.getKey()>maxEntry.getKey())
            {
                maxEntry=entry;
            }
        }
        int maxPage1=0;
        if (maxEntry!=null)
        {
            maxPage1=(int) Math.ceil((double) maxEntry.getKey()/((chestGui.getSize()-1)*9));
        }
        if (maxPage1==0)
        {
            maxPage1=1;
        }
        this.maxPage=maxPage1;

        paint();
    }

    public void paint()
    {
        setPage(page);
    }

    public int getSize()
    {
        return chestGui.getSize();
    }

    public static Builder builder(Plugin plugin)
    {
        return new Builder(plugin);
    }

    public Plugin getPlugin()
    {
        return chestGui.getPlugin();
    }

    public String getTitle()
    {
        return chestGui.getTitle();
    }

    @Override
    public void setTitle(String title)
    {
        chestGui.setTitle(title);
    }

    public Player getPlayer()
    {
        return chestGui.getPlayer();
    }

    @Override
    public boolean isProhibitAnyClick()
    {
        return prohibitAnyClick;
    }

    @Override
    public void setProhibitAnyClick(boolean b)
    {
        this.prohibitAnyClick=b;
    }

    @Override
    public boolean isPreventClose()
    {
        return preventClose;
    }

    @Override
    public void setPreventClose(boolean b)
    {
        this.preventClose=b;
    }

    @Override
    public BiConsumer<PageGui,GuiCloseAction> getOnClose()
    {
        return onClose;
    }

    @Override
    public void setOnClose(BiConsumer<PageGui,GuiCloseAction> onClose)
    {
        this.onClose=onClose;
    }

    public boolean isOpen()
    {
        return chestGui.isOpen();
    }

    public Inventory getInventory()
    {
        return chestGui.getInventory();
    }

    @Override
    public Listener getGuiListener()
    {
        return chestGui.getGuiListener();
    }

    public void open()
    {
        chestGui.open();
    }

    @Override
    public void close()
    {
        chestGui.close();
    }

    public int getMaxPage()
    {
        return maxPage;
    }

    public int getPage()
    {
        return page;
    }

    private void setPage(int page)
    {
        if (page<0)
        {
            page=0;
        }
        if (page>=maxPage)
        {
            page=maxPage;
        }
        this.page=page;
        chestGui.clearItems();

        for (int i=0;i<9;i++)
        {
            chestGui.setItem(getSize()-1,i,bottomItem);
        }

        for (int i=0;i<getSize()-1;i++)
        {
            for (int j=0;j<9;j++)
            {
                int id=page*(getSize()-1)*9+i*9+j;
                if (items.containsKey(id))
                {
                    chestGui.setItem(i,j,items.get(id));
                }
            }
        }

        //关闭按钮
        if (!preventClose)
        {
            chestGui.setItem(getSize()-1,4,closeButton);
        }
        //上一页按钮
        if (page>0)
        {
            chestGui.setItem(getSize()-1,3,turnLeftButton);
        }
        //下一页按钮
        if (page<maxPage-1)
        {
            chestGui.setItem(getSize()-1,5,turnRightButton);
        }
        chestGui.paint();
    }

    public ItemStack getBottomItem()
    {
        return bottomItem.getItem();
    }

    public void setBottomItem(ItemStack bottomItem)
    {
        this.bottomItem.setItem(bottomItem);
    }

    public ItemStack getCloseButton()
    {
        return closeButton.getItem();
    }

    public void setCloseButton(ItemStack closeButton)
    {
        this.closeButton.setItem(closeButton);
    }

    public ItemStack getTurnLeftButton()
    {
        return turnLeftButton.getItem();
    }

    public void setTurnLeftButton(ItemStack turnLeftButton)
    {
        this.turnLeftButton.setItem(turnLeftButton);
    }

    public ItemStack getTurnRightButton()
    {
        return turnRightButton.getItem();
    }

    public void setTurnRightButton(ItemStack turnRightButton)
    {
        this.turnRightButton.setItem(turnRightButton);
    }

    public static class Builder implements GuiBuilder<PageGui>
    {
        private final Map<Integer,GuiItem> items=new HashMap<>();

        private GuiItem emptyItem;
        private String title;
        private int size;
        private boolean prohibitAnyClick=false;
        private boolean preventClose=false;
        private ItemStack bottomItem;
        private ItemStack closeButton;
        private ItemStack turnLeft;
        private ItemStack turnRight;
        private Plugin plugin;
        private BiConsumer<PageGui,GuiCloseAction> onClose;

        private Builder(Plugin plugin)
        {
            this.title="Chest";
            this.size=6;
            try
            {
                this.bottomItem=new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                this.closeButton=new ItemStack(Material.RED_STAINED_GLASS_PANE);
                this.turnLeft=new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
                this.turnRight=new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
            }
            catch (Exception e)
            {
                this.bottomItem=new ItemStack(Objects.requireNonNull(Material.getMaterial("STAINED_GLASS_PANE")),
                                              1,
                                              (short) 15);
                this.closeButton=new ItemStack(Objects.requireNonNull(Material.getMaterial("STAINED_GLASS_PANE")),
                                               1,
                                               (short) 14);
                this.turnLeft=new ItemStack(Objects.requireNonNull(Material.getMaterial("STAINED_GLASS_PANE")),
                                            1,
                                            (short) 13);
                this.turnRight=new ItemStack(Objects.requireNonNull(Material.getMaterial("STAINED_GLASS_PANE")),
                                             1,
                                             (short) 13);
            }
            this.emptyItem=GuiItem.EMPTY;
            this.onClose=(gui,b)->
            {
            };

            ItemMeta meta=bottomItem.getItemMeta();
            assert meta!=null;
            meta.setDisplayName(" ");
            bottomItem.setItemMeta(meta);

            meta=closeButton.getItemMeta();
            assert meta!=null;
            meta.setDisplayName("点击关闭");
            closeButton.setItemMeta(meta);

            meta=turnLeft.getItemMeta();
            assert meta!=null;
            meta.setDisplayName("上一页");
            turnLeft.setItemMeta(meta);

            meta=turnRight.getItemMeta();
            assert meta!=null;
            meta.setDisplayName("下一页");
            turnRight.setItemMeta(meta);
            plugin(plugin);
        }

        public Builder size(int size)
        {
            this.size=size;
            return this;
        }

        public Builder bottomItem(ItemStack bottomItem)
        {
            this.bottomItem=bottomItem;
            return this;
        }

        public Builder closeItem(ItemStack closeItem)
        {
            this.closeButton=closeItem;
            return this;
        }

        public Builder turnLeft(ItemStack turnLeft)
        {
            this.turnLeft=turnLeft;
            return this;
        }

        public Builder turnRight(ItemStack turnRight)
        {
            this.turnRight=turnRight;
            return this;
        }

        public Builder item(int index,GuiItem item)
        {
            items.put(index,item);
            return this;
        }

        public Map<Integer,GuiItem> items()
        {
            return items;
        }

        public Builder emptyItem(GuiItem item)
        {
            this.emptyItem=item;
            return this;
        }

        public PageGui make(Player player)
        {
            return new PageGui(plugin,
                               size,
                               title,
                               player,
                               emptyItem,
                               bottomItem,
                               closeButton,
                               turnLeft,
                               turnRight,
                               items,
                               prohibitAnyClick,
                               preventClose,
                               onClose);
        }

        public Builder title(String title)
        {
            this.title=title;
            return this;
        }

        @Override
        public String title()
        {
            return title;
        }

        public Builder prohibitAnyClick(boolean prohibitAnyClick)
        {
            this.prohibitAnyClick=prohibitAnyClick;
            return this;
        }

        @Override
        public boolean prohibitAnyClick()
        {
            return prohibitAnyClick;
        }

        public Builder preventClose(boolean preventClose)
        {
            this.preventClose=preventClose;
            return this;
        }

        @Override
        public boolean preventClose()
        {
            return preventClose;
        }

        public Builder plugin(Plugin plugin)
        {
            this.plugin=plugin;
            return this;
        }

        @Override
        public Plugin plugin()
        {
            return plugin;
        }

        public Builder onClose(BiConsumer<PageGui,GuiCloseAction> onClose)
        {
            this.onClose=onClose;
            return this;
        }

        @Override
        public BiConsumer<PageGui,GuiCloseAction> onClose()
        {
            return onClose;
        }

        @Override
        public Builder clone()
        {
            Builder clone=new Builder(plugin);
            clone.title=this.title;
            clone.size=this.size;
            clone.bottomItem=this.bottomItem.clone();
            clone.closeButton=this.closeButton.clone();
            clone.turnLeft=this.turnLeft.clone();
            clone.turnRight=this.turnRight.clone();
            clone.plugin=this.plugin;
            for (Map.Entry<Integer,GuiItem> entry: items.entrySet())
            {
                clone.items.put(entry.getKey(),entry.getValue().clone());
            }
            return clone;
        }
    }
}
