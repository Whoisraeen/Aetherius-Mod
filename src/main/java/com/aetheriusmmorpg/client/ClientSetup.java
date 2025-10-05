package com.aetheriusmmorpg.client;

import com.aetheriusmmorpg.AetheriusMod;
import com.aetheriusmmorpg.client.keybind.ModKeyBindings;
import com.aetheriusmmorpg.client.ui.screen.CharacterCreationScreen;
import com.aetheriusmmorpg.client.ui.screen.CharacterSheetScreen;
import com.aetheriusmmorpg.common.registry.ModMenus;
import com.aetheriusmmorpg.network.NetworkHandler;
import com.aetheriusmmorpg.network.packet.C2SOpenCharacterSheetPacket;
import com.aetheriusmmorpg.network.packet.C2SUseSkillPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client-side initialization and event handlers.
 */
@Mod.EventBusSubscriber(modid = AetheriusMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Register menu screens
            MenuScreens.register(ModMenus.CHARACTER_SHEET.get(), CharacterSheetScreen::new);
            MenuScreens.register(ModMenus.CHARACTER_CREATION.get(), CharacterCreationScreen::new);

            AetheriusMod.LOGGER.info("Client setup complete");
        });
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ModKeyBindings.CHARACTER_SHEET);
        event.register(ModKeyBindings.SKILL_TREE);
        event.register(ModKeyBindings.QUEST_LOG);

        for (KeyMapping key : ModKeyBindings.ACTION_BAR_A) {
            event.register(key);
        }

        AetheriusMod.LOGGER.info("Registered keybindings");
    }
}

/**
 * Forge event bus handlers for client ticks.
 */
@Mod.EventBusSubscriber(modid = AetheriusMod.MOD_ID, value = Dist.CLIENT)
class ClientForgeEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();

            // Character sheet keybind
            if (ModKeyBindings.CHARACTER_SHEET.consumeClick()) {
                if (mc.player != null) {
                    NetworkHandler.sendToServer(new C2SOpenCharacterSheetPacket());
                }
            }

            // Skill bar keybinds (1-9)
            if (mc.player != null) {
                for (int i = 0; i < 9; i++) {
                    if (ModKeyBindings.ACTION_BAR_A[i].consumeClick()) {
                        // Get skill from data-driven skill bar
                        ResourceLocation skillId = com.aetheriusmmorpg.client.ClientPlayerData.getSkillInSlot(i);
                        if (skillId != null) {
                            // Simple targeting: use crosshair target or self
                            var target = mc.crosshairPickEntity;
                            int targetId = target != null ? target.getId() : -1;
                            var playerPos = mc.player.position();

                            NetworkHandler.sendToServer(new C2SUseSkillPacket(
                                skillId, targetId,
                                playerPos.x, playerPos.y, playerPos.z
                            ));
                        }
                    }
                }
            }
        }
    }
}
