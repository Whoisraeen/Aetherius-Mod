package com.aetheriusmmorpg.network.packet;

import com.aetheriusmmorpg.AetheriusMod;
import com.aetheriusmmorpg.common.event.DatapackEvents;
import com.aetheriusmmorpg.common.rpg.skill.Skill;
import com.aetheriusmmorpg.server.skill.SkillExecutor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client-to-Server packet requesting to use a skill.
 * Server validates and executes if valid.
 */
public class C2SUseSkillPacket {
    private final ResourceLocation skillId;
    private final int targetEntityId; // -1 if no entity target
    private final double targetX;
    private final double targetY;
    private final double targetZ;

    public C2SUseSkillPacket(ResourceLocation skillId, int targetEntityId, double targetX, double targetY, double targetZ) {
        this.skillId = skillId;
        this.targetEntityId = targetEntityId;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
    }

    public C2SUseSkillPacket(FriendlyByteBuf buf) {
        this.skillId = buf.readResourceLocation();
        this.targetEntityId = buf.readInt();
        this.targetX = buf.readDouble();
        this.targetY = buf.readDouble();
        this.targetZ = buf.readDouble();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeResourceLocation(skillId);
        buf.writeInt(targetEntityId);
        buf.writeDouble(targetX);
        buf.writeDouble(targetY);
        buf.writeDouble(targetZ);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            // Get skill from manager
            DatapackEvents.SKILL_MANAGER.getSkill(skillId).ifPresentOrElse(
                skill -> {
                    // Resolve target entity if specified
                    var target = targetEntityId >= 0 ? player.level().getEntity(targetEntityId) : null;
                    var targetPos = new net.minecraft.world.phys.Vec3(targetX, targetY, targetZ);

                    // Execute skill
                    boolean success = SkillExecutor.executeSkill(
                        player,
                        skill,
                        target instanceof net.minecraft.world.entity.LivingEntity living ? living : null,
                        targetPos
                    );

                    if (!success) {
                        AetheriusMod.LOGGER.debug("Failed to execute skill {} for player {}",
                            skillId, player.getName().getString());
                    }
                },
                () -> AetheriusMod.LOGGER.warn("Player {} tried to use unknown skill: {}",
                    player.getName().getString(), skillId)
            );
        });
        return true;
    }
}
