package com.aetheriusmmorpg.client;

import com.aetheriusmmorpg.AetheriusMod;
import com.aetheriusmmorpg.client.keybind.ModKeyBindings;
import com.aetheriusmmorpg.client.ui.AdvancedChatHUD;
import com.aetheriusmmorpg.client.ui.ChatNotificationOverlay;
import com.aetheriusmmorpg.client.ui.PartyHUD;
import com.aetheriusmmorpg.client.ui.PartyInviteOverlay;
import com.aetheriusmmorpg.client.ui.PlayerContextMenu;
import com.aetheriusmmorpg.client.ui.screen.CharacterCreationScreen;
import com.aetheriusmmorpg.client.ui.screen.CharacterSheetScreen;
import com.aetheriusmmorpg.client.ui.screen.ChatScreen;
import com.aetheriusmmorpg.client.ui.screen.GuildScreen;
import com.aetheriusmmorpg.client.ui.screen.QuestDialogScreen;
import com.aetheriusmmorpg.client.ui.screen.MerchantScreen;
import com.aetheriusmmorpg.client.ui.screen.SkillTrainerScreen;
import com.aetheriusmmorpg.client.ui.screen.SocialScreen;
import com.aetheriusmmorpg.common.registry.ModMenus;
import com.aetheriusmmorpg.network.NetworkHandler;
import com.aetheriusmmorpg.network.packet.C2SOpenCharacterSheetPacket;
import com.aetheriusmmorpg.network.packet.C2SUseSkillPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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
            MenuScreens.register(ModMenus.GUILD.get(), GuildScreen::new);
            MenuScreens.register(ModMenus.QUEST_DIALOG.get(), QuestDialogScreen::new);
            MenuScreens.register(ModMenus.MERCHANT.get(), MerchantScreen::new);
            MenuScreens.register(ModMenus.SKILL_TRAINER.get(), SkillTrainerScreen::new);

            AetheriusMod.LOGGER.info("Client setup complete");
        });
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ModKeyBindings.CHARACTER_SHEET);
        event.register(ModKeyBindings.SKILL_TREE);
        event.register(ModKeyBindings.QUEST_LOG);
        event.register(ModKeyBindings.SOCIAL);
        event.register(ModKeyBindings.CHAT);

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

            // Social screen keybind
            if (ModKeyBindings.SOCIAL.consumeClick()) {
                if (mc.player != null) {
                    mc.setScreen(new SocialScreen());
                }
            }

            // Chat keybind - open advanced chat HUD
            if (ModKeyBindings.CHAT.consumeClick()) {
                if (mc.player != null && !AdvancedChatHUD.isOpen()) {
                    AdvancedChatHUD.open();
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

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) {
            return;
        }

        // Render advanced chat HUD
        AdvancedChatHUD.render(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight());

        // Render chat notifications
        ChatNotificationOverlay.render(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight());

        // Render party HUD
        PartyHUD.render(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight());

        // Render party invite overlay
        PartyInviteOverlay.render(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight());
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        // Advanced chat key handling
        if (AdvancedChatHUD.isOpen()) {
            if (AdvancedChatHUD.keyPressed(event.getKey(), event.getScanCode(), event.getModifiers())) {
                return;
            }
        }

        // Handle Y/N for party invites
        PartyInviteOverlay.handleKeyPress(event.getKey());
    }

    @SubscribeEvent
    public static void onCharTyped(ScreenEvent.CharacterTyped.Pre event) {
        // Advanced chat character input
        if (AdvancedChatHUD.isOpen()) {
            if (AdvancedChatHUD.charTyped(event.getCodePoint(), event.getModifiers())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        // Advanced chat scroll handling
        if (AdvancedChatHUD.isOpen()) {
            if (AdvancedChatHUD.mouseScrolled(event.getScrollDelta())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Advanced chat mouse handling
        if (AdvancedChatHUD.isOpen()) {
            if (AdvancedChatHUD.mouseClicked(event.getMouseX(), event.getMouseY(), mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        // Only handle on client side
        if (!event.getLevel().isClientSide()) {
            return;
        }

        // Check if right-clicking on another player
        if (event.getTarget() instanceof Player targetPlayer) {
            Minecraft mc = Minecraft.getInstance();

            // Don't show menu for self
            if (mc.player != null && !targetPlayer.getUUID().equals(mc.player.getUUID())) {
                // Get screen position from mouse
                int mouseX = (int) (mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth());
                int mouseY = (int) (mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight());

                // Open context menu
                mc.setScreen(new PlayerContextMenu(targetPlayer, mouseX, mouseY));
                event.setCanceled(true);
            }
        }
    }
}
