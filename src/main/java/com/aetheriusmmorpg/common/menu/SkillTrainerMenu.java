package com.aetheriusmmorpg.common.menu;

import com.aetheriusmmorpg.common.registry.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Menu for skill trainer interface.
 */
public class SkillTrainerMenu extends AbstractContainerMenu {

    private final String trainerId;

    public SkillTrainerMenu(int containerId, Inventory playerInventory, String trainerId) {
        super(ModMenus.SKILL_TRAINER.get(), containerId);
        this.trainerId = trainerId;
    }

    public SkillTrainerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, "");
    }

    public String getTrainerId() {
        return trainerId;
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

