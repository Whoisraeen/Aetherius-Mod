package com.aetheriusmmorpg.client.ui.screen;

import com.aetheriusmmorpg.AetheriusMod;
import com.aetheriusmmorpg.common.menu.CharacterCreationMenu;
import com.aetheriusmmorpg.common.rpg.clazz.ClassManager;
import com.aetheriusmmorpg.common.rpg.clazz.PlayerClass;
import com.aetheriusmmorpg.common.rpg.race.Race;
import com.aetheriusmmorpg.common.rpg.race.RaceManager;
import com.aetheriusmmorpg.network.NetworkHandler;
import com.aetheriusmmorpg.network.packet.C2SCreateCharacterPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

/**
 * Character creation screen for selecting race, class, and appearance.
 * Displayed when a player first joins or hasn't created a character yet.
 */
public class CharacterCreationScreen extends AbstractContainerScreen<CharacterCreationMenu> {

    private static final int PANEL_WIDTH = 300;
    private static final int PANEL_HEIGHT = 220;

    private Race selectedRace = null;
    private PlayerClass selectedClass = null;
    private int selectedHairStyle = 0;
    private int selectedSkinTone = 0;

    private final List<Race> availableRaces = new ArrayList<>();
    private final List<PlayerClass> availableClasses = new ArrayList<>();

    private Button createButton;
    private final List<Button> raceButtons = new ArrayList<>();
    private final List<Button> classButtons = new ArrayList<>();
    private final List<Button> appearanceButtons = new ArrayList<>();

    public CharacterCreationScreen(CharacterCreationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, Component.literal("Character Creation"));
        this.imageWidth = PANEL_WIDTH;
        this.imageHeight = PANEL_HEIGHT;

        // Load available races and classes
        availableRaces.addAll(RaceManager.getAllRaces());
        if (!availableRaces.isEmpty()) {
            selectedRace = availableRaces.get(0);
            updateAvailableClasses();
        }
    }

    @Override
    protected void init() {
        super.init();

        int centerX = (this.width - PANEL_WIDTH) / 2;
        int centerY = (this.height - PANEL_HEIGHT) / 2;

        // Race selection buttons (left side)
        int raceX = centerX + 10;
        int raceY = centerY + 30;
        for (int i = 0; i < Math.min(availableRaces.size(), 6); i++) {
            final int index = i;
            Race race = availableRaces.get(i);
            Button raceButton = Button.builder(
                Component.literal(race.name()),
                btn -> selectRace(index)
            ).bounds(raceX, raceY + (i * 22), 80, 20).build();

            this.addRenderableWidget(raceButton);
            raceButtons.add(raceButton);
        }

        // Class selection buttons (middle)
        updateClassButtons();

        // Hair style buttons (right side)
        int hairX = centerX + 200;
        int hairY = centerY + 30;

        Button prevHairButton = Button.builder(
            Component.literal("<"),
            btn -> {
                selectedHairStyle = (selectedHairStyle - 1 + 5) % 5;
            }
        ).bounds(hairX, hairY, 20, 20).build();

        Button nextHairButton = Button.builder(
            Component.literal(">"),
            btn -> {
                selectedHairStyle = (selectedHairStyle + 1) % 5;
            }
        ).bounds(hairX + 60, hairY, 20, 20).build();

        this.addRenderableWidget(prevHairButton);
        this.addRenderableWidget(nextHairButton);
        appearanceButtons.add(prevHairButton);
        appearanceButtons.add(nextHairButton);

        // Skin tone buttons
        int skinY = centerY + 60;
        Button prevSkinButton = Button.builder(
            Component.literal("<"),
            btn -> {
                selectedSkinTone = (selectedSkinTone - 1 + 5) % 5;
            }
        ).bounds(hairX, skinY, 20, 20).build();

        Button nextSkinButton = Button.builder(
            Component.literal(">"),
            btn -> {
                selectedSkinTone = (selectedSkinTone + 1) % 5;
            }
        ).bounds(hairX + 60, skinY, 20, 20).build();

        this.addRenderableWidget(prevSkinButton);
        this.addRenderableWidget(nextSkinButton);
        appearanceButtons.add(prevSkinButton);
        appearanceButtons.add(nextSkinButton);

        // Create character button
        createButton = Button.builder(
            Component.literal("Create Character"),
            btn -> createCharacter()
        ).bounds(centerX + (PANEL_WIDTH - 120) / 2, centerY + PANEL_HEIGHT - 35, 120, 20).build();

        createButton.active = selectedRace != null && selectedClass != null;
        this.addRenderableWidget(createButton);
    }

    private void selectRace(int index) {
        if (index >= 0 && index < availableRaces.size()) {
            selectedRace = availableRaces.get(index);
            updateAvailableClasses();
            updateClassButtons();
            createButton.active = selectedRace != null && selectedClass != null;
        }
    }

    private void selectClass(int index) {
        if (index >= 0 && index < availableClasses.size()) {
            selectedClass = availableClasses.get(index);
            createButton.active = selectedRace != null && selectedClass != null;
        }
    }

    private void updateAvailableClasses() {
        availableClasses.clear();
        if (selectedRace != null) {
            for (PlayerClass playerClass : ClassManager.getAllClasses()) {
                if (selectedRace.canUseClass(playerClass.id())) {
                    availableClasses.add(playerClass);
                }
            }
            if (!availableClasses.isEmpty()) {
                selectedClass = availableClasses.get(0);
            } else {
                selectedClass = null;
            }
        }
    }

    private void updateClassButtons() {
        // Remove old class buttons
        classButtons.forEach(this::removeWidget);
        classButtons.clear();

        int centerX = (this.width - PANEL_WIDTH) / 2;
        int centerY = (this.height - PANEL_HEIGHT) / 2;
        int classX = centerX + 100;
        int classY = centerY + 30;

        for (int i = 0; i < Math.min(availableClasses.size(), 6); i++) {
            final int index = i;
            PlayerClass playerClass = availableClasses.get(i);
            Button classButton = Button.builder(
                Component.literal(playerClass.name()),
                btn -> selectClass(index)
            ).bounds(classX, classY + (i * 22), 90, 20).build();

            this.addRenderableWidget(classButton);
            classButtons.add(classButton);
        }
    }

    private void createCharacter() {
        if (selectedRace != null && selectedClass != null) {
            // Send character creation packet to server
            NetworkHandler.sendToServer(new C2SCreateCharacterPacket(
                selectedRace.id().toString(),
                selectedClass.id().toString(),
                selectedHairStyle,
                selectedSkinTone
            ));

            this.onClose();
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (this.width - PANEL_WIDTH) / 2;
        int y = (this.height - PANEL_HEIGHT) / 2;

        // Draw background
        guiGraphics.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, 0xFF2B2B2B);
        guiGraphics.fill(x + 2, y + 2, x + PANEL_WIDTH - 2, y + PANEL_HEIGHT - 2, 0xFF1A1A1A);

        // Draw section dividers
        guiGraphics.fill(x + 95, y + 20, x + 97, y + PANEL_HEIGHT - 40, 0xFF404040);
        guiGraphics.fill(x + 195, y + 20, x + 197, y + PANEL_HEIGHT - 40, 0xFF404040);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Title
        guiGraphics.drawString(this.font, this.title, (PANEL_WIDTH - this.font.width(this.title)) / 2, 8, 0xFFFFFF, false);

        // Section titles
        guiGraphics.drawString(this.font, "Race", 30, 20, 0xFFD700, false);
        guiGraphics.drawString(this.font, "Class", 120, 20, 0xFFD700, false);
        guiGraphics.drawString(this.font, "Appearance", 210, 20, 0xFFD700, false);

        // Hair style label
        guiGraphics.drawString(this.font, "Hair: " + (selectedHairStyle + 1), 208, 35, 0xFFFFFF, false);

        // Skin tone label
        guiGraphics.drawString(this.font, "Skin: " + (selectedSkinTone + 1), 208, 65, 0xFFFFFF, false);

        // Selected race description
        if (selectedRace != null) {
            int descY = 155;
            String desc = selectedRace.description();
            if (desc.length() > 45) {
                desc = desc.substring(0, 42) + "...";
            }
            guiGraphics.drawString(this.font, desc, 10, descY, 0xAAAAAA, false);
        }

        // Selected class description
        if (selectedClass != null) {
            int descY = 170;
            String desc = selectedClass.description() + " (" + selectedClass.role() + ")";
            if (desc.length() > 45) {
                desc = desc.substring(0, 42) + "...";
            }
            guiGraphics.drawString(this.font, desc, 10, descY, 0xAAAAAA, false);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Prevent closing with ESC - players must create a character
        if (keyCode == 256) { // ESC key
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
