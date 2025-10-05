package com.aetheriusmmorpg.common.quest;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks progress for a single quest.
 */
public class QuestProgress {
    private final ResourceLocation questId;
    private final Map<Integer, Integer> objectiveProgress; // Objective index -> current progress
    private QuestStatus status;
    private long acceptedTime;
    private long completedTime;

    public QuestProgress(ResourceLocation questId) {
        this.questId = questId;
        this.objectiveProgress = new HashMap<>();
        this.status = QuestStatus.AVAILABLE;
        this.acceptedTime = 0;
        this.completedTime = 0;
    }

    public enum QuestStatus {
        AVAILABLE,      // Can be accepted
        ACTIVE,         // Currently in progress
        COMPLETED,      // Successfully completed
        FAILED,         // Failed (time limit, etc.)
        TURNED_IN       // Rewards claimed
    }

    // Progress tracking
    public void incrementObjective(int objectiveIndex, int amount) {
        int current = objectiveProgress.getOrDefault(objectiveIndex, 0);
        objectiveProgress.put(objectiveIndex, current + amount);
    }

    public int getObjectiveProgress(int objectiveIndex) {
        return objectiveProgress.getOrDefault(objectiveIndex, 0);
    }

    public void setObjectiveProgress(int objectiveIndex, int progress) {
        objectiveProgress.put(objectiveIndex, progress);
    }

    // Status management
    public QuestStatus getStatus() {
        return status;
    }

    public void setStatus(QuestStatus status) {
        this.status = status;
        if (status == QuestStatus.ACTIVE && acceptedTime == 0) {
            acceptedTime = System.currentTimeMillis();
        } else if (status == QuestStatus.COMPLETED && completedTime == 0) {
            completedTime = System.currentTimeMillis();
        }
    }

    public ResourceLocation getQuestId() {
        return questId;
    }

    public long getAcceptedTime() {
        return acceptedTime;
    }

    public long getCompletedTime() {
        return completedTime;
    }

    // Check if all objectives are complete
    public boolean areAllObjectivesComplete(Quest quest) {
        if (quest == null) return false;

        for (int i = 0; i < quest.objectives().size(); i++) {
            QuestObjective objective = quest.objectives().get(i);
            int progress = getObjectiveProgress(i);
            if (!objective.isComplete(progress)) {
                return false;
            }
        }
        return true;
    }

    // Check if quest has failed (time limit exceeded)
    public boolean hasTimeLimitExpired(Quest quest) {
        if (quest.timeLimit() <= 0 || status != QuestStatus.ACTIVE) {
            return false;
        }

        long elapsed = (System.currentTimeMillis() - acceptedTime) / 1000; // Convert to seconds
        return elapsed > quest.timeLimit();
    }

    // NBT Serialization
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("QuestId", questId.toString());
        tag.putString("Status", status.name());
        tag.putLong("AcceptedTime", acceptedTime);
        tag.putLong("CompletedTime", completedTime);

        ListTag objectivesTag = new ListTag();
        objectiveProgress.forEach((index, progress) -> {
            CompoundTag objTag = new CompoundTag();
            objTag.putInt("Index", index);
            objTag.putInt("Progress", progress);
            objectivesTag.add(objTag);
        });
        tag.put("Objectives", objectivesTag);

        return tag;
    }

    public static QuestProgress deserializeNBT(CompoundTag tag) {
        ResourceLocation questId = new ResourceLocation(tag.getString("QuestId"));
        QuestProgress progress = new QuestProgress(questId);

        progress.status = QuestStatus.valueOf(tag.getString("Status"));
        progress.acceptedTime = tag.getLong("AcceptedTime");
        progress.completedTime = tag.getLong("CompletedTime");

        ListTag objectivesTag = tag.getList("Objectives", Tag.TAG_COMPOUND);
        for (int i = 0; i < objectivesTag.size(); i++) {
            CompoundTag objTag = objectivesTag.getCompound(i);
            int index = objTag.getInt("Index");
            int objProgress = objTag.getInt("Progress");
            progress.objectiveProgress.put(index, objProgress);
        }

        return progress;
    }
}
