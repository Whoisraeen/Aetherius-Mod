package com.aetheriusmmorpg.client.ui.screen;

import com.aetheriusmmorpg.client.ClientFriendData;
import com.aetheriusmmorpg.client.ClientPartyData;
import com.aetheriusmmorpg.network.NetworkHandler;
import com.aetheriusmmorpg.network.packet.party.C2SPartyActionPacket;
import com.aetheriusmmorpg.network.packet.friend.C2SFriendActionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * PWI-style Social screen with tabs for Friends, Party, and Guild.
 * Provides GUI-based interface for social interactions.
 */
public class SocialScreen extends Screen {

    private static final int WINDOW_WIDTH = 280;
    private static final int WINDOW_HEIGHT = 360;

    private static final int TAB_WIDTH = 90;
    private static final int TAB_HEIGHT = 24;

    private int windowX;
    private int windowY;

    private Tab currentTab = Tab.FRIENDS;

    // Friend list (TODO: Replace with actual friend system)
    private final List<FriendEntry> friends = new ArrayList<>();

    // Buttons
    private Button friendsTabButton;
    private Button partyTabButton;
    private Button guildTabButton;

    private Button addFriendButton;
    private Button leavePartyButton;

    public SocialScreen() {
        super(Component.literal("Social"));
    }

    @Override
    protected void init() {
        super.init();

        this.windowX = (this.width - WINDOW_WIDTH) / 2;
        this.windowY = (this.height - WINDOW_HEIGHT) / 2;

        // Tab buttons
        friendsTabButton = Button.builder(Component.literal("Friends"),
            btn -> switchTab(Tab.FRIENDS))
            .bounds(windowX, windowY, TAB_WIDTH, TAB_HEIGHT)
            .build();

        partyTabButton = Button.builder(Component.literal("Party"),
            btn -> switchTab(Tab.PARTY))
            .bounds(windowX + TAB_WIDTH, windowY, TAB_WIDTH, TAB_HEIGHT)
            .build();

        guildTabButton = Button.builder(Component.literal("Guild"),
            btn -> switchTab(Tab.GUILD))
            .bounds(windowX + TAB_WIDTH * 2, windowY, TAB_WIDTH, TAB_HEIGHT)
            .build();

        addWidget(friendsTabButton);
        addWidget(partyTabButton);
        addWidget(guildTabButton);

        // Action buttons (change based on tab)
        updateActionButtons();
    }

    private void switchTab(Tab tab) {
        this.currentTab = tab;
        updateActionButtons();
    }

    private void updateActionButtons() {
        // Remove old action buttons
        if (addFriendButton != null) {
            removeWidget(addFriendButton);
        }
        if (leavePartyButton != null) {
            removeWidget(leavePartyButton);
        }

        int buttonY = windowY + WINDOW_HEIGHT - 30;

        switch (currentTab) {
            case FRIENDS:
                addFriendButton = Button.builder(Component.literal("Add Friend"),
                    btn -> {
                        // Open add friend dialog
                        if (minecraft != null) {
                            minecraft.setScreen(new com.aetheriusmmorpg.client.ui.screen.AddFriendDialog());
                        }
                    })
                    .bounds(windowX + 10, buttonY, 100, 20)
                    .build();
                addWidget(addFriendButton);
                break;

            case PARTY:
                if (ClientPartyData.hasParty()) {
                    leavePartyButton = Button.builder(Component.literal("Leave Party"),
                        btn -> {
                            NetworkHandler.sendToServer(new C2SPartyActionPacket(
                                C2SPartyActionPacket.PartyAction.LEAVE
                            ));
                            this.onClose();
                        })
                        .bounds(windowX + 10, buttonY, 100, 20)
                        .build();
                    addWidget(leavePartyButton);
                }
                break;

            case GUILD:
                // Guild button - open guild screen
                if (com.aetheriusmmorpg.client.ClientGuildData.hasGuild()) {
                    // View guild button
                    Button viewGuildButton = Button.builder(Component.literal("View Guild"),
                        btn -> {
                            if (minecraft != null && minecraft.player != null) {
                                minecraft.player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                                    (containerId, playerInventory, player) -> new com.aetheriusmmorpg.common.menu.GuildMenu(containerId, playerInventory),
                                    Component.literal("Guild")
                                ));
                            }
                        })
                        .bounds(windowX + 10, buttonY, 100, 20)
                        .build();
                    addWidget(viewGuildButton);
                }
                break;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Background
        renderBackground(guiGraphics);

        // Window background
        guiGraphics.fill(windowX, windowY + TAB_HEIGHT, windowX + WINDOW_WIDTH, windowY + WINDOW_HEIGHT, 0xE0000000);

        // Window border
        guiGraphics.fill(windowX, windowY + TAB_HEIGHT, windowX + WINDOW_WIDTH, windowY + TAB_HEIGHT + 1, 0xFF6B4DB8); // Top
        guiGraphics.fill(windowX, windowY + WINDOW_HEIGHT - 1, windowX + WINDOW_WIDTH, windowY + WINDOW_HEIGHT, 0xFF6B4DB8); // Bottom
        guiGraphics.fill(windowX, windowY + TAB_HEIGHT, windowX + 1, windowY + WINDOW_HEIGHT, 0xFF6B4DB8); // Left
        guiGraphics.fill(windowX + WINDOW_WIDTH - 1, windowY + TAB_HEIGHT, windowX + WINDOW_WIDTH, windowY + WINDOW_HEIGHT, 0xFF6B4DB8); // Right

        // Render tabs (highlight current tab)
        renderTab(guiGraphics, friendsTabButton, currentTab == Tab.FRIENDS);
        renderTab(guiGraphics, partyTabButton, currentTab == Tab.PARTY);
        renderTab(guiGraphics, guildTabButton, currentTab == Tab.GUILD);

        // Render tab content
        switch (currentTab) {
            case FRIENDS:
                renderFriendsTab(guiGraphics, mouseX, mouseY);
                break;
            case PARTY:
                renderPartyTab(guiGraphics, mouseX, mouseY);
                break;
            case GUILD:
                renderGuildTab(guiGraphics, mouseX, mouseY);
                break;
        }

        // Render buttons and widgets
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderTab(GuiGraphics guiGraphics, Button button, boolean active) {
        int x = button.getX();
        int y = button.getY();
        int width = button.getWidth();
        int height = button.getHeight();

        if (active) {
            // Active tab - highlighted
            guiGraphics.fill(x, y, x + width, y + height, 0xE06B4DB8);
            guiGraphics.fill(x, y, x + width, y + 1, 0xFF6B4DB8); // Top border
            guiGraphics.fill(x, y, x + 1, y + height, 0xFF6B4DB8); // Left border
            guiGraphics.fill(x + width - 1, y, x + width, y + height, 0xFF6B4DB8); // Right border
        } else {
            // Inactive tab - darker
            guiGraphics.fill(x, y, x + width, y + height, 0xE0404040);
            guiGraphics.fill(x, y, x + width, y + 1, 0xFF6B4DB8); // Top border
            guiGraphics.fill(x, y, x + 1, y + height, 0xFF6B4DB8); // Left border
            guiGraphics.fill(x + width - 1, y, x + width, y + height, 0xFF6B4DB8); // Right border
        }

        // Draw tab label
        Component label = button.getMessage();
        int textX = x + (width - font.width(label)) / 2;
        int textY = y + (height - 8) / 2;
        guiGraphics.drawString(font, label, textX, textY, active ? 0xFFFFFFFF : 0xFFAAAAAA, false);
    }

    private void renderFriendsTab(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int yPos = windowY + TAB_HEIGHT + 10;

        // Pending incoming requests
        Set<UUID> pendingIncoming = ClientFriendData.getPendingIncoming();
        if (!pendingIncoming.isEmpty()) {
            guiGraphics.drawString(font, Component.literal("§6Friend Requests:"), windowX + 10, yPos, 0xFFFFFFFF, false);
            yPos += 15;

            for (UUID requesterId : pendingIncoming) {
                String requesterName = getPlayerName(requesterId);
                guiGraphics.drawString(font, Component.literal("  §f" + requesterName), windowX + 10, yPos, 0xFFFFFFFF, false);

                // Accept button
                boolean acceptHovered = mouseX >= windowX + 150 && mouseX < windowX + 200 &&
                                       mouseY >= yPos - 2 && mouseY < yPos + 10;
                guiGraphics.drawString(font, Component.literal(acceptHovered ? "§a[Accept]" : "§7[Accept]"),
                    windowX + 150, yPos, 0xFFFFFFFF, false);

                // Decline button
                boolean declineHovered = mouseX >= windowX + 210 && mouseX < windowX + 260 &&
                                        mouseY >= yPos - 2 && mouseY < yPos + 10;
                guiGraphics.drawString(font, Component.literal(declineHovered ? "§c[Decline]" : "§7[Decline]"),
                    windowX + 210, yPos, 0xFFFFFFFF, false);

                yPos += 15;
            }
            yPos += 10;
        }

        // Friend list
        Set<UUID> friendIds = ClientFriendData.getFriends();
        if (friendIds.isEmpty()) {
            Component message = Component.literal("§7No friends yet");
            guiGraphics.drawString(font, message, windowX + 10, yPos, 0xFFFFFFFF, false);

            Component hint = Component.literal("§7Right-click players to add friends");
            guiGraphics.drawString(font, hint, windowX + 10, yPos + 15, 0xFFAAAAAA, false);
        } else {
            guiGraphics.drawString(font, Component.literal("§bFriends (" + friendIds.size() + "):"), windowX + 10, yPos, 0xFFFFFFFF, false);
            yPos += 15;

            for (UUID friendId : friendIds) {
                String friendName = getPlayerName(friendId);
                boolean online = isPlayerOnline(friendId);
                String statusColor = online ? "§a" : "§7";
                guiGraphics.drawString(font, Component.literal("  " + statusColor + friendName), windowX + 10, yPos, 0xFFFFFFFF, false);
                yPos += 15;
            }
        }
    }

    private boolean isPlayerOnline(UUID playerId) {
        if (minecraft != null && minecraft.level != null) {
            return minecraft.level.getPlayerByUUID(playerId) != null;
        }
        return false;
    }

    private void renderPartyTab(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int yPos = windowY + TAB_HEIGHT + 10;

        if (!ClientPartyData.hasParty()) {
            // No party message
            Component message = Component.literal("§7Not in a party");
            guiGraphics.drawString(font, message, windowX + 10, yPos, 0xFFFFFFFF, false);

            Component hint = Component.literal("§7Right-click players to invite to party");
            guiGraphics.drawString(font, hint, windowX + 10, yPos + 15, 0xFFAAAAAA, false);
        } else {
            // Party info
            UUID leaderId = ClientPartyData.getLeaderId();
            List<UUID> memberIds = ClientPartyData.getMemberIds();

            // Leader
            guiGraphics.drawString(font, Component.literal("§6Party Leader:"), windowX + 10, yPos, 0xFFFFFFFF, false);
            yPos += 15;

            String leaderName = getPlayerName(leaderId);
            guiGraphics.drawString(font, Component.literal("  §f" + leaderName), windowX + 10, yPos, 0xFFFFFFFF, false);
            yPos += 20;

            // Members
            if (memberIds.size() > 1) {
                guiGraphics.drawString(font, Component.literal("§bMembers:"), windowX + 10, yPos, 0xFFFFFFFF, false);
                yPos += 15;

                for (UUID memberId : memberIds) {
                    if (!memberId.equals(leaderId)) {
                        String memberName = getPlayerName(memberId);
                        guiGraphics.drawString(font, Component.literal("  §f" + memberName), windowX + 10, yPos, 0xFFFFFFFF, false);
                        yPos += 15;
                    }
                }
            }

            // Party size
            yPos += 10;
            guiGraphics.drawString(font,
                Component.literal("§7Party Size: " + memberIds.size() + "/4"),
                windowX + 10, yPos, 0xFFAAAAAA, false);
        }
    }

    private void renderGuildTab(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int yPos = windowY + TAB_HEIGHT + 10;

        if (!com.aetheriusmmorpg.client.ClientGuildData.hasGuild()) {
            // Not in a guild
            Component message = Component.literal("§7Not in a guild");
            guiGraphics.drawString(font, message, windowX + 10, yPos, 0xFFFFFFFF, false);

            Component hint = Component.literal("§7Find a Guild Master NPC to create or join a guild");
            guiGraphics.drawString(font, hint, windowX + 10, yPos + 15, 0xFFAAAAAA, false);
        } else {
            // Guild info preview
            guiGraphics.drawString(font, Component.literal("§6Guild:"), windowX + 10, yPos, 0xFFFFFFFF, false);
            yPos += 15;

            String guildName = com.aetheriusmmorpg.client.ClientGuildData.getGuildName();
            String guildTag = com.aetheriusmmorpg.client.ClientGuildData.getGuildTag();
            guiGraphics.drawString(font, Component.literal("  §f[" + guildTag + "] " + guildName), windowX + 10, yPos, 0xFFFFFFFF, false);
            yPos += 20;

            int level = com.aetheriusmmorpg.client.ClientGuildData.getLevel();
            guiGraphics.drawString(font, Component.literal("§bLevel: §f" + level), windowX + 10, yPos, 0xFFFFFFFF, false);
            yPos += 15;

            int members = com.aetheriusmmorpg.client.ClientGuildData.getMembers().size();
            int maxMembers = com.aetheriusmmorpg.client.ClientGuildData.getMaxMembers();
            guiGraphics.drawString(font, Component.literal("§bMembers: §f" + members + " / " + maxMembers), windowX + 10, yPos, 0xFFFFFFFF, false);
            yPos += 20;

            guiGraphics.drawString(font, Component.literal("§7Click 'View Guild' to manage your guild"), windowX + 10, yPos, 0xFFAAAAAA, false);
        }
    }

    private String getPlayerName(UUID playerId) {
        if (minecraft != null && minecraft.level != null) {
            Player player = minecraft.level.getPlayerByUUID(playerId);
            if (player != null) {
                return player.getName().getString();
            }
        }
        return playerId.toString().substring(0, 8) + "...";
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle friend request Accept/Decline clicks in Friends tab
        if (currentTab == Tab.FRIENDS) {
            int yPos = windowY + TAB_HEIGHT + 10;
            Set<UUID> pendingIncoming = ClientFriendData.getPendingIncoming();

            if (!pendingIncoming.isEmpty()) {
                yPos += 15; // Skip header

                for (UUID requesterId : pendingIncoming) {
                    // Accept button bounds
                    if (mouseX >= windowX + 150 && mouseX < windowX + 200 &&
                        mouseY >= yPos - 2 && mouseY < yPos + 10) {
                        NetworkHandler.sendToServer(new C2SFriendActionPacket(
                            C2SFriendActionPacket.FriendAction.ACCEPT_REQUEST,
                            requesterId
                        ));
                        return true;
                    }

                    // Decline button bounds
                    if (mouseX >= windowX + 210 && mouseX < windowX + 260 &&
                        mouseY >= yPos - 2 && mouseY < yPos + 10) {
                        NetworkHandler.sendToServer(new C2SFriendActionPacket(
                            C2SFriendActionPacket.FriendAction.DECLINE_REQUEST,
                            requesterId
                        ));
                        return true;
                    }

                    yPos += 15;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private enum Tab {
        FRIENDS,
        PARTY,
        GUILD
    }

    private static class FriendEntry {
        final UUID playerId;
        final String name;
        boolean online;

        FriendEntry(UUID playerId, String name, boolean online) {
            this.playerId = playerId;
            this.name = name;
            this.online = online;
        }
    }
}
