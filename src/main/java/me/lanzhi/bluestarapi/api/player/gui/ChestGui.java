package me.lanzhi.bluestarapi.api.player.gui;

import me.lanzhi.bluestarapi.api.player.input.PlayerAnvilInput;
import org.bukkit.Material;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public class ChestGui
{
    private final Plugin plugin;
    private final int size;
    private final String title;
    private final int maxPage;
    private final Player player;
    private final GuiItem emptyItem;
    private final ItemStack bottomItem;
    private final ItemStack closeButton;
    private final ItemStack turnLeftButton;
    private final ItemStack turnRightButton;
    private final Map<Integer, GuiItem> items;
    private final boolean prohibitAnyClick;
    private final boolean preventClose;
    private final BiConsumer<ChestGui, CloseAction> onClose;
    private boolean isOpen=false;
    private int page;
    private Inventory inventory;
    private GuiListener listener;

    private ChestGui(Plugin plugin,int size,String title,Player player,GuiItem emptyItem,ItemStack bottomItem,ItemStack closeButton,ItemStack turnLeftButton,ItemStack turnRightButton,Map<Integer, GuiItem> items,boolean prohibitAnyClick,boolean preventClose,BiConsumer<ChestGui, CloseAction> onClose)
    {
        this.plugin=plugin;
        this.size=size*9;
        this.title=title;
        this.emptyItem=emptyItem;
        this.items=items;
        this.prohibitAnyClick=prohibitAnyClick;
        this.preventClose=preventClose;
        this.onClose=onClose;
        this.page=0;
        this.player=player;
        this.bottomItem=bottomItem;
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
            maxPage1=(int) Math.ceil((double) maxEntry.getKey()/(this.size-9));
        }
        if (maxPage1==0)
        {
            maxPage1=1;
        }
        this.maxPage=maxPage1;

        init();
        openGui();
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

    private void setPage(int page)
    {
        if (page<0||page>=maxPage)
        {
            return;
        }
        this.page=page;

        for (int i=0;i<size-9;i++)
        {
            inventory.setItem(i,emptyItem.getItem());
        }

        for (int i=size-9;i<size;i++)
        {
            inventory.setItem(i,bottomItem);
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
            inventory.setItem(size-5,closeButton);
        }
        //上一页按钮
        if (page>0)
        {
            inventory.setItem(size-6,turnLeftButton);
        }
        //下一页按钮
        if (page<maxPage-1)
        {
            inventory.setItem(size-4,turnRightButton);
        }
    }

    private GuiItem getItem(int index)
    {
        GuiItem item=ChestGui.this.items.get(page*(size-9)+index);
        if (item==null)
        {
            return emptyItem;
        }
        return item;
    }

    public Inventory getInventory()
    {
        return inventory;
    }

    private void init()
    {
        inventory=player.getServer().createInventory(player,size,title);
        setPage(0);
    }

    private void openGui()
    {
        if (!plugin.isEnabled())
        {
            return;
        }
        listener=new GuiListener();
        plugin.getServer().getPluginManager().registerEvents(listener,plugin);
        setPage(page);
        player.openInventory(inventory);
    }

    private void closeGui(boolean send)
    {
        HandlerList.unregisterAll(listener);
        if (send)
        {
            player.closeInventory();
        }
        isOpen=false;
    }

    public static class Builder implements Cloneable
    {
        private final Map<Integer, GuiItem> items=new HashMap<>();

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
        private BiConsumer<ChestGui, CloseAction> onClose;

        private Builder()
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
                this.bottomItem=new ItemStack(Objects.requireNonNull(Material.getMaterial("STAINED_GLASS_PANE")),1,(short)15);
                this.closeButton=new ItemStack(Objects.requireNonNull(Material.getMaterial("STAINED_GLASS_PANE")),1,(short)14);
                this.turnLeft=new ItemStack(Objects.requireNonNull(Material.getMaterial("STAINED_GLASS_PANE")),1,(short)13);
                this.turnRight=new ItemStack(Objects.requireNonNull(Material.getMaterial("STAINED_GLASS_PANE")),1,(short)13);
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

        public Builder onClose(BiConsumer<ChestGui, CloseAction> onClose)
        {
            this.onClose=onClose;
            return this;
        }

        public Builder emptyItem(GuiItem item)
        {
            this.emptyItem=item;
            return this;
        }

        public ChestGui open(Player player)
        {
            return new ChestGui(plugin,
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

        @Override
        public Builder clone()
        {
            Builder clone=new Builder();
            clone.title=this.title;
            clone.size=this.size;
            clone.bottomItem=this.bottomItem.clone();
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

    public enum CloseAction
    {
        FORCED,
        PLUGIN,
        PLAYER,
        TEMPORARY
    }

    public static class Response
    {
        private final boolean close;
        private final Builder builder;
        private final PlayerAnvilInput.Builder input;

        private Response(boolean close,Builder builder,PlayerAnvilInput.Builder input)
        {
            this.close=close;
            this.builder=builder;
            this.input=input;
        }

        public static Response open(Builder other)
        {
            return new Response(true,other,null);
        }

        public static Response input(PlayerAnvilInput.Builder input)
        {
            return new Response(false,null,input);
        }

        public static Response close()
        {
            return new Response(false,null,null);
        }

        public static Response nothing()
        {
            return new Response(false,null,null);
        }

        private boolean getClose()
        {
            return close;
        }

        private Builder getBuilder()
        {
            return builder;
        }

        private PlayerAnvilInput.Builder getInput()
        {
            return input;
        }
    }

    private class GuiListener implements Listener
    {
        @EventHandler
        public void onPluginDisable(PluginDisableEvent event)
        {
            if (event.getPlugin()==plugin)
            {
                ChestGui.this.closeGui(true);
                onClose.accept(ChestGui.this,CloseAction.FORCED);
            }
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event)
        {
            if (event.getPlayer().equals(player))
            {
                closeGui(false);
                onClose.accept(ChestGui.this,CloseAction.FORCED);
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
                onClose.accept(ChestGui.this,CloseAction.PLAYER);
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
                event.setCancelled(prohibitAnyClick||event.getAction()==InventoryAction.MOVE_TO_OTHER_INVENTORY||event.getAction()==InventoryAction.COLLECT_TO_CURSOR);
                return;
            }

            event.setCancelled(true);
            if (event.getSlot()<=ChestGui.this.size-9)
            {
                GuiItem item=ChestGui.this.getItem(event.getSlot());
                Response response=item.getOnClick().apply(ChestGui.this,event.getClick());
                if (response.getInput()!=null)
                {
                    ChestGui.this.closeGui(true);
                    onClose.accept(ChestGui.this,CloseAction.TEMPORARY);
                    response.getInput().clone().preventClose(true).onClose(anvilInput->openGui()).open(player);
                    return;
                }
                if (response.getClose())
                {
                    ChestGui.this.closeGui(true);
                    onClose.accept(ChestGui.this,CloseAction.PLUGIN);
                }
                if (response.getBuilder()!=null)
                {
                    response.getBuilder().open(player);
                }
                return;
            }
            if (event.getSlot()==size-6&&page>0)
            {
                setPage(page-1);
                return;
            }
            if (event.getSlot()==size-4&&page<maxPage-1)
            {
                setPage(page+1);
                return;
            }
            if (event.getSlot()==size-5&&!preventClose)
            {
                ChestGui.this.closeGui(true);
                onClose.accept(ChestGui.this,CloseAction.PLAYER);
            }
        }
    }
}
