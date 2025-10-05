package com.aetheriusmmorpg.client.ui;

import com.aetheriusmmorpg.client.ClientPartyData;
import com.aetheriusmmorpg.network.NetworkHandler;
import com.aetheriusmmorpg.network.packet.party.C2SPartyActionPacket;
import com.aetheriusmmorpg.network.packet.friend.C2SFriendActionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Context menu that appears when right-clicking a player.
 * PWI-style interaction menu (not full screen, appears next to player).
 */
public class PlayerContextMenu extends Screen {

    private final Player targetPlayer;
    private final int screenX;
    private final int screenY;
    private final List<MenuOption> options = new ArrayList<>();

    private static final int MENU_WIDTH = 140;
    private static final int OPTION_HEIGHT = 20;
    private static final int PADDING = 4;

    public PlayerContextMenu(Player targetPlayer, int screenX, int screenY) {
        super(Component.literal("Player Menu"));
        this.targetPlayer = targetPlayer;
        this.screenX = Math.min(screenX, Minecraft.getInstance().getWindow().getGuiScaledWidth() - MENU_WIDTH - 10);
        this.screenY = Math.min(screenY, Minecraft.getInstance().getWindow().getGuiScaledHeight() - 200);

        buildMenuOptions();
    }

    private void buildMenuOptions() {
        Minecraft mc = Minecraft.getInstance();
        Player localPlayer = mc.player;

        if (localPlayer == null || targetPlayer.getUUID().equals(localPlayer.getUUID())) {
            return; // Don't show menu for self
        }

        // Add Friend option
        options.add(new MenuOption(
            "§bAdd Friend",
            () -> {
                NetworkHandler.sendToServer(new C2SFriendActionPacket(
                    C2SFriendActionPacket.FriendAction.SEND_REQUEST,
                    targetPlayer.getUUID()
                ));
                this.onClose();
            }
        ));

        // Party options
        if (ClientPartyData.hasParty()) {
            if (ClientPartyData.isLeader(localPlayer.getUUID())) {
                // Leader can invite
                options.add(new MenuOption(
                    "§6Invite to Party",
                    () -> {
                        NetworkHandler.sendToServer(new C2SPartyActionPacket(
                            C2SPartyActionPacket.PartyAction.INVITE,
                            targetPlayer.getUUID()
                        ));
                        this.onClose();
                    }
                ));

                // Leader can kick (if target is in party)
                if (ClientPartyData.getMemberIds().contains(targetPlayer.getUUID())) {
                    options.add(new MenuOption(
                        "§cKick from Party",
                        () -> {
                            NetworkHandler.sendToServer(new C2SPartyActionPacket(
                                C2SPartyActionPacket.PartyAction.KICK,
                                targetPlayer.getUUID()
                            ));
                            this.onClose();
                        }
                    ));

                    options.add(new MenuOption(
                        "§eTransfer Leadership",
                        () -> {
                            NetworkHandler.sendToServer(new C2SPartyActionPacket(
                                C2SPartyActionPacket.PartyAction.TRANSFER_LEADERSHIP,
                                targetPlayer.getUUID()
                            ));
                            this.onClose();
                        }
                    ));
                }
            }
        } else {
            // Not in party - can create and invite
            options.add(new MenuOption(
                "§aInvite to Party",
                () -> {
                    // Create party first, then invite
                    NetworkHandler.sendToServer(new C2SPartyActionPacket(
                        C2SPartyActionPacket.PartyAction.CREATE
                    ));
                    // Small delay then invite
                    Minecraft.getInstance().execute(() -> {
                        NetworkHandler.sendToServer(new C2SPartyActionPacket(
                            C2SPartyActionPacket.PartyAction.INVITE,
                            targetPlayer.getUUID()
                        ));
                    });
                    this.onClose();
                }
            ));
        }

        // Trade option
        options.add(new MenuOption(
            "§eRequest Trade",
            () -> {
                // Send trade request packet
                com.aetheriusmmorpg.network.NetworkHandler.sendToServer(
                    com.aetheriusmmorpg.network.packet.trade.C2STradeRequestPacket.request(targetPlayer.getUUID())
                );
                localPlayer.sendSystemMessage(Component.literal("§aTrade request sent to " + targetPlayer.getName().getString()));
                this.onClose();
            }
        ));

        // Whisper option
        options.add(new MenuOption(
            "§dSend Message",
            () -> {
                // Open chat with /tell command pre-filled
                mc.setScreen(null);
                mc.gui.getChat().addRecentChat("/tell " + targetPlayer.getName().getString() + " ");
            }
        ));

        // View Profile
        options.add(new MenuOption(
            "§7View Profile",
            () -> {
                // Open player profile screen
                minecraft.setScreen(new com.aetheriusmmorpg.client.ui.screen.PlayerProfileScreen(targetPlayer));
                localPlayer.sendSystemMessage(Component.literal("§7Viewing " + targetPlayer.getName().getString() + "'s profile"));
                this.onClose();
            }
        ));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Semi-transparent background
        int menuHeight = options.size() * OPTION_HEIGHT + PADDING * 2;

        // Draw menu background
        guiGraphics.fill(screenX, screenY, screenX + MENU_WIDTH, screenY + menuHeight, 0xE0000000);

        // Draw border
        guiGraphics.fill(screenX, screenY, screenX + MENU_WIDTH, screenY + 1, 0xFF6B4DB8); // Top
        guiGraphics.fill(screenX, screenY + menuHeight - 1, screenX + MENU_WIDTH, screenY + menuHeight, 0xFF6B4DB8); // Bottom
        guiGraphics.fill(screenX, screenY, screenX + 1, screenY + menuHeight, 0xFF6B4DB8); // Left
        guiGraphics.fill(screenX + MENU_WIDTH - 1, screenY, screenX + MENU_WIDTH, screenY + menuHeight, 0xFF6B4DB8); // Right

        // Draw player name header
        Component headerText = Component.literal("§f" + targetPlayer.getName().getString());
        guiGraphics.drawString(font, headerText, screenX + MENU_WIDTH / 2 - font.width(headerText) / 2, screenY + 4, 0xFFFFFFFF);

        // Draw separator
        guiGraphics.fill(screenX + 4, screenY + 18, screenX + MENU_WIDTH - 4, screenY + 19, 0xFF444444);

        // Draw options
        int yPos = screenY + PADDING + 16;
        for (MenuOption option : options) {
            boolean hovered = mouseX >= screenX && mouseX < screenX + MENU_WIDTH &&
                            mouseY >= yPos && mouseY < yPos + OPTION_HEIGHT;

            // Highlight on hover
            if (hovered) {
                guiGraphics.fill(screenX + 2, yPos, screenX + MENU_WIDTH - 2, yPos + OPTION_HEIGHT, 0x80FFFFFF);
            }

            guiGraphics.drawString(font, option.label, screenX + 8, yPos + 6, 0xFFFFFFFF);
            yPos += OPTION_HEIGHT;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int yPos = screenY + PADDING + 16;

        for (MenuOption option : options) {
            if (mouseX >= screenX && mouseX < screenX + MENU_WIDTH &&
                mouseY >= yPos && mouseY < yPos + OPTION_HEIGHT) {
                option.action.run();
                return true;
            }
            yPos += OPTION_HEIGHT;
        }

        // Click outside closes menu
        this.onClose();
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class MenuOption {
        final String label;
        final Runnable action;

        MenuOption(String label, Runnable action) {
            this.label = label;
            this.action = action;
        }
    }
}
