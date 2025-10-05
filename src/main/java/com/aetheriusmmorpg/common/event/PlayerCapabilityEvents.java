package com.aetheriusmmorpg.common.event;

import com.aetheriusmmorpg.AetheriusMod;
import com.aetheriusmmorpg.common.capability.player.PlayerRpgDataProvider;
import com.aetheriusmmorpg.network.NetworkHandler;
import com.aetheriusmmorpg.network.packet.S2COpenIntroVideoPacket;
import com.aetheriusmmorpg.network.packet.S2CStatSyncPacket;
import com.aetheriusmmorpg.network.packet.S2CSkillBarPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Event handlers for player capability attachment, syncing, and cloning.
 */
@Mod.EventBusSubscriber(modid = AetheriusMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerCapabilityEvents {

    /**
     * Attach PlayerRpgData capability to all player entities.
     */
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(PlayerRpgDataProvider.PLAYER_RPG_DATA).isPresent()) {
                event.addCapability(
                    new ResourceLocation(AetheriusMod.MOD_ID, "player_rpg_data"),
                    new PlayerRpgDataProvider()
                );
            }
        }
    }

    /**
     * Sync player data when they log in.
     * Opens character creation screen if player hasn't created a character yet.
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCapability(PlayerRpgDataProvider.PLAYER_RPG_DATA).ifPresent(data -> {
                // Check if player needs to create a character
                if (!data.hasCreatedCharacter()) {
                    AetheriusMod.LOGGER.info("Player {} needs to create a character", serverPlayer.getName().getString());
                    // Show intro video, then character creation
                    NetworkHandler.sendToPlayer(new S2COpenIntroVideoPacket(true), serverPlayer);
                    return; // Don't sync stats yet
                }

                // Initialize default skill bar if empty
                ResourceLocation[] skillBar = data.getSkillBar();
                boolean hasSkills = false;
                for (ResourceLocation skill : skillBar) {
                    if (skill != null) {
                        hasSkills = true;
                        break;
                    }
                }

                // Set default skills for new players
                if (!hasSkills) {
                    data.setSkillInSlot(0, new ResourceLocation("aetherius", "arcane_bolt"));
                    data.setSkillInSlot(1, new ResourceLocation("aetherius", "flame_dot"));
                    data.setSkillInSlot(2, new ResourceLocation("aetherius", "healing_touch"));
                    data.setSkillInSlot(3, new ResourceLocation("aetherius", "thunderclap"));
                    data.setSkillInSlot(4, new ResourceLocation("aetherius", "meteor_strike"));
                    data.setSkillInSlot(5, new ResourceLocation("aetherius", "battle_stance"));
                    skillBar = data.getSkillBar();
                }

                // Sync stats
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
                ), serverPlayer);

                // Sync skill bar
                NetworkHandler.sendToPlayer(new S2CSkillBarPacket(skillBar), serverPlayer);

                AetheriusMod.LOGGER.debug("Synced RPG data to player {} on login", serverPlayer.getName().getString());
            });
        }
    }

    /**
     * Copy player data on respawn/dimension change.
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            // On death, copy data from old player to new player
            event.getOriginal().getCapability(PlayerRpgDataProvider.PLAYER_RPG_DATA).ifPresent(oldData -> {
                event.getEntity().getCapability(PlayerRpgDataProvider.PLAYER_RPG_DATA).ifPresent(newData -> {
                    newData.copyFrom(oldData);
                    AetheriusMod.LOGGER.debug("Copied RPG data on player death/respawn");
                });
            });
        }
    }

    /**
     * Sync player data on respawn.
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCapability(PlayerRpgDataProvider.PLAYER_RPG_DATA).ifPresent(data -> {
                // Sync stats
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
                ), serverPlayer);

                // Sync skill bar
                NetworkHandler.sendToPlayer(new S2CSkillBarPacket(data.getSkillBar()), serverPlayer);

                AetheriusMod.LOGGER.debug("Synced RPG data to player {} on respawn", serverPlayer.getName().getString());
            });
        }
    }
}
