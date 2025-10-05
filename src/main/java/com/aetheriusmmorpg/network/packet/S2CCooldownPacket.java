package com.aetheriusmmorpg.network.packet;

import com.aetheriusmmorpg.client.ClientPlayerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Server-to-Client packet syncing skill cooldowns.
 * Sent when cooldowns change or player logs in.
 */
public class S2CCooldownPacket {
    private final Map<ResourceLocation, Long> cooldowns;
    private final long currentTick;

    public S2CCooldownPacket(Map<ResourceLocation, Long> cooldowns, long currentTick) {
        this.cooldowns = cooldowns;
        this.currentTick = currentTick;
    }

    public S2CCooldownPacket(FriendlyByteBuf buf) {
        this.currentTick = buf.readLong();
        int size = buf.readInt();
        this.cooldowns = new HashMap<>();
        for (int i = 0; i < size; i++) {
            ResourceLocation skillId = buf.readResourceLocation();
            long expirationTick = buf.readLong();
            cooldowns.put(skillId, expirationTick);
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeLong(currentTick);
        buf.writeInt(cooldowns.size());
        for (Map.Entry<ResourceLocation, Long> entry : cooldowns.entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            buf.writeLong(entry.getValue());
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            // Update client-side cooldown cache
            ClientPlayerData.getInstance().setCooldowns(cooldowns, currentTick);
        });
        return true;
    }
}
