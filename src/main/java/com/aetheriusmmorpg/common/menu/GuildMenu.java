package com.aetheriusmmorpg.common.menu;

import com.aetheriusmmorpg.common.registry.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Menu/Container for the guild GUI.
 * PWI-style guild management interface.
 */
public class GuildMenu extends AbstractContainerMenu {

    public GuildMenu(int containerId, Inventory playerInventory) {
        super(ModMenus.GUILD.get(), containerId);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}

