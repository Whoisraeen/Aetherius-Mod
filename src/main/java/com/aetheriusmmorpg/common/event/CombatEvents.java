package com.aetheriusmmorpg.common.event;

import com.aetheriusmmorpg.AetheriusMod;
import com.aetheriusmmorpg.common.capability.player.PlayerRpgData;
import com.aetheriusmmorpg.common.entity.AetheriusMob;
import com.aetheriusmmorpg.common.party.Party;
import com.aetheriusmmorpg.common.party.PartyManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles combat-related events including XP distribution for parties.
 */
@Mod.EventBusSubscriber(modid = AetheriusMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CombatEvents {

    /**
     * Handle mob death and distribute XP to party members (PWI-style).
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();

        // Only handle server-side
        if (entity.level().isClientSide) {
            return;
        }

        // Check if killed by a player
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof ServerPlayer killer)) {
            return;
        }

        // Calculate XP reward
        int xpReward = calculateXPReward(entity, killer);
        if (xpReward <= 0) {
            return;
        }

        // Check if killer is in a party
        PartyManager partyManager = PartyManager.get(killer.getServer());
        Party party = partyManager.getPlayerParty(killer.getUUID());

        if (party == null || !party.isShareExperience()) {
            // No party or XP sharing disabled - give all XP to killer
            giveExperience(killer, xpReward);
            return;
        }

        // Get all online party members in range
        List<ServerPlayer> onlineMembers = partyManager.getOnlineMembers(party, killer.getServer());
        List<ServerPlayer> membersInRange = new ArrayList<>();

        for (ServerPlayer member : onlineMembers) {
            if (party.isInRange(killer, member)) {
                membersInRange.add(member);
            }
        }

        if (membersInRange.isEmpty()) {
            // No one in range, give XP to killer only
            giveExperience(killer, xpReward);
            return;
        }

        // Distribute XP among party members in range (PWI-style: equal split)
        int sharedXP = xpReward / membersInRange.size();

        // Bonus XP for being in party (10% bonus, split among members)
        int bonusXP = (int) (xpReward * 0.1f / membersInRange.size());

        for (ServerPlayer member : membersInRange) {
            int totalXP = sharedXP + bonusXP;
            giveExperience(member, totalXP);
        }
    }

    /**
     * Calculate XP reward based on mob type and level.
     */
    private static int calculateXPReward(LivingEntity entity, ServerPlayer killer) {
        // Base XP for vanilla mobs
        int baseXP = 10;

        // Check if it's a custom Aetherius mob
        if (entity instanceof AetheriusMob aetheriusMob) {
            baseXP = aetheriusMob.getXpReward();
        }

        // Level difference modifier (reduce XP if mob is too low level)
        int mobLevel = getMobLevel(entity);
        int playerLevel = getPlayerLevel(killer);
        int levelDiff = playerLevel - mobLevel;

        float levelModifier = 1.0f;
        if (levelDiff > 10) {
            // Significantly reduced XP for low-level mobs
            levelModifier = 0.1f;
        } else if (levelDiff > 5) {
            // Moderately reduced XP
            levelModifier = 0.5f;
        } else if (levelDiff < -5) {
            // Bonus XP for higher level mobs
            levelModifier = 1.5f;
        }

        return (int) (baseXP * levelModifier);
    }

    /**
     * Get mob level (for custom mobs).
     */
    private static int getMobLevel(LivingEntity entity) {
        if (entity instanceof AetheriusMob aetheriusMob) {
            return aetheriusMob.getMobLevel();
        }
        return 1; // Default level for vanilla mobs
    }

    /**
     * Get player level from RPG data.
     */
    private static int getPlayerLevel(ServerPlayer player) {
        return player.getCapability(PlayerRpgData.CAPABILITY)
            .map(PlayerRpgData::getLevel)
            .orElse(1);
    }

    /**
     * Give experience to a player.
     */
    private static void giveExperience(ServerPlayer player, int amount) {
        player.getCapability(PlayerRpgData.CAPABILITY).ifPresent(data -> {
            int oldLevel = data.getLevel();
            data.addExperience(amount);
            int newLevel = data.getLevel();

            // Show XP gained message
            player.sendSystemMessage(Component.literal("§a+§f" + amount + " XP"));

            // Show level up message
            if (newLevel > oldLevel) {
                player.sendSystemMessage(Component.literal("§6§lLEVEL UP! §fYou are now level " + newLevel));
                player.sendSystemMessage(Component.literal("§7All attributes increased!"));

                // Play level up sound
                player.playSound(net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, 1.0f, 1.0f);
            }
        });
    }
}
