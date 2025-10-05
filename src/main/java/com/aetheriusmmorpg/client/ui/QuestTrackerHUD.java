package com.aetheriusmmorpg.client.ui;

import com.aetheriusmmorpg.client.ClientQuestData;
import com.aetheriusmmorpg.common.quest.Quest;
import com.aetheriusmmorpg.common.quest.QuestManager;
import com.aetheriusmmorpg.common.quest.QuestObjective;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * PWI-style quest tracker HUD displaying active quest objectives.
 */
public class QuestTrackerHUD {

    private static ResourceLocation trackedQuestId = null;

    public static void setTrackedQuest(ResourceLocation questId) {
        trackedQuestId = questId;
    }

    public static void render(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        if (trackedQuestId == null) return;

        Quest quest = QuestManager.getQuest(trackedQuestId);
        if (quest == null) {
            trackedQuestId = null;
            return;
        }

        int[] progress = ClientQuestData.getQuestProgress(trackedQuestId);
        if (progress == null) {
            trackedQuestId = null;
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        int xPos = 10;
        int yPos = screenHeight / 2 - 100;

        // Quest tracker background
        int trackerWidth = 220;
        int trackerHeight = 15 + (quest.objectives().size() * 12) + 10;
        
        guiGraphics.fill(xPos, yPos, xPos + trackerWidth, yPos + trackerHeight, 0xC0000000);
        guiGraphics.fill(xPos, yPos, xPos + trackerWidth, yPos + 1, 0xFF6B4DB8); // Top border

        // Quest name
        Component questName = Component.literal("§6" + quest.name());
        guiGraphics.drawString(font, questName, xPos + 5, yPos + 5, 0xFFFFFFFF, false);

        // Objectives
        int objYPos = yPos + 18;
        for (int i = 0; i < quest.objectives().size() && i < progress.length; i++) {
            QuestObjective obj = quest.objectives().get(i);
            int current = i < progress.length ? progress[i] : 0;
            int required = 1; // TODO: Get from objective when method exists

            boolean isComplete = current >= required;
            String color = isComplete ? "§a" : "§f";
            String checkbox = isComplete ? "§a[✓]" : "§7[ ]";

            Component objText = Component.literal(checkbox + " " + color + obj.description() + " §7(" + current + "/" + required + ")");
            guiGraphics.drawString(font, objText, xPos + 5, objYPos, 0xFFFFFFFF, false);
            objYPos += 12;
        }
    }
}

