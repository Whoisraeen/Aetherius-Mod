package com.aetheriusmmorpg.client.ui.screen;

import com.aetheriusmmorpg.client.ClientPlayerData;
import com.aetheriusmmorpg.common.menu.SkillTrainerMenu;
import com.aetheriusmmorpg.common.rpg.skill.Skill;
import com.aetheriusmmorpg.common.rpg.skill.SkillManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

/**
 * PWI-style skill trainer screen for learning new skills.
 */
public class SkillTrainerScreen extends AbstractContainerScreen<SkillTrainerMenu> {

    private static final int WINDOW_WIDTH = 380;
    private static final int WINDOW_HEIGHT = 360;

    private List<Skill> availableSkills = new ArrayList<>();
    private Skill selectedSkill = null;
    private int scrollOffset = 0;

    private Button learnButton;

    public SkillTrainerScreen(SkillTrainerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = WINDOW_WIDTH;
        this.imageHeight = WINDOW_HEIGHT;

        // Load available skills for player's class (would filter by class in production)
        availableSkills = new ArrayList<>();
        // TODO: Load from SkillManager based on player class
        // For now, empty list as skills need to be properly loaded from datapacks
    }

    @Override
    protected void init() {
        super.init();

        // Learn button
        learnButton = Button.builder(Component.literal("Learn Skill"),
            btn -> {
                if (selectedSkill != null) {
                    // TODO: Send learn skill packet
                    minecraft.player.sendSystemMessage(Component.literal("§aLearned: " + selectedSkill.name()));
                    this.onClose();
                }
            })
            .bounds(leftPos + 20, topPos + imageHeight - 35, 120, 20)
            .build();
        learnButton.visible = false;
        addRenderableWidget(learnButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.fill(leftPos, topPos, leftPos + WINDOW_WIDTH, topPos + WINDOW_HEIGHT, 0xE0000000);
        guiGraphics.fill(leftPos, topPos, leftPos + WINDOW_WIDTH, topPos + 1, 0xFF6B4DB8);
        guiGraphics.fill(leftPos, topPos + WINDOW_HEIGHT - 1, leftPos + WINDOW_WIDTH, topPos + WINDOW_HEIGHT, 0xFF6B4DB8);
        guiGraphics.fill(leftPos, topPos, leftPos + 1, topPos + WINDOW_HEIGHT, 0xFF6B4DB8);
        guiGraphics.fill(leftPos + WINDOW_WIDTH - 1, topPos, leftPos + WINDOW_WIDTH, topPos + WINDOW_HEIGHT, 0xFF6B4DB8);
        
        // Divider
        guiGraphics.fill(leftPos + 130, topPos + 40, leftPos + 131, topPos + WINDOW_HEIGHT - 45, 0xFF6B4DB8);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Component titleText = Component.literal("§b§lSkill Trainer");
        guiGraphics.drawString(font, titleText, (WINDOW_WIDTH - font.width(titleText)) / 2, 8, 0xFFFFFFFF, false);

        // Skill list
        guiGraphics.drawString(font, Component.literal("§eAvailable Skills:"), 10, 25, 0xFFFFFFFF, false);
        renderSkillList(guiGraphics, mouseX, mouseY);

        // Skill details
        if (selectedSkill != null) {
            renderSkillDetails(guiGraphics);
            learnButton.visible = true;
        } else {
            guiGraphics.drawString(font, Component.literal("§7Select a skill"), 140, 50, 0xFFAAAAAA, false);
            learnButton.visible = false;
        }
    }

    private void renderSkillList(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int yPos = 40;
        for (int i = scrollOffset; i < Math.min(availableSkills.size(), scrollOffset + 14); i++) {
            Skill skill = availableSkills.get(i);
            boolean isHovered = mouseX >= leftPos + 10 && mouseX < leftPos + 125 &&
                               mouseY >= topPos + yPos && mouseY < topPos + yPos + 12;

            if (isHovered) {
                guiGraphics.fill(10, yPos - 1, 125, yPos + 11, 0x40FFFFFF);
            }

            if (selectedSkill == skill) {
                guiGraphics.fill(10, yPos - 1, 125, yPos + 11, 0x406B4DB8);
            }

            String skillName = skill.name();
            if (skillName.length() > 14) {
                skillName = skillName.substring(0, 14) + "...";
            }

            guiGraphics.drawString(font, Component.literal("§f" + skillName), 10, yPos, 0xFFFFFFFF, false);
            yPos += 12;
        }
    }

    private void renderSkillDetails(GuiGraphics guiGraphics) {
        int xStart = 140;
        int yPos = 40;

        guiGraphics.drawString(font, Component.literal("§b" + selectedSkill.name()), xStart, yPos, 0xFFFFFFFF, false);
        yPos += 15;

        guiGraphics.drawString(font, Component.literal("§7Level Required: §f1"), xStart, yPos, 0xFFFFFFFF, false); // TODO: Get from skill
        yPos += 12;

        guiGraphics.drawString(font, Component.literal("§7Mana Cost: §f100"), xStart, yPos, 0xFFFFFFFF, false); // TODO: Get from skill
        yPos += 12;

        guiGraphics.drawString(font, Component.literal("§7Cooldown: §f" + (selectedSkill.cooldown() / 20) + "s"), xStart, yPos, 0xFFFFFFFF, false);
        yPos += 15;

        guiGraphics.drawString(font, Component.literal("§eDescription:"), xStart, yPos, 0xFFFFFFFF, false);
        yPos += 12;

        String desc = selectedSkill.description();
        List<String> wrappedDesc = wrapText(desc, 220);
        for (String line : wrappedDesc) {
            guiGraphics.drawString(font, Component.literal("§7" + line), xStart, yPos, 0xFFFFFFFF, false);
            yPos += 10;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= leftPos + 10 && mouseX < leftPos + 125) {
            int relativeY = (int)(mouseY - topPos - 40);
            if (relativeY >= 0) {
                int clickedIndex = relativeY / 12 + scrollOffset;
                if (clickedIndex < availableSkills.size()) {
                    selectedSkill = availableSkills.get(clickedIndex);
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseX >= leftPos + 10 && mouseX < leftPos + 125) {
            scrollOffset = Math.max(0, Math.min(scrollOffset - (int)delta, Math.max(0, availableSkills.size() - 14)));
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

