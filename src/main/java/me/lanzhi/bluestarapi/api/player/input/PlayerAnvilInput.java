package me.lanzhi.bluestarapi.api.player.input;


import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public final class PlayerAnvilInput
{
    private final AnvilGUI anvilGUI;

    public PlayerAnvilInput(AnvilGUI anvilGUI)
    {
        this.anvilGUI=anvilGUI;
    }

    public void closeInventory()
    {
        anvilGUI.closeInventory();
    }

    public Inventory getInventory()
    {
        return anvilGUI.getInventory();
    }

    public static class Builder
    {
        AnvilGUI.Builder builder;

        private Builder()
        {
            builder=new AnvilGUI.Builder();
        }

        public Builder preventClose()
        {
            builder.preventClose();
            return this;
        }

        public Builder onClose(Consumer<Player> closeListener)
        {
            builder.onClose(closeListener);
            return this;
        }

        public Builder onLeftInputClick(Consumer<Player> inputLeftClickListener)
        {
            builder.onLeftInputClick(inputLeftClickListener);
            return this;
        }

        public Builder onRightInputClick(Consumer<Player> inputRightClickListener)
        {
            builder.onRightInputClick(inputRightClickListener);
            return this;
        }

        public Builder onComplete(BiFunction<Player, String, Response> completeFunction)
        {
            builder.onComplete((player,s)->completeFunction.apply(player,s).response);
            return this;
        }

        public Builder plugin(Plugin plugin)
        {
            builder.plugin(plugin);
            return this;
        }

        public Builder text(String text)
        {
            builder.text(text);
            return this;
        }

        public Builder title(String title)
        {
            builder.title(title);
            return this;
        }

        @Deprecated
        public Builder item(ItemStack item)
        {
            return this.itemLeft(item);
        }

        public Builder itemLeft(ItemStack item)
        {
            builder.itemLeft(item);
            return this;
        }

        public Builder itemRight(ItemStack item)
        {
            builder.itemRight(item);
            return this;
        }

        public PlayerAnvilInput open(Player player)
        {
            return new PlayerAnvilInput(builder.open(player));
        }
    }

    public static class Response
    {
        private final AnvilGUI.Response response;

        public Response(AnvilGUI.Response response)
        {
            this.response=response;
        }

        public static Response close()
        {
            return new Response(AnvilGUI.Response.close());
        }

        public static Response text(String text)
        {
            return new Response(AnvilGUI.Response.text(text));
        }

        public static Response openInventory(Inventory inventory)
        {
            return new Response(AnvilGUI.Response.openInventory(inventory));
        }

        public String getText()
        {
            return response.getText();
        }

        public Inventory getInventoryToOpen()
        {
            return response.getInventoryToOpen();
        }
    }
}
