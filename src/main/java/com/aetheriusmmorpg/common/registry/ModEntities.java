package com.aetheriusmmorpg.common.registry;

import com.aetheriusmmorpg.AetheriusMod;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Registry for all Aetherius entities (mobs, bosses, NPCs, projectiles).
 */
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AetheriusMod.MOD_ID);

    // Entities will be registered in M4+
}
