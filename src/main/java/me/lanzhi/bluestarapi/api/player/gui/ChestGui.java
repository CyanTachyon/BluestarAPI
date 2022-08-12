package me.lanzhi.bluestarapi.api.player.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class ChestGui
{
    private final Plugin plugin;
    private final int size;
    private final String title;
    private final int maxPage;
    private final Player player;
    private final ItemStack emptySlot;
    private final ItemStack closeButton;
    private final ItemStack turnLeftButton;
    private final ItemStack turnRightButton;
    private final Map<Integer, GuiItem> items;
    private final boolean prohibitAnyClick;
    private final boolean preventClose;
    private final BiConsumer<ChestGui, Boolean> onClose;
    private int page;
    private Inventory inventory;
    private GuiListener listener;

    private ChestGui(Plugin plugin,int size,String title,Player player,ItemStack emptySlot,ItemStack closeButton,ItemStack turnLeftButton,ItemStack turnRightButton,Map<Integer, GuiItem> items,boolean prohibitAnyClick,boolean preventClose,BiConsumer<ChestGui, Boolean> onClose)
    {
        this.plugin=plugin;
        this.size=size*9;
        this.title=title;
        this.items=items;
        this.prohibitAnyClick=prohibitAnyClick;
        this.preventClose=preventClose;
        this.onClose=onClose;
        this.page=0;
        this.player=player;
        this.emptySlot=emptySlot;
        this.closeButton=closeButton;
        this.turnLeftButton=turnLeftButton;
        this.turnRightButton=turnRightButton;
        Map.Entry<Integer, GuiItem> maxEntry=null;
        for (Map.Entry<Integer, GuiItem> entry: this.items.entrySet())
        {
            if (maxEntry==null||entry.getKey()>maxEntry.getKey())
            {
                maxEntry=entry;
            }
        }
        int maxPage1=0;
        if (maxEntry!=null)
        {
            maxPage1=(int) Math.ceil((double) maxEntry.getKey()/size);
        }
        if (maxPage1==0)
        {
            maxPage1=1;
        }
        this.maxPage=maxPage1;
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public Plugin getPlugin()
    {
        return plugin;
    }

    public int getSize()
    {
        return size;
    }

    public String getTitle()
    {
        return title;
    }

    public int getMaxPage()
    {
        return maxPage;
    }

    public Player getPlayer()
    {
        return player;
    }

    public int getPage()
    {
        return page;
    }

    public Inventory getInventory()
    {
        return inventory;
    }

    private void openPage(int page)
    {
        if (page<0||page>=maxPage)
        {
            return;
        }
        this.page=page;

        for (int i=0;i<size;i++)
        {
            inventory.setItem(i,emptySlot);
        }

        for (int i=page*(size-9);i<(page+1)*(size-9);i++)
        {
            if (items.containsKey(i))
            {
                inventory.setItem(i-page*(size-9),items.get(i).getItem());
            }
        }
        //关闭按钮
        if (!preventClose)
        {
            inventory.setItem(size-4,closeButton);
        }
        //上一页按钮
        if (page>0)
        {
            inventory.setItem(size-5,turnLeftButton);
        }
        //下一页按钮
        if (page<maxPage-1)
        {
            inventory.setItem(size-3,turnRightButton);
        }
        player.openInventory(inventory);
    }

    private void openGui()
    {
        inventory=player.getServer().createInventory(player,size,title);
        openPage(1);
        listener=new GuiListener();
        plugin.getServer().getPluginManager().registerEvents(listener,plugin);
    }

    private void closeGui(boolean b)
    {
        if (b)
        {
            player.closeInventory();
        }
        HandlerList.unregisterAll(listener);
    }

    public static class Builder implements Cloneable
    {
        private final Map<Integer, GuiItem> items=new HashMap<>();

        private String title;
        private int size;
        private boolean prohibitAnyClick=false;
        private boolean preventClose=false;
        private ItemStack emptySlot;
        private ItemStack closeButton;
        private ItemStack turnLeft;
        private ItemStack turnRight;
        private Plugin plugin;
        private BiConsumer<ChestGui, Boolean> onClose;

        private Builder()
        {
            this.title="Chest";
            this.size=6;
            this.emptySlot=new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
            this.closeButton=new ItemStack(Material.RED_STAINED_GLASS_PANE);
            this.turnLeft=new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
            this.turnRight=new ItemStack(Material.GREEN_STAINED_GLASS_PANE);

            ItemMeta meta=emptySlot.getItemMeta();
            assert meta!=null;
            meta.setDisplayName(" ");
            emptySlot.setItemMeta(meta);

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
        }

        public Builder title(String title)
        {
            this.title=title;
            return this;
        }

        public Builder size(int size)
        {
            this.size=size;
            return this;
        }

        public Builder emptySlot(ItemStack emptySlot)
        {
            this.emptySlot=emptySlot;
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

        public Map<Integer, GuiItem> items()
        {
            return items;
        }

        public Builder plugin(Plugin plugin)
        {
            this.plugin=plugin;
            return this;
        }

        public Builder prohibitAnyClick(boolean prohibitAnyClick)
        {
            this.prohibitAnyClick=prohibitAnyClick;
            return this;
        }

        public Builder preventClose(boolean preventClose)
        {
            this.preventClose=preventClose;
            return this;
        }

        public Builder onClose(BiConsumer<ChestGui, Boolean> onClose)
        {
            this.onClose=onClose;
            return this;
        }

        public ChestGui open(Player player)
        {
            ChestGui gui=new ChestGui(plugin,
                                      size,
                                      title,
                                      player,
                                      emptySlot,
                                      closeButton,
                                      turnLeft,
                                      turnRight,
                                      items,
                                      prohibitAnyClick,
                                      preventClose,
                                      onClose);
            gui.openGui();
            return gui;
        }

        @Override
        public Builder clone()
        {
            Builder clone=new Builder();
            clone.title=this.title;
            clone.size=this.size;
            clone.emptySlot=this.emptySlot.clone();
            clone.closeButton=this.closeButton.clone();
            clone.turnLeft=this.turnLeft.clone();
            clone.turnRight=this.turnRight.clone();
            clone.plugin=this.plugin;
            for (Map.Entry<Integer, GuiItem> entry: items.entrySet())
            {
                clone.items.put(entry.getKey(),entry.getValue().clone());
            }
            return clone;
        }
    }

    public static class Response
    {
        private final boolean close;
        private final Builder builder;

        private Response(boolean close,Builder builder)
        {
            this.close=close;
            this.builder=builder;
        }

        public static Response open(Builder other)
        {
            return new Response(true,other);
        }

        public static Response close()
        {
            return new Response(false,null);
        }

        public static Response nothing()
        {
            return new Response(false,null);
        }

        private boolean getClose()
        {
            return close;
        }

        private Builder getBuilder()
        {
            return builder;
        }
    }

    private class GuiListener implements Listener
    {
        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event)
        {
            if (event.getPlayer().equals(player))
            {
                closeGui(false);
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
                closeGui(false);
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
                event.setCancelled(prohibitAnyClick||event.getAction()==InventoryAction.MOVE_TO_OTHER_INVENTORY);
                return;
            }

            event.setCancelled(true);
            if (event.getSlot()<=ChestGui.this.size-9)
            {
                GuiItem item=ChestGui.this.items.get(page*(size-9)+event.getSlot());
                if (item==null)
                {
                    return;
                }
                Response response=item.getOnClick().apply(ChestGui.this,event.getClick());
                if (response.getClose())
                {
                    ChestGui.this.closeGui(true);
                }
                if (response.getBuilder()!=null)
                {
                    response.getBuilder().open(player);
                }
                return;
            }
            if (event.getSlot()==size-5&&page>0)
            {
                openPage(page-1);
                return;
            }
            if (event.getSlot()==size-3&&page<maxPage-1)
            {
                openPage(page+1);
                return;
            }
            if (event.getSlot()==size-4&&!preventClose)
            {
                ChestGui.this.closeGui(true);
            }
        }
    }
}
