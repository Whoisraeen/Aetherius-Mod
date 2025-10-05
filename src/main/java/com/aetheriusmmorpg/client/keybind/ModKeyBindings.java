package com.aetheriusmmorpg.client.keybind;

import com.aetheriusmmorpg.AetheriusMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

/**
 * Keybindings for Aetherius.
 * Following PWI-style defaults where applicable.
 */
public class ModKeyBindings {
    public static final String CATEGORY = "key.categories." + AetheriusMod.MOD_ID;

    // Core UI Keybinds (PWI-style)
    public static final KeyMapping CHARACTER_SHEET = new KeyMapping(
        "key." + AetheriusMod.MOD_ID + ".character_sheet",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_C,  // PWI default: C
        CATEGORY
    );

    public static final KeyMapping SKILL_TREE = new KeyMapping(
        "key." + AetheriusMod.MOD_ID + ".skill_tree",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_R,  // PWI default: R
        CATEGORY
    );

    public static final KeyMapping QUEST_LOG = new KeyMapping(
        "key." + AetheriusMod.MOD_ID + ".quest_log",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_Q,  // PWI default: Q
        CATEGORY
    );

    public static final KeyMapping SOCIAL = new KeyMapping(
        "key." + AetheriusMod.MOD_ID + ".social",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_F,  // PWI default: F for friends/social
        CATEGORY
    );

    public static final KeyMapping CHAT = new KeyMapping(
        "key." + AetheriusMod.MOD_ID + ".chat",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_ENTER,  // PWI default: Enter for chat
        CATEGORY
    );

    // Action bar 1 (skills 1-9)
    public static final KeyMapping[] ACTION_BAR_A = new KeyMapping[9];

    static {
        for (int i = 0; i < 9; i++) {
            ACTION_BAR_A[i] = new KeyMapping(
                "key." + AetheriusMod.MOD_ID + ".action_bar_a." + (i + 1),
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_1 + i,  // Keys 1-9
                CATEGORY
            );
        }
    }
}
