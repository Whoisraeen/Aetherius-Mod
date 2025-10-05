package com.aetheriusmmorpg.client.ui.screen;

import com.aetheriusmmorpg.client.ClientPlayerData;
import com.aetheriusmmorpg.client.ClientQuestData;
import com.aetheriusmmorpg.common.menu.QuestDialogMenu;
import com.aetheriusmmorpg.common.quest.Quest;
import com.aetheriusmmorpg.common.quest.QuestManager;
import com.aetheriusmmorpg.common.quest.QuestObjective;
import com.aetheriusmmorpg.network.NetworkHandler;
import com.aetheriusmmorpg.network.packet.quest.C2SQuestActionPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

/**
 * PWI-style quest dialog screen for NPC interactions.
 * Shows available quests, quest details, objectives, and rewards.
 */
public class QuestDialogScreen extends AbstractContainerScreen<QuestDialogMenu> {

    private static final int WINDOW_WIDTH = 420;
    private static final int WINDOW_HEIGHT = 380;

    private List<Quest> availableQuests = new ArrayList<>();
    private Quest selectedQuest = null;
    private int scrollOffset = 0;

    private Button acceptButton;
    private Button completeButton;
    private Button closeButton;

    public QuestDialogScreen(QuestDialogMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = WINDOW_WIDTH;
        this.imageHeight = WINDOW_HEIGHT;

        // Load available quests for this NPC
        String npcId = menu.getNpcId();
        if (npcId != null && !npcId.isEmpty()) {
            availableQuests = QuestManager.getQuestsByGiver(npcId).stream()
                .filter(quest -> quest.canAccept(ClientPlayerData.getLevel(), ClientPlayerData.getCompletedQuests()))
                .toList();
        }
    }

    @Override
    protected void init() {
        super.init();

        int centerX = leftPos + WINDOW_WIDTH / 2;
        int bottomY = topPos + WINDOW_HEIGHT - 35;

        // Accept button
        acceptButton = Button.builder(Component.literal("Accept Quest"),
            btn -> {
                if (selectedQuest != null) {
                    NetworkHandler.sendToServer(C2SQuestActionPacket.accept(selectedQuest.id()));
                    this.onClose();
                }
            })
            .bounds(leftPos + 20, bottomY, 120, 20)
            .build();
        acceptButton.visible = false;
        addRenderableWidget(acceptButton);

        // Complete button
        completeButton = Button.builder(Component.literal("Complete Quest"),
            btn -> {
                if (selectedQuest != null) {
                    NetworkHandler.sendToServer(C2SQuestActionPacket.complete(selectedQuest.id()));
                    this.onClose();
                }
            })
            .bounds(leftPos + 20, bottomY, 120, 20)
            .build();
        completeButton.visible = false;
        addRenderableWidget(completeButton);

        // Close button
        closeButton = Button.builder(Component.literal("Close"),
            btn -> this.onClose())
            .bounds(centerX + 40, bottomY, 80, 20)
            .build();
        addRenderableWidget(closeButton);
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
        guiGraphics.fill(leftPos, topPos, leftPos + WINDOW_WIDTH, topPos + WINDOW_HEIGHT, 0xE0000000);

        // Window border
        guiGraphics.fill(leftPos, topPos, leftPos + WINDOW_WIDTH, topPos + 1, 0xFF6B4DB8); // Top
        guiGraphics.fill(leftPos, topPos + WINDOW_HEIGHT - 1, leftPos + WINDOW_WIDTH, topPos + WINDOW_HEIGHT, 0xFF6B4DB8); // Bottom
        guiGraphics.fill(leftPos, topPos, leftPos + 1, topPos + WINDOW_HEIGHT, 0xFF6B4DB8); // Left
        guiGraphics.fill(leftPos + WINDOW_WIDTH - 1, topPos, leftPos + WINDOW_WIDTH, topPos + WINDOW_HEIGHT, 0xFF6B4DB8); // Right

        // Divider between quest list and details
        guiGraphics.fill(leftPos + 140, topPos + 40, leftPos + 141, topPos + WINDOW_HEIGHT - 45, 0xFF6B4DB8);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Title
        Component titleText = Component.literal("§6§lQuests");
        guiGraphics.drawString(font, titleText, (WINDOW_WIDTH - font.width(titleText)) / 2, 8, 0xFFFFFFFF, false);

        // Quest list section
        guiGraphics.drawString(font, Component.literal("§eAvailable:"), 10, 25, 0xFFFFFFFF, false);
        renderQuestList(guiGraphics, mouseX, mouseY);

        // Quest details section
        if (selectedQuest != null) {
            renderQuestDetails(guiGraphics);
        } else {
            guiGraphics.drawString(font, Component.literal("§7Select a quest to view details"), 150, 50, 0xFFAAAAAA, false);
        }
    }

    private void renderQuestList(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int yPos = 40;
        int maxVisible = 14;
        
        for (int i = scrollOffset; i < Math.min(availableQuests.size(), scrollOffset + maxVisible); i++) {
            Quest quest = availableQuests.get(i);
            boolean isActive = ClientQuestData.hasActiveQuest(quest.id());
            boolean isCompleted = ClientQuestData.hasCompletedQuest(quest.id());
            boolean isHovered = mouseX >= leftPos + 10 && mouseX < leftPos + 135 &&
                               mouseY >= topPos + yPos && mouseY < topPos + yPos + 12;

            // Quest type indicator
            String prefix = switch (quest.type()) {
                case MAIN -> "§6[!] ";
                case SIDE -> "§b[?] ";
                case DAILY -> "§a[D] ";
                case GUILD -> "§5[G] ";
                default -> "§7[?] ";
            };

            // Quest name
            String questName = quest.name();
            if (questName.length() > 15) {
                questName = questName.substring(0, 15) + "...";
            }

            String color = isCompleted ? "§7" : isActive ? "§a" : "§f";
            String displayName = prefix + color + questName;

            if (isHovered) {
                guiGraphics.fill(10, yPos - 1, 135, yPos + 11, 0x40FFFFFF);
            }

            if (selectedQuest == quest) {
                guiGraphics.fill(10, yPos - 1, 135, yPos + 11, 0x406B4DB8);
            }

            guiGraphics.drawString(font, Component.literal(displayName), 10, yPos, 0xFFFFFFFF, false);
            yPos += 12;
        }
    }

    private void renderQuestDetails(GuiGraphics guiGraphics) {
        int xStart = 150;
        int yPos = 40;

        // Quest name and type
        guiGraphics.drawString(font, Component.literal("§6" + selectedQuest.name()), xStart, yPos, 0xFFFFFFFF, false);
        yPos += 12;

        String typeStr = "§7[" + selectedQuest.type().name() + "]";
        guiGraphics.drawString(font, Component.literal(typeStr), xStart, yPos, 0xFFFFFFFF, false);
        yPos += 15;

        // Level requirement
        guiGraphics.drawString(font, Component.literal("§7Required Level: §f" + selectedQuest.requiredLevel()), xStart, yPos, 0xFFFFFFFF, false);
        yPos += 15;

        // Description
        guiGraphics.drawString(font, Component.literal("§eDescription:"), xStart, yPos, 0xFFFFFFFF, false);
        yPos += 12;
        
        String desc = selectedQuest.description();
        List<String> wrappedDesc = wrapText(desc, 250);
        for (String line : wrappedDesc) {
            guiGraphics.drawString(font, Component.literal("§7" + line), xStart, yPos, 0xFFFFFFFF, false);
            yPos += 10;
        }
        yPos += 5;

        // Objectives
        guiGraphics.drawString(font, Component.literal("§eObjectives:"), xStart, yPos, 0xFFFFFFFF, false);
        yPos += 12;

        for (int i = 0; i < selectedQuest.objectives().size(); i++) {
            QuestObjective obj = selectedQuest.objectives().get(i);
            String objText = "§7• " + obj.description();
            guiGraphics.drawString(font, Component.literal(objText), xStart, yPos, 0xFFFFFFFF, false);
            yPos += 10;
        }
        yPos += 5;

        // Rewards
        if (selectedQuest.rewards() != null) {
            guiGraphics.drawString(font, Component.literal("§eRewards:"), xStart, yPos, 0xFFFFFFFF, false);
            yPos += 12;

            if (selectedQuest.rewards().experience() > 0) {
                guiGraphics.drawString(font, Component.literal("§7• " + selectedQuest.rewards().experience() + " XP"), xStart, yPos, 0xFFFFFFFF, false);
                yPos += 10;
            }

            if (selectedQuest.rewards().gold() > 0) {
                guiGraphics.drawString(font, Component.literal("§7• " + selectedQuest.rewards().gold() + " Gold"), xStart, yPos, 0xFFFFFFFF, false);
                yPos += 10;
            }

            if (!selectedQuest.rewards().items().isEmpty()) {
                guiGraphics.drawString(font, Component.literal("§7• " + selectedQuest.rewards().items().size() + " Item(s)"), xStart, yPos, 0xFFFFFFFF, false);
                yPos += 10;
            }
        }

        // Update button visibility
        boolean hasQuest = ClientQuestData.hasActiveQuest(selectedQuest.id());
        acceptButton.visible = !hasQuest && !ClientQuestData.hasCompletedQuest(selectedQuest.id());
        completeButton.visible = hasQuest;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle quest list clicks
        if (mouseX >= leftPos + 10 && mouseX < leftPos + 135) {
            int relativeY = (int)(mouseY - topPos - 40);
            if (relativeY >= 0) {
                int clickedIndex = relativeY / 12 + scrollOffset;
                if (clickedIndex < availableQuests.size()) {
                    selectedQuest = availableQuests.get(clickedIndex);
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseX >= leftPos + 10 && mouseX < leftPos + 135) {
            scrollOffset = Math.max(0, Math.min(scrollOffset - (int)delta, Math.max(0, availableQuests.size() - 14)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (font.width(currentLine + word) > maxWidth) {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
            }
            if (currentLine.length() > 0) {
                currentLine.append(" ");
            }
            currentLine.append(word);
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}


