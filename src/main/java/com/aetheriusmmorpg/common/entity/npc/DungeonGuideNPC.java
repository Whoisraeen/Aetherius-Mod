package com.aetheriusmmorpg.common.entity.npc;

import com.aetheriusmmorpg.AetheriusMod;
import com.aetheriusmmorpg.common.capability.player.PlayerRpgData;
import com.aetheriusmmorpg.common.dungeon.Dungeon;
import com.aetheriusmmorpg.common.dungeon.DungeonInstance;
import com.aetheriusmmorpg.common.dungeon.DungeonManager;
import com.aetheriusmmorpg.common.party.Party;
import com.aetheriusmmorpg.common.party.PartyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * NPC that provides access to dungeons.
 * Checks party requirements and teleports players to dungeon instances.
 */
public class DungeonGuideNPC extends AetheriusNPC {

    private ResourceLocation assignedDungeon;

    public DungeonGuideNPC(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setNPCType(NPCType.GENERIC);
    }

    /**
     * Set which dungeon this NPC provides access to.
     */
    public void setAssignedDungeon(ResourceLocation dungeonId) {
        this.assignedDungeon = dungeonId;
    }

    public ResourceLocation getAssignedDungeon() {
        return assignedDungeon;
    }

    @Override
    protected void openGenericDialog(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        if (assignedDungeon == null) {
            player.sendSystemMessage(Component.literal("§cThis dungeon guide has no assigned dungeon."));
            return;
        }

        Dungeon dungeon = DungeonManager.getDungeon(assignedDungeon);
        if (dungeon == null) {
            player.sendSystemMessage(Component.literal("§cDungeon not found!"));
            return;
        }

        // Display dungeon info
        player.sendSystemMessage(Component.literal("§6=== " + dungeon.name() + " ==="));
        player.sendSystemMessage(Component.literal("§7" + dungeon.description()));
        player.sendSystemMessage(Component.literal("§7Difficulty: §f" + dungeon.difficulty().name()));
        player.sendSystemMessage(Component.literal("§7Required Level: §f" + dungeon.requiredLevel() + " - " + dungeon.maxLevel()));
        player.sendSystemMessage(Component.literal("§7Party Size: §f" + dungeon.minPartySize() + " - " + dungeon.maxPartySize()));
        player.sendSystemMessage(Component.literal("§7Time Limit: §f" + dungeon.timeLimitMinutes() + " minutes"));
        player.sendSystemMessage(Component.literal("§7Cooldown: §f" + dungeon.cooldownHours() + " hours"));

        // Check if player can enter
        PartyManager partyManager = PartyManager.get(serverPlayer.getServer());
        Party party = partyManager.getPlayerParty(serverPlayer.getUUID());

        if (party == null) {
            player.sendSystemMessage(Component.literal("§cYou need to be in a party to enter a dungeon!"));
            player.sendSystemMessage(Component.literal("§7Use §a/party create§7 to create a party."));
            return;
        }

        if (!party.isLeader(serverPlayer.getUUID())) {
            player.sendSystemMessage(Component.literal("§cOnly the party leader can enter a dungeon!"));
            return;
        }

        // Get party members
        List<ServerPlayer> partyMembers = partyManager.getOnlineMembers(party, serverPlayer.getServer());

        // Check party size
        if (!dungeon.isPartySizeValid(partyMembers.size())) {
            player.sendSystemMessage(Component.literal(
                "§cInvalid party size! This dungeon requires " + dungeon.minPartySize() + "-" + dungeon.maxPartySize() + " players."
            ));
            return;
        }

        // Check all members' levels
        for (ServerPlayer member : partyMembers) {
            member.getCapability(PlayerRpgData.CAPABILITY).ifPresent(data -> {
                if (!dungeon.isLevelInRange(data.getLevel())) {
                    player.sendSystemMessage(Component.literal(
                        "§c" + member.getName().getString() + " is not within the level range for this dungeon!"
                    ));
                }
            });
        }

        // Check cooldowns
        DungeonManager dungeonManager = DungeonManager.get(serverPlayer.getServer());
        for (ServerPlayer member : partyMembers) {
            if (dungeonManager.isOnCooldown(member.getUUID(), dungeon.id())) {
                int cooldownSeconds = dungeonManager.getRemainingCooldown(member.getUUID(), dungeon.id());
                int hours = cooldownSeconds / 3600;
                int minutes = (cooldownSeconds % 3600) / 60;

                player.sendSystemMessage(Component.literal(
                    "§c" + member.getName().getString() + " is on cooldown! Time remaining: " + hours + "h " + minutes + "m"
                ));
                return;
            }
        }

        // Check if party already has an active instance
        if (dungeonManager.getPartyInstance(party.getPartyId()) != null) {
            player.sendSystemMessage(Component.literal("§cYour party already has an active dungeon instance!"));
            return;
        }

        // All checks passed - create instance and enter
        player.sendSystemMessage(Component.literal("§aCreating dungeon instance..."));
        enterDungeon(serverPlayer, party, dungeon, partyMembers);
    }

    /**
     * Create dungeon instance and teleport party.
     */
    private void enterDungeon(ServerPlayer leader, Party party, Dungeon dungeon, List<ServerPlayer> partyMembers) {
        DungeonManager dungeonManager = DungeonManager.get(leader.getServer());

        // Create instance
        DungeonInstance instance = dungeonManager.createInstance(dungeon, party, partyMembers);
        if (instance == null) {
            leader.sendSystemMessage(Component.literal("§cFailed to create dungeon instance!"));
            return;
        }

        // Teleport all party members to dungeon entrance
        // For now, use a simple offset from the current world
        // In a full implementation, this would create a separate dimension
        BlockPos entrancePos = leader.blockPosition().offset(0, 100, 0);

        for (ServerPlayer member : partyMembers) {
            teleportToDungeon(member, entrancePos);
            member.sendSystemMessage(Component.literal("§aEntering " + dungeon.name() + "..."));
            member.sendSystemMessage(Component.literal("§7Time limit: " + dungeon.timeLimitMinutes() + " minutes"));
        }

        AetheriusMod.LOGGER.info("Party {} entered dungeon {} (Instance: {})",
            party.getPartyId(), dungeon.id(), instance.getInstanceId());
    }

    /**
     * Teleport player to dungeon.
     */
    private void teleportToDungeon(ServerPlayer player, BlockPos pos) {
        player.teleportTo(
            player.serverLevel(),
            pos.getX() + 0.5,
            pos.getY(),
            pos.getZ() + 0.5,
            player.getYRot(),
            player.getXRot()
        );
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (assignedDungeon != null) {
            tag.putString("AssignedDungeon", assignedDungeon.toString());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("AssignedDungeon")) {
            assignedDungeon = new ResourceLocation(tag.getString("AssignedDungeon"));
        }
    }
}
