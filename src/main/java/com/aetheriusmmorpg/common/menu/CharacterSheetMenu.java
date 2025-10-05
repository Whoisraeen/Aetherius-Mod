package com.aetheriusmmorpg.common.menu;

import com.aetheriusmmorpg.common.registry.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Menu/Container for the character sheet GUI.
 * Server-side container logic.
 */
public class CharacterSheetMenu extends AbstractContainerMenu {

    public CharacterSheetMenu(int containerId, Inventory playerInventory) {
        super(ModMenus.CHARACTER_SHEET.get(), containerId);
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
