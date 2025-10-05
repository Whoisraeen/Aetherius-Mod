package com.aetheriusmmorpg.client.ui.screen;

import com.aetheriusmmorpg.client.ClientGuildData;
import com.aetheriusmmorpg.common.menu.GuildMenu;
import com.aetheriusmmorpg.network.NetworkHandler;
import com.aetheriusmmorpg.network.packet.guild.C2SGuildActionPacket;
import com.aetheriusmmorpg.network.packet.guild.S2CGuildDataPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Map;
import java.util.UUID;

/**
 * PWI-style guild management screen.
 * Shows guild info, members, ranks, bank, and management options.
 */
public class GuildScreen extends AbstractContainerScreen<GuildMenu> {

    private static final int WINDOW_WIDTH = 400;
    private static final int WINDOW_HEIGHT = 360;

    private Tab currentTab = Tab.INFO;

    // UI Elements
    private Button infoTabButton;
    private Button membersTabButton;
    private Button ranksTabButton;
    private Button bankTabButton;

    // Guild Creation UI
    private EditBox guildNameBox;
    private EditBox guildTagBox;
    private Button createGuildButton;

    private int windowX;
    private int windowY;

    public GuildScreen(GuildMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = WINDOW_WIDTH;
        this.imageHeight = WINDOW_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();

        this.windowX = this.leftPos;
        this.windowY = this.topPos;

        // Tab buttons
        int tabWidth = 90;
        int tabHeight = 24;
        int tabY = windowY + 10;

        infoTabButton = Button.builder(Component.literal("Info"),
            btn -> switchTab(Tab.INFO))
            .bounds(windowX + 10, tabY, tabWidth, tabHeight)
            .build();

        membersTabButton = Button.builder(Component.literal("Members"),
            btn -> switchTab(Tab.MEMBERS))
            .bounds(windowX + 10 + tabWidth + 5, tabY, tabWidth, tabHeight)
            .build();

        ranksTabButton = Button.builder(Component.literal("Ranks"),
            btn -> switchTab(Tab.RANKS))
            .bounds(windowX + 10 + (tabWidth + 5) * 2, tabY, tabWidth, tabHeight)
            .build();

        bankTabButton = Button.builder(Component.literal("Bank"),
            btn -> switchTab(Tab.BANK))
            .bounds(windowX + 10 + (tabWidth + 5) * 3, tabY, tabWidth, tabHeight)
            .build();

        addRenderableWidget(infoTabButton);
        addRenderableWidget(membersTabButton);
        addRenderableWidget(ranksTabButton);
        addRenderableWidget(bankTabButton);

        // Guild creation UI (only show if not in guild)
        if (!ClientGuildData.hasGuild()) {
            initGuildCreationUI();
        }
    }

    private void initGuildCreationUI() {
        int centerX = windowX + WINDOW_WIDTH / 2;
        int startY = windowY + 80;

        // Guild name input
        guildNameBox = new EditBox(font, centerX - 100, startY, 200, 20, Component.literal("Guild Name"));
        guildNameBox.setMaxLength(20);
        guildNameBox.setHint(Component.literal("Enter guild name..."));
        addRenderableWidget(guildNameBox);

        // Guild tag input
        guildTagBox = new EditBox(font, centerX - 100, startY + 30, 200, 20, Component.literal("Guild Tag"));
        guildTagBox.setMaxLength(5);
        guildTagBox.setHint(Component.literal("Tag (2-5 letters)"));
        addRenderableWidget(guildTagBox);

        // Create button
        createGuildButton = Button.builder(Component.literal("Create Guild"),
            btn -> {
                String name = guildNameBox.getValue().trim();
                String tag = guildTagBox.getValue().trim();
                if (!name.isEmpty() && !tag.isEmpty()) {
                    NetworkHandler.sendToServer(C2SGuildActionPacket.create(name, tag));
                    this.onClose();
                }
            })
            .bounds(centerX - 75, startY + 60, 150, 20)
            .build();
        addRenderableWidget(createGuildButton);
    }

    private void switchTab(Tab tab) {
        this.currentTab = tab;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Window background
        guiGraphics.fill(windowX, windowY, windowX + WINDOW_WIDTH, windowY + WINDOW_HEIGHT, 0xE0000000);

        // Window border
        guiGraphics.fill(windowX, windowY, windowX + WINDOW_WIDTH, windowY + 1, 0xFF6B4DB8); // Top
        guiGraphics.fill(windowX, windowY + WINDOW_HEIGHT - 1, windowX + WINDOW_WIDTH, windowY + WINDOW_HEIGHT, 0xFF6B4DB8); // Bottom
        guiGraphics.fill(windowX, windowY, windowX + 1, windowY + WINDOW_HEIGHT, 0xFF6B4DB8); // Left
        guiGraphics.fill(windowX + WINDOW_WIDTH - 1, windowY, windowX + WINDOW_WIDTH, windowY + WINDOW_HEIGHT, 0xFF6B4DB8); // Right

        // Title
        Component titleText = Component.literal("§5§lGUILD");
        int titleX = windowX + (WINDOW_WIDTH - font.width(titleText)) / 2;
        guiGraphics.drawString(font, titleText, titleX, windowY + 50, 0xFFFFFFFF, false);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Render based on current tab
        int contentY = 110;

        if (!ClientGuildData.hasGuild()) {
            // Show guild creation UI
            Component header = Component.literal("§6Create a Guild");
            guiGraphics.drawString(font, header, WINDOW_WIDTH / 2 - font.width(header) / 2, 65, 0xFFFFFFFF, false);

            Component hint = Component.literal("§7Guild creation costs 100 gold");
            guiGraphics.drawString(font, hint, WINDOW_WIDTH / 2 - font.width(hint) / 2, 150, 0xFFAAAAAA, false);
        } else {
            // Show guild info based on tab
            switch (currentTab) {
                case INFO:
                    renderInfoTab(guiGraphics, contentY);
                    break;
                case MEMBERS:
                    renderMembersTab(guiGraphics, contentY);
                    break;
                case RANKS:
                    renderRanksTab(guiGraphics, contentY);
                    break;
                case BANK:
                    renderBankTab(guiGraphics, contentY);
                    break;
            }
        }
    }

    private void renderInfoTab(GuiGraphics guiGraphics, int startY) {
        int yPos = startY;

        // Guild name and tag
        guiGraphics.drawString(font, Component.literal("§6[" + ClientGuildData.getGuildTag() + "] " + ClientGuildData.getGuildName()), 10, yPos, 0xFFFFFFFF, false);
        yPos += 20;

        // Level and experience
        guiGraphics.drawString(font, Component.literal("§bLevel: §f" + ClientGuildData.getLevel()), 10, yPos, 0xFFFFFFFF, false);
        yPos += 15;

        long exp = ClientGuildData.getExperience();
        long expNext = ClientGuildData.getExpForNextLevel();
        guiGraphics.drawString(font, Component.literal("§bExperience: §f" + exp + " / " + expNext), 10, yPos, 0xFFFFFFFF, false);
        yPos += 15;

        // Experience bar
        int barWidth = 200;
        int barHeight = 10;
        guiGraphics.fill(10, yPos, 10 + barWidth, yPos + barHeight, 0xFF333333);
        int filledWidth = expNext > 0 ? (int) ((double) exp / expNext * barWidth) : 0;
        guiGraphics.fill(10, yPos, 10 + filledWidth, yPos + barHeight, 0xFF6B4DB8);
        yPos += 20;

        // Members
        guiGraphics.drawString(font, Component.literal("§bMembers: §f" + ClientGuildData.getMembers().size() + " / " + ClientGuildData.getMaxMembers()), 10, yPos, 0xFFFFFFFF, false);
        yPos += 20;

        // Announcement
        guiGraphics.drawString(font, Component.literal("§6Announcement:"), 10, yPos, 0xFFFFFFFF, false);
        yPos += 15;
        guiGraphics.drawString(font, Component.literal("§7" + ClientGuildData.getAnnouncement()), 10, yPos, 0xFFFFFFFF, false);
    }

    private void renderMembersTab(GuiGraphics guiGraphics, int startY) {
        int yPos = startY;

        guiGraphics.drawString(font, Component.literal("§6Guild Members:"), 10, yPos, 0xFFFFFFFF, false);
        yPos += 20;

        Map<UUID, S2CGuildDataPacket.MemberData> members = ClientGuildData.getMembers();
        for (Map.Entry<UUID, S2CGuildDataPacket.MemberData> entry : members.entrySet()) {
            S2CGuildDataPacket.MemberData member = entry.getValue();
            boolean isLeader = entry.getKey().equals(ClientGuildData.getLeaderId());
            String prefix = isLeader ? "§6[Leader] " : "§7[" + member.rank() + "] ";
            guiGraphics.drawString(font, Component.literal(prefix + "§f" + member.name()), 10, yPos, 0xFFFFFFFF, false);
            yPos += 15;
        }
    }

    private void renderRanksTab(GuiGraphics guiGraphics, int startY) {
        int yPos = startY;
        guiGraphics.drawString(font, Component.literal("§6Guild Ranks:"), 10, yPos, 0xFFFFFFFF, false);
        yPos += 20;

        // Display PWI-style ranks
        String[] ranks = {"Leader", "Marshal", "Executioner", "Commander", "Officer", "Member"};
        for (String rank : ranks) {
            guiGraphics.drawString(font, Component.literal("§7• §f" + rank), 10, yPos, 0xFFFFFFFF, false);
            yPos += 15;
        }
    }

    private void renderBankTab(GuiGraphics guiGraphics, int startY) {
        int yPos = startY;
        guiGraphics.drawString(font, Component.literal("§6Guild Bank:"), 10, yPos, 0xFFFFFFFF, false);
        yPos += 20;

        guiGraphics.drawString(font, Component.literal("§eGold: §f" + ClientGuildData.getGuildGold()), 10, yPos, 0xFFFFFFFF, false);
        yPos += 20;

        guiGraphics.drawString(font, Component.literal("§7Bank features coming soon..."), 10, yPos, 0xFFAAAAAA, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private enum Tab {
        INFO,
        MEMBERS,
        RANKS,
        BANK
    }
}

