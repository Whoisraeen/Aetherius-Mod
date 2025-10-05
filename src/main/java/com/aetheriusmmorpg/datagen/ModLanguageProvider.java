package com.aetheriusmmorpg.datagen;

import com.aetheriusmmorpg.AetheriusMod;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

/**
 * Generates en_us.json language file for all translatable text.
 * Covers items, blocks, GUI labels, tooltips, and system messages.
 */
public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(PackOutput output) {
        super(output, AetheriusMod.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        // Mod info
        add("aetherius.name", "Aetherius MMO");

        // Items
        add("item.aetherius.basic_sword", "Basic Sword");
        add("item.aetherius.health_potion", "Health Potion");

        // Custom attributes
        add("attribute.aetherius.power", "Power");
        add("attribute.aetherius.spirit", "Spirit");
        add("attribute.aetherius.agility", "Agility");
        add("attribute.aetherius.defense", "Defense");
        add("attribute.aetherius.crit_rate", "Critical Rate");
        add("attribute.aetherius.haste", "Haste");

        // GUI labels
        add("gui.aetherius.character_sheet", "Character Sheet");
        add("gui.aetherius.skill_tree", "Skill Tree");
        add("gui.aetherius.quest_log", "Quest Log");
        add("gui.aetherius.level", "Level: %s");
        add("gui.aetherius.experience", "Experience: %s / %s");

        // Keybindings
        add("key.categories.aetherius", "Aetherius MMO");
        add("key.aetherius.character_sheet", "Open Character Sheet");
        add("key.aetherius.skill_tree", "Open Skill Tree");
        add("key.aetherius.quest_log", "Open Quest Log");
        for (int i = 1; i <= 9; i++) {
            add("key.aetherius.action_bar_a." + i, "Action Bar A - Skill " + i);
        }

        // System messages
        add("aetherius.system.welcome", "Welcome to Aetherius!");
        add("aetherius.system.level_up", "Level Up! You are now level %s");

        // More translations will be added as content is implemented
    }
}
