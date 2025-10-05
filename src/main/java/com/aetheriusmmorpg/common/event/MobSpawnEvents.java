package com.aetheriusmmorpg.common.event;

import com.aetheriusmmorpg.AetheriusMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.*;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles mob spawning events, including disabling vanilla hostile mobs.
 */
@Mod.EventBusSubscriber(modid = AetheriusMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MobSpawnEvents {

    /**
     * Cancel all vanilla hostile mob spawns.
     * This allows only custom Aetherius mobs to spawn.
     */
    @SubscribeEvent
    public static void onMobSpawn(MobSpawnEvent.FinalizeSpawn event) {
        // Check if it's a vanilla hostile mob
        if (isVanillaHostileMob(event.getEntity().getType())) {
            event.setSpawnCancelled(true);
            event.setCanceled(true);
            event.setResult(Event.Result.DENY);

            // Optional: Log for debugging
            // AetheriusMod.LOGGER.debug("Cancelled vanilla mob spawn: {}", event.getEntity().getType());
        }
    }

    /**
     * Check if entity type is a vanilla hostile mob.
     */
    private static boolean isVanillaHostileMob(EntityType<?> type) {
        return type == EntityType.ZOMBIE ||
               type == EntityType.SKELETON ||
               type == EntityType.CREEPER ||
               type == EntityType.SPIDER ||
               type == EntityType.CAVE_SPIDER ||
               type == EntityType.ENDERMAN ||
               type == EntityType.WITCH ||
               type == EntityType.BLAZE ||
               type == EntityType.GHAST ||
               type == EntityType.SLIME ||
               type == EntityType.MAGMA_CUBE ||
               type == EntityType.SILVERFISH ||
               type == EntityType.ENDERMITE ||
               type == EntityType.GUARDIAN ||
               type == EntityType.ELDER_GUARDIAN ||
               type == EntityType.SHULKER ||
               type == EntityType.PHANTOM ||
               type == EntityType.DROWNED ||
               type == EntityType.HUSK ||
               type == EntityType.STRAY ||
               type == EntityType.WITHER_SKELETON ||
               type == EntityType.ZOMBIE_VILLAGER ||
               type == EntityType.PIGLIN ||
               type == EntityType.PIGLIN_BRUTE ||
               type == EntityType.HOGLIN ||
               type == EntityType.ZOGLIN ||
               type == EntityType.ZOMBIFIED_PIGLIN ||
               type == EntityType.RAVAGER ||
               type == EntityType.VEX ||
               type == EntityType.VINDICATOR ||
               type == EntityType.EVOKER ||
               type == EntityType.PILLAGER ||
               type == EntityType.WARDEN ||
               type == EntityType.WITHER;
    }

    /**
     * Alternative: Check by class type (more comprehensive but might catch modded mobs too)
     */
    private static boolean isVanillaMonster(EntityType<?> type) {
        try {
            Class<?> entityClass = type.getBaseClass();
            return Monster.class.isAssignableFrom(entityClass) &&
                   !entityClass.getPackage().getName().contains("aetheriusmmorpg");
        } catch (Exception e) {
            return false;
        }
    }
}
