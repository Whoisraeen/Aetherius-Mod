package com.aetheriusmmorpg.client.ui.hud;

import com.aetheriusmmorpg.AetheriusMod;
import com.aetheriusmmorpg.client.keybind.ModKeyBindings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Renders the skill bar HUD showing skills 1-9 with cooldowns and keybinds.
 */
@Mod.EventBusSubscriber(modid = AetheriusMod.MOD_ID, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public class SkillBarHud {

    private static final int SLOT_SIZE = 32;
    private static final int SLOT_SPACING = 4;
    private static final int BAR_START_Y = 10;

    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay().id().toString().equals("minecraft:hotbar")) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.options.hideGui) return;

            GuiGraphics guiGraphics = event.getGuiGraphics();
            int screenWidth = mc.getWindow().getGuiScaledWidth();

            renderSkillBar(guiGraphics, screenWidth);
        }
    }

    private static void renderSkillBar(GuiGraphics guiGraphics, int screenWidth) {
        int totalWidth = (SLOT_SIZE + SLOT_SPACING) * 9 - SLOT_SPACING;
        int startX = (screenWidth - totalWidth) / 2;

        for (int i = 0; i < 9; i++) {
            int slotX = startX + (SLOT_SIZE + SLOT_SPACING) * i;
            renderSkillSlot(guiGraphics, slotX, BAR_START_Y, i + 1);
        }
    }

    private static void renderSkillSlot(GuiGraphics guiGraphics, int x, int y, int slotNumber) {
        // Draw slot background (dark gray)
        guiGraphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF333333);

        // Draw slot border (light gray)
        guiGraphics.fill(x, y, x + SLOT_SIZE, y + 1, 0xFF888888); // Top
        guiGraphics.fill(x, y + SLOT_SIZE - 1, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF888888); // Bottom
        guiGraphics.fill(x, y, x + 1, y + SLOT_SIZE, 0xFF888888); // Left
        guiGraphics.fill(x + SLOT_SIZE - 1, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF888888); // Right

        // Draw keybind number (1-9)
        String keyText = String.valueOf(slotNumber);
        Minecraft mc = Minecraft.getInstance();
        int textX = x + SLOT_SIZE / 2 - mc.font.width(keyText) / 2;
        int textY = y + SLOT_SIZE / 2 - mc.font.lineHeight / 2;
        guiGraphics.drawString(mc.font, keyText, textX, textY, 0xFFFFFF, true);

        // Draw cooldown overlay
        net.minecraft.resources.ResourceLocation skillId = com.aetheriusmmorpg.client.ClientPlayerData.getSkillInSlot(slotNumber - 1);
        if (skillId != null && mc.level != null) {
            long currentTick = mc.level.getGameTime();
            int remainingTicks = com.aetheriusmmorpg.client.ClientPlayerData.getRemainingCooldown(skillId, currentTick);
            if (remainingTicks > 0) {
                // Draw semi-transparent overlay
                int cooldownHeight = (int) ((remainingTicks / 60.0f) * SLOT_SIZE);
                guiGraphics.fill(x + 1, y + SLOT_SIZE - cooldownHeight - 1,
                    x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, 0x80000000);

                // Draw cooldown text
                String cooldownText = String.format("%.1f", remainingTicks / 20.0f);
                int cdTextX = x + SLOT_SIZE / 2 - mc.font.width(cooldownText) / 2;
                int cdTextY = y + SLOT_SIZE - mc.font.lineHeight - 2;
                guiGraphics.drawString(mc.font, cooldownText, cdTextX, cdTextY, 0xFFFF00, true);
            }
        }

        // TODO: Draw skill icon when skills are loaded
        // TODO: Highlight on key press
    }
}
