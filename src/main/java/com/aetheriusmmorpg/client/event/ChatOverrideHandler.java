package com.aetheriusmmorpg.client.event;

import com.aetheriusmmorpg.AetheriusMod;
import com.aetheriusmmorpg.client.ui.AdvancedChatHUD;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Overrides Minecraft's default chat screen with the advanced chat HUD.
 */
@Mod.EventBusSubscriber(modid = AetheriusMod.MOD_ID, value = Dist.CLIENT)
public class ChatOverrideHandler {

    /**
     * Cancel vanilla chat screen and open our advanced chat instead.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onScreenOpen(ScreenEvent.Opening event) {
        if (event.getScreen() instanceof ChatScreen) {
            // Cancel vanilla chat screen
            event.setCanceled(true);

            // Open our advanced chat HUD
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.setScreen(null); // Keep in-game
                AdvancedChatHUD.open();
            }
        }
    }
}
