package com.aetheriusmmorpg.network.packet;

import com.aetheriusmmorpg.AetheriusMod;
import com.aetheriusmmorpg.common.capability.player.PlayerRpgData;
import com.aetheriusmmorpg.common.event.DatapackEvents;
import com.aetheriusmmorpg.common.rpg.clazz.PlayerClass;
import com.aetheriusmmorpg.common.rpg.race.Race;
import com.aetheriusmmorpg.network.NetworkHandler;
import com.aetheriusmmorpg.network.packet.S2CSkillBarPacket;
import com.aetheriusmmorpg.network.packet.S2CStatSyncPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client->Server packet to create a new character with selected race, class, and appearance.
 */
public record C2SCreateCharacterPacket(
    String raceId,
    String classId,
    int hairStyle,
    int skinTone
) {

    public C2SCreateCharacterPacket(FriendlyByteBuf buf) {
        this(
            buf.readUtf(),
            buf.readUtf(),
            buf.readInt(),
            buf.readInt()
        );
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(raceId);
        buf.writeUtf(classId);
        buf.writeInt(hairStyle);
        buf.writeInt(skinTone);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                player.getCapability(PlayerRpgData.CAPABILITY).ifPresent(data -> {
                    // Validate the character hasn't already been created
                    if (data.hasCreatedCharacter()) {
                        AetheriusMod.LOGGER.warn("Player {} attempted to create character but already has one", player.getName().getString());
                        return;
                    }

                    // Validate race and class
                    ResourceLocation raceResLoc = new ResourceLocation(raceId);
                    ResourceLocation classResLoc = new ResourceLocation(classId);

                    Race race = DatapackEvents.RACE_MANAGER.getRace(raceResLoc).orElse(null);
                    PlayerClass playerClass = DatapackEvents.CLASS_MANAGER.getPlayerClass(classResLoc).orElse(null);

                    if (race == null) {
                        AetheriusMod.LOGGER.error("Invalid race ID: {}", raceId);
                        return;
                    }

                    if (playerClass == null) {
                        AetheriusMod.LOGGER.error("Invalid class ID: {}", classId);
                        return;
                    }

                    // Validate race can use this class
                    if (!race.canUseClass(classResLoc)) {
                        AetheriusMod.LOGGER.error("Race {} cannot use class {}", raceId, classId);
                        return;
                    }

                    // Set character data
                    data.setRaceId(raceId);
                    data.setClassId(classId);
                    data.setHasCreatedCharacter(true);

                    // Apply base attributes from race
                    data.setPower(race.getBaseAttribute("power"));
                    data.setSpirit(race.getBaseAttribute("spirit"));
                    data.setAgility(race.getBaseAttribute("agility"));
                    data.setDefense(race.getBaseAttribute("defense"));
                    data.setCritRate(race.getBaseAttribute("crit_rate"));
                    data.setHaste(race.getBaseAttribute("haste"));

                    // Save appearance data
                    data.setHairStyle(this.hairStyle);
                    data.setSkinTone(this.skinTone);

                    AetheriusMod.LOGGER.info("Player {} created character: Race={}, Class={}, Hair={}, Skin={}",
                        player.getName().getString(), raceId, classId, hairStyle, skinTone);

                    // Sync player stats to client
                    NetworkHandler.sendToPlayer(new S2CStatSyncPacket(
                        data.getLevel(),
                        data.getExperience(),
                        data.getExperienceForNextLevel(),
                        data.getPower(),
                        data.getSpirit(),
                        data.getAgility(),
                        data.getDefense(),
                        data.getCritRate(),
                        data.getHaste(),
                        data.getGold()
                    ), player);

                    // Sync skill bar
                    NetworkHandler.sendToPlayer(new S2CSkillBarPacket(data.getSkillBar()), player);

                    // Teleport player to race-specific starting city
                    teleportToStartingCity(player, race);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }

    /**
     * Teleport the player to their race's starting city.
     */
    private static void teleportToStartingCity(ServerPlayer player, com.aetheriusmmorpg.common.rpg.race.Race race) {
        ResourceLocation startingCityId = race.startingCity();
        
        com.aetheriusmmorpg.common.world.StartingCity city = DatapackEvents.STARTING_CITY_MANAGER
            .getCity(startingCityId)
            .orElse(null);

        if (city != null) {
            // Get the target dimension
            net.minecraft.server.level.ServerLevel targetLevel = player.getServer().getLevel(city.dimension());
            
            if (targetLevel != null) {
                // Teleport to starting city
                net.minecraft.core.BlockPos pos = city.position();
                player.teleportTo(
                    targetLevel,
                    pos.getX() + 0.5,
                    pos.getY(),
                    pos.getZ() + 0.5,
                    city.yaw(),
                    city.pitch()
                );
                
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§6Welcome to " + city.name() + "!"));
                
                AetheriusMod.LOGGER.info("Player {} teleported to starting city {} at {}",
                    player.getName().getString(), city.name(), pos);
            } else {
                AetheriusMod.LOGGER.error("Target dimension {} not found for starting city {}",
                    city.dimension(), startingCityId);
                teleportToWorldSpawn(player);
            }
        } else {
            AetheriusMod.LOGGER.warn("Starting city {} not found, using world spawn",
                startingCityId);
            teleportToWorldSpawn(player);
        }
    }

    /**
     * Fallback teleport to world spawn.
     */
    private static void teleportToWorldSpawn(ServerPlayer player) {
        net.minecraft.core.BlockPos spawnPos = player.serverLevel().getSharedSpawnPos();
        player.teleportTo(
            player.serverLevel(),
            spawnPos.getX() + 0.5,
            spawnPos.getY(),
            spawnPos.getZ() + 0.5,
            player.getYRot(),
            player.getXRot()
        );
    }
}
