package com.aetheriusmmorpg.network.packet;

import com.aetheriusmmorpg.client.ClientPlayerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server-to-Client packet for syncing player RPG stats.
 * Sent on login, respawn, and whenever stats change.
 */
public class S2CStatSyncPacket {
    private final int level;
    private final long experience;
    private final long experienceForNextLevel;
    private final double power;
    private final double spirit;
    private final double agility;
    private final double defense;
    private final double critRate;
    private final double haste;
    private final long gold;

    public S2CStatSyncPacket(int level, long experience, long experienceForNextLevel,
                             double power, double spirit, double agility, double defense,
                             double critRate, double haste, long gold) {
        this.level = level;
        this.experience = experience;
        this.experienceForNextLevel = experienceForNextLevel;
        this.power = power;
        this.spirit = spirit;
        this.agility = agility;
        this.defense = defense;
        this.critRate = critRate;
        this.haste = haste;
        this.gold = gold;
    }

    public S2CStatSyncPacket(FriendlyByteBuf buf) {
        this.level = buf.readInt();
        this.experience = buf.readLong();
        this.experienceForNextLevel = buf.readLong();
        this.power = buf.readDouble();
        this.spirit = buf.readDouble();
        this.agility = buf.readDouble();
        this.defense = buf.readDouble();
        this.critRate = buf.readDouble();
        this.haste = buf.readDouble();
        this.gold = buf.readLong();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(level);
        buf.writeLong(experience);
        buf.writeLong(experienceForNextLevel);
        buf.writeDouble(power);
        buf.writeDouble(spirit);
        buf.writeDouble(agility);
        buf.writeDouble(defense);
        buf.writeDouble(critRate);
        buf.writeDouble(haste);
        buf.writeLong(gold);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            // Client-side: update cached data
            ClientPlayerData.setLevel(level);
            ClientPlayerData.setExperience(experience);
            ClientPlayerData.setExperienceForNextLevel(experienceForNextLevel);
            ClientPlayerData.setPower(power);
            ClientPlayerData.setSpirit(spirit);
            ClientPlayerData.setAgility(agility);
            ClientPlayerData.setDefense(defense);
            ClientPlayerData.setCritRate(critRate);
            ClientPlayerData.setHaste(haste);
            ClientPlayerData.setGold(gold);
        });
        return true;
    }
}
