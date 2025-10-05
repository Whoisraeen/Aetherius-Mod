package com.aetheriusmmorpg.network.packet;

import com.aetheriusmmorpg.client.ClientPlayerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server-to-Client packet syncing skill bar configuration.
 * Sent when player logs in or skill bar changes.
 */
public class S2CSkillBarPacket {
    private final ResourceLocation[] skillBar;

    public S2CSkillBarPacket(ResourceLocation[] skillBar) {
        this.skillBar = skillBar;
    }

    public S2CSkillBarPacket(FriendlyByteBuf buf) {
        this.skillBar = new ResourceLocation[9];
        for (int i = 0; i < 9; i++) {
            boolean hasSkill = buf.readBoolean();
            if (hasSkill) {
                skillBar[i] = buf.readResourceLocation();
            } else {
                skillBar[i] = null;
            }
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        for (int i = 0; i < 9; i++) {
            if (skillBar[i] != null) {
                buf.writeBoolean(true);
                buf.writeResourceLocation(skillBar[i]);
            } else {
                buf.writeBoolean(false);
            }
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            // Update client-side skill bar cache
            ClientPlayerData.getInstance().setSkillBar(skillBar);
        });
        return true;
    }
}
