package com.aetheriusmmorpg.common.capability.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

/**
 * Implementation of PlayerRpgData capability.
 * Stores all player RPG state with NBT serialization.
 */
public class PlayerRpgDataImpl implements PlayerRpgData {

    private int level = 1;
    private long experience = 0;

    private boolean hasCreatedCharacter = false;
    private String raceId = "";
    private String classId = "";
    private int hairStyle = 0;
    private int skinTone = 0;

    private double power = 10.0;
    private double spirit = 10.0;
    private double agility = 10.0;
    private double defense = 10.0;
    private double critRate = 5.0;
    private double haste = 0.0;

    private long gold = 0;

    // Cooldowns: skill ID -> expiration tick
    private final Map<ResourceLocation, Long> cooldowns = new HashMap<>();

    // Skill bar: 9 slots (indices 0-8)
    private final ResourceLocation[] skillBar = new ResourceLocation[9];

    // Unlocked Skills
    private final Set<ResourceLocation> unlockedSkills = new HashSet<>();

    // Quests
    private final Map<ResourceLocation, com.aetheriusmmorpg.common.quest.QuestProgress> activeQuests = new HashMap<>();
    private final List<ResourceLocation> completedQuests = new ArrayList<>();

    private boolean dirty = false;

    // Level & Experience
    @Override
    public int getLevel() { return level; }

    @Override
    public void setLevel(int level) {
        this.level = level;
        markDirty();
    }

    @Override
    public long getExperience() { return experience; }

    @Override
    public void setExperience(long xp) {
        this.experience = Math.max(0, xp);
        markDirty();
    }

    @Override
    public long getExperienceForNextLevel() {
        // Formula: 100 * level^1.5
        return (long) (100 * Math.pow(level, 1.5));
    }

    @Override
    public void addExperience(long amount) {
        this.experience += amount;

        // Level up if needed
        while (this.experience >= getExperienceForNextLevel() && level < 100) {
            this.experience -= getExperienceForNextLevel();
            level++;
        }
        markDirty();
    }

    // Character Creation
    @Override
    public boolean hasCreatedCharacter() { return hasCreatedCharacter; }

    @Override
    public void setHasCreatedCharacter(boolean created) {
        this.hasCreatedCharacter = created;
        markDirty();
    }

    // Race & Class
    @Override
    public String getRaceId() { return raceId; }

    @Override
    public void setRaceId(String raceId) {
        this.raceId = raceId;
        markDirty();
    }

    @Override
    public String getClassId() { return classId; }

    @Override
    public void setClassId(String classId) {
        this.classId = classId;
        markDirty();
    }

    // Appearance
    @Override
    public int getHairStyle() { return hairStyle; }

    @Override
    public void setHairStyle(int hairStyle) {
        this.hairStyle = hairStyle;
        markDirty();
    }

    @Override
    public int getSkinTone() { return skinTone; }

    @Override
    public void setSkinTone(int skinTone) {
        this.skinTone = skinTone;
        markDirty();
    }

    // Custom Attributes
    @Override
    public double getPower() { return power; }

    @Override
    public void setPower(double power) {
        this.power = power;
        markDirty();
    }

    @Override
    public double getSpirit() { return spirit; }

    @Override
    public void setSpirit(double spirit) {
        this.spirit = spirit;
        markDirty();
    }

    @Override
    public double getAgility() { return agility; }

    @Override
    public void setAgility(double agility) {
        this.agility = agility;
        markDirty();
    }

    @Override
    public double getDefense() { return defense; }

    @Override
    public void setDefense(double defense) {
        this.defense = defense;
        markDirty();
    }

    @Override
    public double getCritRate() { return critRate; }

    @Override
    public void setCritRate(double critRate) {
        this.critRate = critRate;
        markDirty();
    }

    @Override
    public double getHaste() { return haste; }

    @Override
    public void setHaste(double haste) {
        this.haste = haste;
        markDirty();
    }

    // Currency
    @Override
    public long getGold() { return gold; }

    @Override
    public void setGold(long gold) {
        this.gold = Math.max(0, gold);
        markDirty();
    }

    @Override
    public void addGold(long amount) {
        this.gold += amount;
        markDirty();
    }

    @Override
    public boolean removeGold(long amount) {
        if (this.gold >= amount) {
            this.gold -= amount;
            markDirty();
            return true;
        }
        return false;
    }

    // Cooldowns
    @Override
    public boolean isOnCooldown(ResourceLocation skillId, long currentTick) {
        Long expirationTick = cooldowns.get(skillId);
        return expirationTick != null && currentTick < expirationTick;
    }

    @Override
    public void setCooldown(ResourceLocation skillId, long currentTick, int cooldownTicks) {
        cooldowns.put(skillId, currentTick + cooldownTicks);
        markDirty();
    }

    @Override
    public int getRemainingCooldown(ResourceLocation skillId, long currentTick) {
        Long expirationTick = cooldowns.get(skillId);
        if (expirationTick == null || currentTick >= expirationTick) {
            return 0;
        }
        return (int) (expirationTick - currentTick);
    }

    @Override
    public Map<ResourceLocation, Long> getCooldowns() {
        return cooldowns;
    }

    // Skill Bar
    @Override
    public ResourceLocation getSkillInSlot(int slot) {
        if (slot < 0 || slot >= 9) {
            return null;
        }
        return skillBar[slot];
    }

    @Override
    public void setSkillInSlot(int slot, ResourceLocation skillId) {
        if (slot >= 0 && slot < 9) {
            skillBar[slot] = skillId;
            markDirty();
        }
    }

    @Override
    public ResourceLocation[] getSkillBar() {
        return skillBar.clone();
    }

    @Override
    public void setSkillBar(ResourceLocation[] newSkillBar) {
        if (newSkillBar.length == 9) {
            System.arraycopy(newSkillBar, 0, skillBar, 0, 9);
            markDirty();
        }
    }

    // Unlocked Skills
    @Override
    public Set<ResourceLocation> getUnlockedSkills() {
        return new HashSet<>(unlockedSkills);
    }

    @Override
    public void unlockSkill(ResourceLocation skillId) {
        if (unlockedSkills.add(skillId)) {
            markDirty();
        }
    }

    @Override
    public boolean isSkillUnlocked(ResourceLocation skillId) {
        return unlockedSkills.contains(skillId);
    }

    // Quests
    @Override
    public java.util.List<com.aetheriusmmorpg.common.quest.QuestProgress> getActiveQuests() {
        return new java.util.ArrayList<>(activeQuests.values());
    }

    @Override
    public java.util.List<ResourceLocation> getCompletedQuests() {
        return new java.util.ArrayList<>(completedQuests);
    }

    @Override
    public com.aetheriusmmorpg.common.quest.QuestProgress getQuestProgress(ResourceLocation questId) {
        return activeQuests.get(questId);
    }

    @Override
    public void setQuestProgress(com.aetheriusmmorpg.common.quest.QuestProgress progress) {
        activeQuests.put(progress.getQuestId(), progress);
        markDirty();
    }

    @Override
    public void completeQuest(ResourceLocation questId) {
        activeQuests.remove(questId);
        if (!completedQuests.contains(questId)) {
            completedQuests.add(questId);
        }
        markDirty();
    }

    @Override
    public boolean isQuestCompleted(ResourceLocation questId) {
        return completedQuests.contains(questId);
    }

    // Utility
    @Override
    public void copyFrom(PlayerRpgData source) {
        this.level = source.getLevel();
        this.experience = source.getExperience();
        this.hasCreatedCharacter = source.hasCreatedCharacter();
        this.raceId = source.getRaceId();
        this.classId = source.getClassId();
        this.hairStyle = source.getHairStyle();
        this.skinTone = source.getSkinTone();
        this.power = source.getPower();
        this.spirit = source.getSpirit();
        this.agility = source.getAgility();
        this.defense = source.getDefense();
        this.critRate = source.getCritRate();
        this.haste = source.getHaste();
        this.gold = source.getGold();
        this.cooldowns.clear();
        this.cooldowns.putAll(source.getCooldowns());
        ResourceLocation[] sourceSkillBar = source.getSkillBar();
        System.arraycopy(sourceSkillBar, 0, this.skillBar, 0, 9);
        this.unlockedSkills.clear();
        this.unlockedSkills.addAll(source.getUnlockedSkills());
        this.activeQuests.clear();
        source.getActiveQuests().forEach(q -> this.activeQuests.put(q.getQuestId(), q));
        this.completedQuests.clear();
        this.completedQuests.addAll(source.getCompletedQuests());
        markDirty();
    }

    @Override
    public void markDirty() {
        this.dirty = true;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void clearDirty() {
        this.dirty = false;
    }

    // NBT Serialization
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putInt("Level", level);
        tag.putLong("Experience", experience);

        tag.putBoolean("HasCreatedCharacter", hasCreatedCharacter);
        tag.putString("RaceId", raceId);
        tag.putString("ClassId", classId);
        tag.putInt("HairStyle", hairStyle);
        tag.putInt("SkinTone", skinTone);

        tag.putDouble("Power", power);
        tag.putDouble("Spirit", spirit);
        tag.putDouble("Agility", agility);
        tag.putDouble("Defense", defense);
        tag.putDouble("CritRate", critRate);
        tag.putDouble("Haste", haste);

        tag.putLong("Gold", gold);

        // Serialize cooldowns
        CompoundTag cooldownTag = new CompoundTag();
        for (Map.Entry<ResourceLocation, Long> entry : cooldowns.entrySet()) {
            cooldownTag.putLong(entry.getKey().toString(), entry.getValue());
        }
        tag.put("Cooldowns", cooldownTag);

        // Serialize skill bar
        ListTag skillBarTag = new ListTag();
        for (int i = 0; i < 9; i++) {
            if (skillBar[i] != null) {
                CompoundTag slotTag = new CompoundTag();
                slotTag.putInt("Slot", i);
                slotTag.putString("Skill", skillBar[i].toString());
                skillBarTag.add(slotTag);
            }
        }
        tag.put("SkillBar", skillBarTag);

        // Serialize unlocked skills
        ListTag unlockedSkillsTag = new ListTag();
        for (ResourceLocation skillId : unlockedSkills) {
            unlockedSkillsTag.add(StringTag.valueOf(skillId.toString()));
        }
        tag.put("UnlockedSkills", unlockedSkillsTag);

        // Serialize quests
        ListTag activeQuestsTag = new ListTag();
        for (com.aetheriusmmorpg.common.quest.QuestProgress progress : activeQuests.values()) {
            activeQuestsTag.add(progress.serializeNBT());
        }
        tag.put("ActiveQuests", activeQuestsTag);

        ListTag completedQuestsTag = new ListTag();
        for (ResourceLocation questId : completedQuests) {
            completedQuestsTag.add(StringTag.valueOf(questId.toString()));
        }
        tag.put("CompletedQuests", completedQuestsTag);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        level = tag.getInt("Level");
        experience = tag.getLong("Experience");

        hasCreatedCharacter = tag.getBoolean("HasCreatedCharacter");
        raceId = tag.getString("RaceId");
        classId = tag.getString("ClassId");
        hairStyle = tag.getInt("HairStyle");
        skinTone = tag.getInt("SkinTone");

        power = tag.getDouble("Power");
        spirit = tag.getDouble("Spirit");
        agility = tag.getDouble("Agility");
        defense = tag.getDouble("Defense");
        critRate = tag.getDouble("CritRate");
        haste = tag.getDouble("Haste");

        gold = tag.getLong("Gold");

        // Deserialize cooldowns
        cooldowns.clear();
        if (tag.contains("Cooldowns")) {
            CompoundTag cooldownTag = tag.getCompound("Cooldowns");
            for (String key : cooldownTag.getAllKeys()) {
                ResourceLocation skillId = new ResourceLocation(key);
                long expirationTick = cooldownTag.getLong(key);
                cooldowns.put(skillId, expirationTick);
            }
        }

        // Deserialize skill bar
        for (int i = 0; i < 9; i++) {
            skillBar[i] = null;
        }
        if (tag.contains("SkillBar")) {
            ListTag skillBarTag = tag.getList("SkillBar", 10); // 10 = CompoundTag type
            for (int i = 0; i < skillBarTag.size(); i++) {
                CompoundTag slotTag = skillBarTag.getCompound(i);
                int slot = slotTag.getInt("Slot");
                String skillStr = slotTag.getString("Skill");
                if (slot >= 0 && slot < 9 && !skillStr.isEmpty()) {
                    skillBar[slot] = new ResourceLocation(skillStr);
                }
            }
        }

        // Deserialize unlocked skills
        unlockedSkills.clear();
        if (tag.contains("UnlockedSkills")) {
            ListTag unlockedSkillsTag = tag.getList("UnlockedSkills", 8); // 8 = String type
            for (int i = 0; i < unlockedSkillsTag.size(); i++) {
                String skillStr = unlockedSkillsTag.getString(i);
                if (!skillStr.isEmpty()) {
                    unlockedSkills.add(new ResourceLocation(skillStr));
                }
            }
        }

        // Deserialize quests
        activeQuests.clear();
        if (tag.contains("ActiveQuests")) {
            ListTag activeQuestsTag = tag.getList("ActiveQuests", 10); // 10 = CompoundTag type
            for (int i = 0; i < activeQuestsTag.size(); i++) {
                CompoundTag questTag = activeQuestsTag.getCompound(i);
                com.aetheriusmmorpg.common.quest.QuestProgress progress =
                    com.aetheriusmmorpg.common.quest.QuestProgress.deserializeNBT(questTag);
                activeQuests.put(progress.getQuestId(), progress);
            }
        }

        completedQuests.clear();
        if (tag.contains("CompletedQuests")) {
            ListTag completedQuestsTag = tag.getList("CompletedQuests", 8); // 8 = String type
            for (int i = 0; i < completedQuestsTag.size(); i++) {
                String questIdStr = completedQuestsTag.getString(i);
                completedQuests.add(new ResourceLocation(questIdStr));
            }
        }
    }
}
