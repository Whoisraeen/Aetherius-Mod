package com.aetheriusmmorpg.common.menu;

import com.aetheriusmmorpg.common.registry.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Container menu for the character creation screen.
 * No slots needed - purely for UI purposes.
 */
public class CharacterCreationMenu extends AbstractContainerMenu {

    public CharacterCreationMenu(int containerId, Inventory playerInventory) {
        super(ModMenus.CHARACTER_CREATION.get(), containerId);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true; // Always valid - players must create a character
    }
}
