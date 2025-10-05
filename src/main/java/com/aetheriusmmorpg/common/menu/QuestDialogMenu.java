package com.aetheriusmmorpg.common.menu;

import com.aetheriusmmorpg.common.registry.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Menu/Container for the quest dialog GUI.
 * PWI-style quest interaction interface.
 */
public class QuestDialogMenu extends AbstractContainerMenu {

    private final String npcId;

    public QuestDialogMenu(int containerId, Inventory playerInventory, String npcId) {
        super(ModMenus.QUEST_DIALOG.get(), containerId);
        this.npcId = npcId;
    }

    public QuestDialogMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, "");
    }

    public String getNpcId() {
        return npcId;
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

