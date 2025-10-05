package com.aetheriusmmorpg.common.capability.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Capability interface storing all RPG data for a player.
 * Server-authoritative: all modifications must happen server-side.
 * Client receives synced copies for display only.
 */
public interface PlayerRpgData extends INBTSerializable<CompoundTag> {

    Capability<PlayerRpgData> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    // Level & Experience
    int getLevel();
    void setLevel(int level);
    long getExperience();
    void setExperience(long xp);
    long getExperienceForNextLevel();
    void addExperience(long amount);

    // Character Creation
    boolean hasCreatedCharacter();
    void setHasCreatedCharacter(boolean created);

    // Race & Class
    String getRaceId();
    void setRaceId(String raceId);
    String getClassId();
    void setClassId(String classId);

    // Custom Attributes (base values, modifiers applied through attribute system)
    double getPower();
    void setPower(double power);
    double getSpirit();
    void setSpirit(double spirit);
    double getAgility();
    void setAgility(double agility);
    double getDefense();
    void setDefense(double defense);
    double getCritRate();
    void setCritRate(double critRate);
    double getHaste();
    void setHaste(double haste);

    // Currency
    long getGold();
    void setGold(long gold);
    void addGold(long amount);
    boolean removeGold(long amount);

    // Cooldowns
    /**
     * Check if a skill is currently on cooldown.
     * @param skillId The skill to check
     * @param currentTick The current server tick
     * @return true if on cooldown, false if ready
     */
    boolean isOnCooldown(net.minecraft.resources.ResourceLocation skillId, long currentTick);

    /**
     * Set a skill on cooldown.
     * @param skillId The skill to put on cooldown
     * @param currentTick The current server tick
     * @param cooldownTicks The cooldown duration in ticks
     */
    void setCooldown(net.minecraft.resources.ResourceLocation skillId, long currentTick, int cooldownTicks);

    /**
     * Get remaining cooldown ticks for a skill.
     * @param skillId The skill to check
     * @param currentTick The current server tick
     * @return Remaining ticks, or 0 if not on cooldown
     */
    int getRemainingCooldown(net.minecraft.resources.ResourceLocation skillId, long currentTick);

    /**
     * Get all active cooldowns.
     * @return Map of skill ID to expiration tick
     */
    java.util.Map<net.minecraft.resources.ResourceLocation, Long> getCooldowns();

    // Skill Bar
    /**
     * Get skill assigned to a specific slot (0-8).
     * @param slot The slot index
     * @return The skill ID, or null if empty
     */
    net.minecraft.resources.ResourceLocation getSkillInSlot(int slot);

    /**
     * Set skill in a specific slot (0-8).
     * @param slot The slot index
     * @param skillId The skill ID, or null to clear
     */
    void setSkillInSlot(int slot, net.minecraft.resources.ResourceLocation skillId);

    /**
     * Get all skill bar assignments.
     * @return Array of 9 skill IDs (may contain nulls)
     */
    net.minecraft.resources.ResourceLocation[] getSkillBar();

    /**
     * Set the entire skill bar.
     * @param skillBar Array of 9 skill IDs
     */
    void setSkillBar(net.minecraft.resources.ResourceLocation[] skillBar);

    // Quests
    /**
     * Get all active quests.
     */
    java.util.List<com.aetheriusmmorpg.common.quest.QuestProgress> getActiveQuests();

    /**
     * Get all completed quest IDs.
     */
    java.util.List<net.minecraft.resources.ResourceLocation> getCompletedQuests();

    /**
     * Get progress for a specific quest.
     */
    com.aetheriusmmorpg.common.quest.QuestProgress getQuestProgress(net.minecraft.resources.ResourceLocation questId);

    /**
     * Add or update quest progress.
     */
    void setQuestProgress(com.aetheriusmmorpg.common.quest.QuestProgress progress);

    /**
     * Mark quest as completed.
     */
    void completeQuest(net.minecraft.resources.ResourceLocation questId);

    /**
     * Check if quest is completed.
     */
    boolean isQuestCompleted(net.minecraft.resources.ResourceLocation questId);

    // Utility
    void copyFrom(PlayerRpgData source);
    void markDirty();
    boolean isDirty();
    void clearDirty();
}
