package com.aetheriusmmorpg.common.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Utility class for giving items to players as rewards.
 */
public class ItemRewardUtil {

    /**
     * Give an item to a player by resource location.
     * If inventory is full, drops item at player's feet.
     */
    public static boolean giveItem(ServerPlayer player, ResourceLocation itemId, int quantity) {
        Item item = ForgeRegistries.ITEMS.getValue(itemId);
        if (item == null) {
            return false;
        }

        ItemStack stack = new ItemStack(item, quantity);
        return giveItemStack(player, stack);
    }

    /**
     * Give an ItemStack to a player.
     * If inventory is full, drops item at player's feet.
     */
    public static boolean giveItemStack(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        // Try to add to inventory
        boolean added = player.getInventory().add(stack);

        // If not fully added (inventory full), drop remaining items
        if (!stack.isEmpty()) {
            ItemEntity itemEntity = new ItemEntity(
                player.level(),
                player.getX(),
                player.getY(),
                player.getZ(),
                stack.copy()
            );
            player.level().addFreshEntity(itemEntity);
        }

        return added;
    }

    /**
     * Give multiple items to a player.
     */
    public static void giveItems(ServerPlayer player, ResourceLocation itemId, int quantity) {
        // Split into stacks if quantity is large
        Item item = ForgeRegistries.ITEMS.getValue(itemId);
        if (item == null) {
            return;
        }

        int maxStackSize = item.getMaxStackSize();
        while (quantity > 0) {
            int stackSize = Math.min(quantity, maxStackSize);
            giveItem(player, itemId, stackSize);
            quantity -= stackSize;
        }
    }
}
