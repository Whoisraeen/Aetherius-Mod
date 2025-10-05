package com.aetheriusmmorpg.common.menu;

import com.aetheriusmmorpg.common.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * PWI-style merchant menu with buy/sell functionality.
 */
public class MerchantMenu extends AbstractContainerMenu {

    private final Container merchantInventory;
    private final String merchantId;

    public MerchantMenu(int containerId, Inventory playerInventory, String merchantId) {
        super(ModMenus.MERCHANT.get(), containerId);
        this.merchantId = merchantId;
        this.merchantInventory = new SimpleContainer(45); // 5 rows of 9 slots

        // Merchant inventory slots (display only)
        for (int row = 0; row < 5; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(merchantInventory, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        // Player inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 103 + row * 18 + 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 161 + 18));
        }
    }

    public MerchantMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, "");
    }

    public String getMerchantId() {
        return merchantId;
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

