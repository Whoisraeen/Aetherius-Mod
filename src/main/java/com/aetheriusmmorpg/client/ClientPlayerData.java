package com.aetheriusmmorpg.client;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Client-side cache of player RPG data.
 * Updated via sync packets from server.
 * Used for UI display only - never use for game logic.
 */
public class ClientPlayerData {
    private static final ClientPlayerData INSTANCE = new ClientPlayerData();

    private static int level = 1;
    private static long experience = 0;
    private static long experienceForNextLevel = 100;

    private static double power = 10.0;
    private static double spirit = 10.0;
    private static double agility = 10.0;
    private static double defense = 10.0;
    private static double critRate = 5.0;
    private static double haste = 0.0;

    private static long gold = 0;

    // Cooldowns: skill ID -> expiration tick
    private static final Map<ResourceLocation, Long> cooldowns = new HashMap<>();
    private static long lastSyncTick = 0;

    // Skill bar: 9 slots
    private static final ResourceLocation[] skillBar = new ResourceLocation[9];

    // Singleton accessor
    public static ClientPlayerData getInstance() {
        return INSTANCE;
    }

    // Getters
    public static int getLevel() { return level; }
    public static long getExperience() { return experience; }
    public static long getExperienceForNextLevel() { return experienceForNextLevel; }
    public static double getPower() { return power; }
    public static double getSpirit() { return spirit; }
    public static double getAgility() { return agility; }
    public static double getDefense() { return defense; }
    public static double getCritRate() { return critRate; }
    public static double getHaste() { return haste; }
    public static long getGold() { return gold; }

    // Setters (called by sync packets)
    public static void setLevel(int level) { ClientPlayerData.level = level; }
    public static void setExperience(long experience) { ClientPlayerData.experience = experience; }
    public static void setExperienceForNextLevel(long experienceForNextLevel) {
        ClientPlayerData.experienceForNextLevel = experienceForNextLevel;
    }
    public static void setPower(double power) { ClientPlayerData.power = power; }
    public static void setSpirit(double spirit) { ClientPlayerData.spirit = spirit; }
    public static void setAgility(double agility) { ClientPlayerData.agility = agility; }
    public static void setDefense(double defense) { ClientPlayerData.defense = defense; }
    public static void setCritRate(double critRate) { ClientPlayerData.critRate = critRate; }
    public static void setHaste(double haste) { ClientPlayerData.haste = haste; }
    public static void setGold(long gold) { ClientPlayerData.gold = gold; }

    // Cooldown management
    public void setCooldowns(Map<ResourceLocation, Long> newCooldowns, long currentTick) {
        cooldowns.clear();
        cooldowns.putAll(newCooldowns);
        lastSyncTick = currentTick;
    }

    public static int getRemainingCooldown(ResourceLocation skillId, long currentTick) {
        Long expirationTick = cooldowns.get(skillId);
        if (expirationTick == null || currentTick >= expirationTick) {
            return 0;
        }
        return (int) (expirationTick - currentTick);
    }

    public static float getCooldownPercent(ResourceLocation skillId, long currentTick) {
        Long expirationTick = cooldowns.get(skillId);
        if (expirationTick == null || currentTick >= expirationTick) {
            return 0.0f;
        }
        // Calculate percentage based on estimated full cooldown
        // This is approximate - ideally we'd track the original cooldown duration
        int remaining = (int) (expirationTick - currentTick);
        return Math.min(1.0f, remaining / 60.0f); // Rough estimate
    }

    // Skill bar management
    public void setSkillBar(ResourceLocation[] newSkillBar) {
        if (newSkillBar.length == 9) {
            System.arraycopy(newSkillBar, 0, skillBar, 0, 9);
        }
    }

    public static ResourceLocation getSkillInSlot(int slot) {
        if (slot >= 0 && slot < 9) {
            return skillBar[slot];
        }
        return null;
    }

    public static ResourceLocation[] getSkillBar() {
        return skillBar.clone();
    }

    // Reset on disconnect
    public static void reset() {
        level = 1;
        experience = 0;
        experienceForNextLevel = 100;
        power = 10.0;
        spirit = 10.0;
        agility = 10.0;
        defense = 10.0;
        critRate = 5.0;
        haste = 0.0;
        gold = 0;
        cooldowns.clear();
        lastSyncTick = 0;
        for (int i = 0; i < 9; i++) {
            skillBar[i] = null;
        }
    }
}
