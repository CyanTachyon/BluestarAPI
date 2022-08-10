package me.lanzhi.bluestarapi.api.player.input;


import net.wesjd.anvilgui.version.VersionMatcher;
import net.wesjd.anvilgui.version.VersionWrapper;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public final class PlayerAnvilInput
{
    private static final VersionWrapper WRAPPER=new VersionMatcher().match();
    private final Plugin plugin;
    private final Player player;
    private final boolean preventClose;
    private final boolean prohibitAnyClick;
    private final Consumer<PlayerAnvilInput> closeListener;
    private final BiFunction<PlayerAnvilInput, String, PlayerAnvilInput.Response> completeFunction;
    private final Consumer<PlayerAnvilInput> inputLeftClickListener;
    private final Consumer<PlayerAnvilInput> inputRightClickListener;
    private final PlayerAnvilInput.ListenUp listener;
    private final String inventoryTitle;
    private final ItemStack inputLeft;
    private final ItemStack inputRight;
    private int containerId;
    private Inventory inventory;
    private boolean open;

    private PlayerAnvilInput(Plugin plugin,Player player,String inventoryTitle,String itemText,ItemStack inputLeft,ItemStack inputRight,boolean prohibitAnyClick,boolean preventClose,Consumer<PlayerAnvilInput> closeListener,Consumer<PlayerAnvilInput> inputLeftClickListener,Consumer<PlayerAnvilInput> inputRightClickListener,BiFunction<PlayerAnvilInput, String, Response> completeFunction)
    {
        this.listener=new PlayerAnvilInput.ListenUp();
        this.plugin=plugin;
        this.player=player;
        this.inventoryTitle=inventoryTitle;
        this.preventClose=preventClose;
        this.prohibitAnyClick=prohibitAnyClick;
        this.closeListener=closeListener;
        this.inputLeftClickListener=inputLeftClickListener;
        this.inputRightClickListener=inputRightClickListener;
        this.completeFunction=completeFunction;
        if (itemText!=null)
        {
            if (inputLeft==null||inputLeft.getType()==Material.AIR)
            {
                inputLeft=new ItemStack(Material.PAPER);
            }

            ItemMeta paperMeta=inputLeft.getItemMeta();
            assert paperMeta!=null;
            paperMeta.setDisplayName(itemText);
            inputLeft.setItemMeta(paperMeta);
        }
        this.inputLeft=inputLeft;
        this.inputRight=inputRight;

        this.openInventory();
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public Plugin getPlugin()
    {
        return plugin;
    }

    public Player getPlayer()
    {
        return player;
    }

    public String getInventoryTitle()
    {
        return inventoryTitle;
    }

    public ItemStack getInputLeft()
    {
        return inputLeft;
    }

    public ItemStack getInputRight()
    {
        return inputRight;
    }

    public boolean isOpen()
    {
        return open;
    }

    private void openInventory()
    {
        WRAPPER.handleInventoryCloseEvent(this.player);
        WRAPPER.setActiveContainerDefault(this.player);
        Bukkit.getPluginManager().registerEvents(this.listener,this.plugin);
        Object container=WRAPPER.newContainerAnvil(this.player,this.inventoryTitle);
        this.inventory=WRAPPER.toBukkitInventory(container);
        this.inventory.setItem(0,this.inputLeft);
        if (this.inputRight!=null)
        {
            this.inventory.setItem(1,this.inputRight);
        }

        this.containerId=WRAPPER.getNextContainerId(this.player,container);
        WRAPPER.sendPacketOpenWindow(this.player,this.containerId,this.inventoryTitle);
        WRAPPER.setActiveContainer(this.player,container);
        WRAPPER.setActiveContainerId(container,this.containerId);
        WRAPPER.addActiveContainerSlotListener(container,this.player);
        this.open=true;
    }

    public void closeInventory()
    {
        this.closeInventory(true);
    }

    private void closeInventory(boolean sendClosePacket)
    {
        if (this.open)
        {
            this.open=false;
            HandlerList.unregisterAll(this.listener);
            if (sendClosePacket)
            {
                WRAPPER.handleInventoryCloseEvent(this.player);
                WRAPPER.setActiveContainerDefault(this.player);
                WRAPPER.sendPacketCloseWindow(this.player,this.containerId);
            }

            if (this.closeListener!=null)
            {
                this.closeListener.accept(this);
            }

        }
    }

    public Inventory getInventory()
    {
        return this.inventory;
    }

    public static class Slot
    {
        public static final int INPUT_LEFT=0;
        public static final int INPUT_RIGHT=1;
        public static final int OUTPUT=2;
        private static final int[] VALUES=new int[]{0,1,2};

        public Slot()
        {
        }

        public static int[] values()
        {
            return VALUES;
        }
    }

    public static class Response
    {
        private final String text;
        private final Builder builder;

        private Response(String text,Builder builder)
        {
            this.text=text;
            this.builder=builder;
        }

        public static PlayerAnvilInput.Response close()
        {
            return new PlayerAnvilInput.Response(null,null);
        }

        public static PlayerAnvilInput.Response text(String text)
        {
            return new PlayerAnvilInput.Response(text,null);
        }

        public static PlayerAnvilInput.Response openInventory(Builder builder)
        {
            return new PlayerAnvilInput.Response(null,builder);
        }

        public String getText()
        {
            return this.text;
        }

        public Builder getGuiToOpen()
        {
            return this.builder;
        }
    }

    public static class Builder
    {
        private Consumer<PlayerAnvilInput> closeListener;
        private boolean preventClose=false;
        private boolean prohibitAnyClick=false;
        private Consumer<PlayerAnvilInput> inputLeftClickListener;
        private Consumer<PlayerAnvilInput> inputRightClickListener;
        private BiFunction<PlayerAnvilInput, String, PlayerAnvilInput.Response> completeFunction;
        private Plugin plugin;
        private String title="Repair & Name";
        private String itemText;
        private ItemStack itemLeft;
        private ItemStack itemRight;

        private Builder()
        {
        }

        public PlayerAnvilInput.Builder prohibitAnyClick(boolean b)
        {
            this.prohibitAnyClick=b;
            return this;
        }

        public PlayerAnvilInput.Builder preventClose(boolean b)
        {
            this.preventClose=b;
            return this;
        }

        public PlayerAnvilInput.Builder onClose(Consumer<PlayerAnvilInput> closeListener)
        {
            Validate.notNull(closeListener,"closeListener cannot be null");
            this.closeListener=closeListener;
            return this;
        }

        public PlayerAnvilInput.Builder onLeftInputClick(Consumer<PlayerAnvilInput> inputLeftClickListener)
        {
            this.inputLeftClickListener=inputLeftClickListener;
            return this;
        }

        public PlayerAnvilInput.Builder onRightInputClick(Consumer<PlayerAnvilInput> inputRightClickListener)
        {
            this.inputRightClickListener=inputRightClickListener;
            return this;
        }

        public PlayerAnvilInput.Builder onComplete(BiFunction<PlayerAnvilInput, String, PlayerAnvilInput.Response> completeFunction)
        {
            Validate.notNull(completeFunction,"Complete function cannot be null");
            this.completeFunction=completeFunction;
            return this;
        }

        public PlayerAnvilInput.Builder plugin(Plugin plugin)
        {
            Validate.notNull(plugin,"Plugin cannot be null");
            this.plugin=plugin;
            return this;
        }

        public PlayerAnvilInput.Builder text(String text)
        {
            Validate.notNull(text,"Text cannot be null");
            this.itemText=text;
            return this;
        }

        public PlayerAnvilInput.Builder title(String title)
        {
            Validate.notNull(title,"title cannot be null");
            this.title=title;
            return this;
        }

        public PlayerAnvilInput.Builder itemLeft(ItemStack item)
        {
            Validate.notNull(item,"item cannot be null");
            this.itemLeft=item;
            return this;
        }

        public PlayerAnvilInput.Builder itemRight(ItemStack item)
        {
            this.itemRight=item;
            return this;
        }

        public PlayerAnvilInput open(Player player)
        {
            Validate.notNull(this.plugin,"Plugin cannot be null");
            Validate.notNull(this.completeFunction,"Complete function cannot be null");
            Validate.notNull(player,"Player cannot be null");
            return new PlayerAnvilInput(this.plugin,
                                        player,
                                        this.title,
                                        this.itemText,
                                        this.itemLeft,
                                        this.itemRight,
                                        this.prohibitAnyClick,
                                        this.preventClose,
                                        this.closeListener,
                                        this.inputLeftClickListener,
                                        this.inputRightClickListener,
                                        this.completeFunction);
        }
    }

    private class ListenUp implements Listener
    {
        private ListenUp()
        {
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event)
        {
            if (!event.getInventory().equals(PlayerAnvilInput.this.inventory))
            {
                return;
            }

            if (event.getRawSlot()>=3&&!event.getAction()
                                             .equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)&&!PlayerAnvilInput.this.prohibitAnyClick)
            {
                return;
            }
            event.setCancelled(true);
            Player clicker=(Player) event.getWhoClicked();
            if (event.getRawSlot()==2)
            {
                ItemStack clicked=PlayerAnvilInput.this.inventory.getItem(2);
                if (clicked==null||clicked.getType()==Material.AIR)
                {
                    return;
                }

                Response response;
                if (clicked.hasItemMeta())
                {
                    ItemMeta meta=clicked.getItemMeta();
                    assert meta!=null;
                    response=completeFunction.apply(PlayerAnvilInput.this,meta.getDisplayName());
                }
                else
                {
                    response=completeFunction.apply(PlayerAnvilInput.this,"");
                }
                if (response.getText()!=null)
                {
                    ItemMeta meta=clicked.getItemMeta();
                    assert meta!=null;
                    meta.setDisplayName(response.getText());
                    clicked.setItemMeta(meta);
                    PlayerAnvilInput.this.inventory.setItem(0,clicked);
                }
                else if (response.getGuiToOpen()!=null)
                {
                    PlayerAnvilInput.this.closeInventory();
                    response.getGuiToOpen().open(clicker);
                }
                else
                {
                    PlayerAnvilInput.this.closeInventory();
                }
            }
            else if (event.getRawSlot()==0)
            {
                if (PlayerAnvilInput.this.inputLeftClickListener!=null)
                {
                    PlayerAnvilInput.this.inputLeftClickListener.accept(PlayerAnvilInput.this);
                }
            }
            else if (event.getRawSlot()==1&&PlayerAnvilInput.this.inputRightClickListener!=null)
            {
                PlayerAnvilInput.this.inputRightClickListener.accept(PlayerAnvilInput.this);
            }
        }

        @EventHandler
        public void onInventoryDrag(InventoryDragEvent event)
        {
            if (event.getInventory().equals(PlayerAnvilInput.this.inventory))
            {
                int[] var2=PlayerAnvilInput.Slot.values();

                for (int slot: var2)
                {
                    if (event.getRawSlots().contains(slot))
                    {
                        event.setCancelled(true);
                        break;
                    }
                }
            }

        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event)
        {
            if (PlayerAnvilInput.this.open&&event.getInventory().equals(PlayerAnvilInput.this.inventory))
            {
                PlayerAnvilInput.this.closeInventory(false);
                if (PlayerAnvilInput.this.preventClose)
                {
                    Bukkit.getScheduler().runTask(PlayerAnvilInput.this.plugin,PlayerAnvilInput.this::openInventory);
                }
            }

        }
    }
}
