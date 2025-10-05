package com.aetheriusmmorpg.client.ui.screen;

import com.aetheriusmmorpg.client.ClientPlayerData;
import com.aetheriusmmorpg.common.menu.MerchantMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * PWI-style merchant screen for buying/selling items.
 */
public class MerchantScreen extends AbstractContainerScreen<MerchantMenu> {

    private static final int WINDOW_WIDTH = 176;
    private static final int WINDOW_HEIGHT = 222;

    private Button buyButton;
    private Button sellButton;
    private ItemStack selectedItem = ItemStack.EMPTY;
    private int buyPrice = 0;
    private int sellPrice = 0;

    public MerchantScreen(MerchantMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = WINDOW_WIDTH;
        this.imageHeight = WINDOW_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();

        // Buy button
        buyButton = Button.builder(Component.literal("Buy"),
            btn -> {
                if (!selectedItem.isEmpty() && ClientPlayerData.getGold() >= buyPrice) {
                    // TODO: Send buy packet to server
                    minecraft.player.sendSystemMessage(Component.literal("§aBought " + selectedItem.getHoverName().getString() + " for " + buyPrice + " gold"));
                }
            })
            .bounds(leftPos + 10, topPos + imageHeight - 25, 60, 20)
            .build();
        addRenderableWidget(buyButton);

        // Sell button
        sellButton = Button.builder(Component.literal("Sell"),
            btn -> {
                if (!selectedItem.isEmpty()) {
                    // TODO: Send sell packet to server
                    minecraft.player.sendSystemMessage(Component.literal("§aSold " + selectedItem.getHoverName().getString() + " for " + sellPrice + " gold"));
                }
            })
            .bounds(leftPos + 106, topPos + imageHeight - 25, 60, 20)
            .build();
        addRenderableWidget(sellButton);

        // Load merchant inventory (example items)
        loadMerchantItems();
    }

    private void loadMerchantItems() {
        // Example items - would be loaded from datapack in production
        menu.getSlot(0).set(new ItemStack(Items.DIAMOND_SWORD));
        menu.getSlot(1).set(new ItemStack(Items.DIAMOND_CHESTPLATE));
        menu.getSlot(2).set(new ItemStack(Items.GOLDEN_APPLE, 16));
        menu.getSlot(3).set(new ItemStack(Items.ENDER_PEARL, 8));
        menu.getSlot(4).set(new ItemStack(Items.POTION));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Background
        guiGraphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xE0000000);
        
        // Border
        guiGraphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + 1, 0xFF6B4DB8);
        guiGraphics.fill(leftPos, topPos + imageHeight - 1, leftPos + imageWidth, topPos + imageHeight, 0xFF6B4DB8);
        guiGraphics.fill(leftPos, topPos, leftPos + 1, topPos + imageHeight, 0xFF6B4DB8);
        guiGraphics.fill(leftPos + imageWidth - 1, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF6B4DB8);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Title
        Component titleText = Component.literal("§6Merchant");
        guiGraphics.drawString(font, titleText, (imageWidth - font.width(titleText)) / 2, 6, 0xFFFFFFFF, false);

        // Gold display
        guiGraphics.drawString(font, Component.literal("§eGold: §f" + ClientPlayerData.getGold()), 10, imageHeight - 38, 0xFFFFFFFF, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle item selection
        for (int i = 0; i < 45; i++) {
            ItemStack stack = menu.getSlot(i).getItem();
            if (!stack.isEmpty()) {
                // Calculate slot position and check if clicked
                int slotX = leftPos + 8 + (i % 9) * 18;
                int slotY = leftPos + 18 + (i / 9) * 18;
                
                if (mouseX >= slotX && mouseX < slotX + 16 && mouseY >= slotY && mouseY < slotY + 16) {
                    selectedItem = stack;
                    // Calculate prices (example formula)
                    buyPrice = 100; // Would be from datapack
                    sellPrice = 50;
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

