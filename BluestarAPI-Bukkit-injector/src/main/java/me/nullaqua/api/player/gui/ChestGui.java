package me.nullaqua.api.player.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.function.BiConsumer;

public class ChestGui implements Gui<ChestGui>
{
    private final Plugin plugin;

    private final GuiItem[][] items;
    private final Player player;
    private int size;
    private String title;
    private GuiItem emptyItem;
    private boolean prohibitAnyClick;
    private boolean preventClose;
    private BiConsumer<ChestGui,GuiCloseAction> onClose;
    private boolean isOpen=false;
    private Inventory inventory;
    private GuiListener listener;

    private ChestGui(Plugin plugin,int size,String title,Player player,GuiItem emptyItem,boolean prohibitAnyClick,
                     boolean preventClose,BiConsumer<ChestGui,GuiCloseAction> onClose,GuiItem[][] items)
    {
        this.plugin=plugin;
        this.size=size;
        this.title=title;
        this.player=player;
        this.emptyItem=emptyItem;
        this.prohibitAnyClick=prohibitAnyClick;
        this.preventClose=preventClose;
        this.onClose=onClose;
        this.items=items;
        init();
        open();
    }

    private void init()
    {
        inventory=player.getServer().createInventory(player,size*9,title);
        paint();
    }

    public void paint()
    {
        for (int i=0;i<this.size;i++)
        {
            for (int j=0;j<9;j++)
            {
                if (items[i][j]==null)
                {
                    items[i][j]=emptyItem;
                }
                inventory.setItem(i*9+j,items[i][j].getItem());
            }
        }
    }

    public static Builder builder(Plugin plugin)
    {
        return new Builder(plugin);
    }

    public GuiItem getEmptyItem()
    {
        return emptyItem;
    }

    public void setEmptyItem(GuiItem emptyItem)
    {
        this.emptyItem=emptyItem;
    }

    public GuiItem getItem(int x,int y)
    {
        if (x>=0&&x<this.size&&y>=0&&y<9)
        {
            return this.items[x][y]!=null?this.items[x][y]:emptyItem;
        }
        return null;
    }

    public ChestGui clearItems()
    {
        for (int i=0;i<size;i++)
        {
            for (int j=0;j<9;j++)
            {
                removeItem(i,j);
            }
        }
        return this;
    }

    public GuiItem removeItem(int x,int y)
    {
        return setItem(x,y,emptyItem);
    }

    public GuiItem setItem(int x,int y,GuiItem item)
    {
        if (x>=0&&x<this.size&&y>=0&&y<9)
        {
            GuiItem item1=items[x][y];
            this.items[x][y]=item;
            return item1;
        }
        return null;
    }

    public Plugin getPlugin()
    {
        return plugin;
    }

    public String getTitle()
    {
        return title;
    }

    @Override
    public void setTitle(String title)
    {
        this.title=title;
        if (isOpen)
        {
            close(true);
            init();
            open();
        }
        else
        {
            init();
        }
    }

    public Player getPlayer()
    {
        return player;
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
    public BiConsumer<ChestGui,GuiCloseAction> getOnClose()
    {
        return onClose;
    }

    @Override
    public void setOnClose(BiConsumer<ChestGui,GuiCloseAction> onClose)
    {
        this.onClose=onClose;
    }

    @Override
    public boolean isOpen()
    {
        return isOpen;
    }

    public Inventory getInventory()
    {
        return inventory;
    }

    @Override
    public Listener getGuiListener()
    {
        return listener;
    }

    public void open()
    {
        if (!plugin.isEnabled()||isOpen)
        {
            return;
        }
        listener=new ChestGui.GuiListener();
        plugin.getServer().getPluginManager().registerEvents(listener,plugin);
        player.openInventory(inventory);
        isOpen=true;
    }

    public void close()
    {
        if (isOpen)
        {
            close(true);
            onClose.accept(this,GuiCloseAction.PLUGIN);
        }
    }

    protected void close(boolean send)
    {
        if (!isOpen)
        {
            return;
        }
        HandlerList.unregisterAll(listener);
        if (send)
        {
            player.closeInventory();
        }
        isOpen=false;
    }

    public int getSize()
    {
        return size;
    }

    public void setSize(int size)
    {
        this.size=size;
    }

    public static class Builder implements GuiBuilder<ChestGui>
    {
        private final GuiItem[][] items=new GuiItem[6][9];
        private GuiItem emptyItem=GuiItem.EMPTY;
        private String title="Gui";
        private int size=6;
        private boolean prohibitAnyClick=false;
        private boolean preventClose=false;
        private Plugin plugin;
        private BiConsumer<ChestGui,GuiCloseAction> onClose=(chestGui,guiCloseAction)->
        {
        };

        Builder(Plugin plugin)
        {
            this.plugin=plugin;
        }

        public GuiItem getEmptyItem()
        {
            return emptyItem;
        }

        public Builder emptyItem(GuiItem emptyItem)
        {
            if (emptyItem==null)
            {
                return this;
            }
            this.emptyItem=emptyItem;
            return this;
        }

        @Override
        public ChestGui make(Player player)
        {
            return new ChestGui(plugin,size,title,player,emptyItem,prohibitAnyClick,preventClose,onClose,items);
        }

        public Builder title(String title)
        {
            if (title==null)
            {
                return this;
            }
            this.title=title;
            return this;
        }

        public String title()
        {
            return title;
        }

        public Builder prohibitAnyClick(boolean prohibitAnyClick)
        {
            this.prohibitAnyClick=prohibitAnyClick;
            return this;
        }

        public boolean prohibitAnyClick()
        {
            return prohibitAnyClick;
        }

        public Builder preventClose(boolean preventClose)
        {
            this.preventClose=preventClose;
            return this;
        }

        public boolean preventClose()
        {
            return preventClose;
        }

        @Override
        public Builder plugin(Plugin plugin)
        {
            if (plugin==null)
            {
                return this;
            }
            this.plugin=plugin;
            return this;
        }

        @Override
        public Plugin plugin()
        {
            return plugin;
        }

        public Builder onClose(BiConsumer<ChestGui,GuiCloseAction> onClose)
        {
            if (onClose==null)
            {
                return this;
            }
            this.onClose=onClose;
            return this;
        }

        public BiConsumer<ChestGui,GuiCloseAction> onClose()
        {
            return onClose;
        }

        public int getSize()
        {
            return size;
        }

        public Builder setSize(int size)
        {
            if (size<=0||size>6)
            {
                return this;
            }
            this.size=size;
            return this;
        }
    }

    private class GuiListener implements Listener
    {
        @EventHandler
        public void onPluginDisable(PluginDisableEvent event)
        {
            if (event.getPlugin().equals(plugin))
            {
                ChestGui.this.close(true);
                onClose.accept(ChestGui.this,GuiCloseAction.FORCED);
            }
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event)
        {
            if (event.getPlayer().equals(player))
            {
                ChestGui.this.close(false);
                onClose.accept(ChestGui.this,GuiCloseAction.FORCED);
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event)
        {
            if (!inventory.equals(event.getInventory()))
            {
                return;
            }
            if (preventClose)
            {
                player.openInventory(inventory);
            }
            else
            {
                close(false);
                onClose.accept(ChestGui.this,GuiCloseAction.PLAYER);
            }
        }

        @EventHandler(ignoreCancelled=true)
        public void onInventoryClick(InventoryClickEvent event)
        {
            if (!inventory.equals(event.getInventory()))
            {
                return;
            }

            if (!inventory.equals(event.getClickedInventory()))
            {
                event.setCancelled(prohibitAnyClick||
                                   event.getAction()==InventoryAction.MOVE_TO_OTHER_INVENTORY||
                                   event.getAction()==InventoryAction.COLLECT_TO_CURSOR);
                return;
            }
            event.setCancelled(true);
            int x=event.getSlot()/9;
            int y=event.getSlot()%9;
            GuiItem.Response response=items[x][y].getOnClick().apply(ChestGui.this,event.getClick());
            if (response.getInput()!=null)
            {
                ChestGui.this.close(true);
                onClose.accept(ChestGui.this,GuiCloseAction.TEMPORARY);
                response.getInput().clone().preventClose(true).onClose(anvilInput->open()).open(player);
                return;
            }
            if (response.getClose())
            {
                ChestGui.this.close(true);
                onClose.accept(ChestGui.this,GuiCloseAction.PLUGIN);
            }
            if (response.getBuilder()!=null)
            {
                response.getBuilder().open(player);
            }
        }
    }
}
