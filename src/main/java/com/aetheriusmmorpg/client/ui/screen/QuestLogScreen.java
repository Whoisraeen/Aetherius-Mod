package com.aetheriusmmorpg.client.ui.screen;

import com.aetheriusmmorpg.common.quest.Quest;
import com.aetheriusmmorpg.common.quest.QuestManager;
import com.aetheriusmmorpg.common.quest.QuestObjective;
import com.aetheriusmmorpg.common.quest.QuestProgress;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Quest Log UI displaying active and completed quests.
 */
public class QuestLogScreen extends Screen {

    private static final int PANEL_WIDTH = 280;
    private static final int PANEL_HEIGHT = 200;

    private final List<QuestProgress> activeQuests;
    private final List<net.minecraft.resources.ResourceLocation> completedQuests;
    private int selectedQuestIndex = 0;
    private int scrollOffset = 0;

    public QuestLogScreen(List<QuestProgress> activeQuests,
                         List<net.minecraft.resources.ResourceLocation> completedQuests) {
        super(Component.literal("Quest Log"));
        this.activeQuests = new ArrayList<>(activeQuests);
        this.completedQuests = new ArrayList<>(completedQuests);
    }

    @Override
    protected void init() {
        super.init();

        int centerX = (this.width - PANEL_WIDTH) / 2;
        int centerY = (this.height - PANEL_HEIGHT) / 2;

        // Close button
        Button closeButton = Button.builder(
            Component.literal("Close"),
            btn -> this.onClose()
        ).bounds(centerX + PANEL_WIDTH - 60, centerY + PANEL_HEIGHT - 30, 50, 20).build();

        this.addRenderableWidget(closeButton);

        // Track Quest button
        if (!activeQuests.isEmpty()) {
            Button trackButton = Button.builder(
                Component.literal("Track"),
                btn -> {
                    if (selectedQuestIndex < activeQuests.size()) {
                        QuestProgress progress = activeQuests.get(selectedQuestIndex);
                        Quest quest = QuestManager.getQuest(progress.getQuestId());
                        if (quest != null) {
                            com.aetheriusmmorpg.client.ClientQuestData.setTrackedQuest(quest.id());
                            minecraft.player.sendSystemMessage(Component.literal("§eNow tracking: " + quest.name()));
                        }
                    }
                }
            ).bounds(centerX + 10, centerY + PANEL_HEIGHT - 30, 50, 20).build();

            this.addRenderableWidget(trackButton);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        int centerX = (this.width - PANEL_WIDTH) / 2;
        int centerY = (this.height - PANEL_HEIGHT) / 2;

        // Draw background
        guiGraphics.fill(centerX, centerY, centerX + PANEL_WIDTH, centerY + PANEL_HEIGHT, 0xFF2B2B2B);
        guiGraphics.fill(centerX + 2, centerY + 2, centerX + PANEL_WIDTH - 2, centerY + PANEL_HEIGHT - 2, 0xFF1A1A1A);

        // Title
        guiGraphics.drawString(this.font, this.title,
            centerX + (PANEL_WIDTH - this.font.width(this.title)) / 2,
            centerY + 10, 0xFFD700, false);

        // Section tabs (visual only for now)
        guiGraphics.drawString(this.font, "§eActive §7(" + activeQuests.size() + ")",
            centerX + 10, centerY + 28, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "§7Completed (" + completedQuests.size() + ")",
            centerX + 110, centerY + 28, 0x808080, false);

        // Divider
        guiGraphics.fill(centerX + 5, centerY + 40, centerX + PANEL_WIDTH - 5, centerY + 41, 0xFF404040);

        // Quest list
        if (activeQuests.isEmpty()) {
            guiGraphics.drawString(this.font, "No active quests",
                centerX + 10, centerY + 50, 0x808080, false);
        } else {
            int yOffset = 50;
            int maxDisplay = 5; // Max quests to display at once

            for (int i = scrollOffset; i < Math.min(activeQuests.size(), scrollOffset + maxDisplay); i++) {
                QuestProgress progress = activeQuests.get(i);
                Quest quest = QuestManager.getQuest(progress.getQuestId());

                if (quest != null) {
                    // Quest name
                    boolean isSelected = (i == selectedQuestIndex);
                    int nameColor = isSelected ? 0xFFD700 : 0xFFFFFF;
                    String questName = quest.name();
                    if (questName.length() > 30) {
                        questName = questName.substring(0, 27) + "...";
                    }

                    guiGraphics.drawString(this.font, questName,
                        centerX + 10, centerY + yOffset, nameColor, false);

                    // Quest progress summary
                    int completedObj = 0;
                    for (int j = 0; j < quest.objectives().size(); j++) {
                        QuestObjective obj = quest.objectives().get(j);
                        int prog = progress.getObjectiveProgress(j);
                        if (obj.isComplete(prog)) {
                            completedObj++;
                        }
                    }

                    String progressText = "(" + completedObj + "/" + quest.objectives().size() + ")";
                    guiGraphics.drawString(this.font, progressText,
                        centerX + PANEL_WIDTH - 50, centerY + yOffset, 0xAAAAAA, false);

                    yOffset += 15;
                }
            }

            // Show selected quest details
            if (selectedQuestIndex < activeQuests.size()) {
                QuestProgress progress = activeQuests.get(selectedQuestIndex);
                Quest quest = QuestManager.getQuest(progress.getQuestId());

                if (quest != null) {
                    int detailY = centerY + 130;

                    // Quest description
                    String desc = quest.description();
                    if (desc.length() > 40) {
                        desc = desc.substring(0, 37) + "...";
                    }
                    guiGraphics.drawString(this.font, "§7" + desc,
                        centerX + 10, detailY, 0xAAAAAA, false);

                    // Objectives
                    detailY += 12;
                    for (int i = 0; i < quest.objectives().size() && i < 2; i++) {
                        QuestObjective obj = quest.objectives().get(i);
                        int prog = progress.getObjectiveProgress(i);
                        String objText = obj.getProgressText(prog);
                        if (objText.length() > 35) {
                            objText = objText.substring(0, 32) + "...";
                        }

                        int color = obj.isComplete(prog) ? 0x00FF00 : 0xFFFFFF;
                        String checkbox = obj.isComplete(prog) ? "☑" : "☐";
                        guiGraphics.drawString(this.font, checkbox + " " + objText,
                            centerX + 10, detailY, color, false);
                        detailY += 10;
                    }
                }
            }
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle quest selection from list
        int centerX = (this.width - PANEL_WIDTH) / 2;
        int centerY = (this.height - PANEL_HEIGHT) / 2;

        if (mouseX >= centerX + 10 && mouseX <= centerX + PANEL_WIDTH - 10) {
            int yOffset = 50;
            int maxDisplay = 5;

            for (int i = scrollOffset; i < Math.min(activeQuests.size(), scrollOffset + maxDisplay); i++) {
                int questY = centerY + yOffset;
                if (mouseY >= questY && mouseY < questY + 15) {
                    selectedQuestIndex = i;
                    return true;
                }
                yOffset += 15;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // Scroll through quest list
        if (delta > 0 && scrollOffset > 0) {
            scrollOffset--;
        } else if (delta < 0 && scrollOffset < activeQuests.size() - 5) {
            scrollOffset++;
        }
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
