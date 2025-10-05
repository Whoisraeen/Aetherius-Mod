package com.aetheriusmmorpg.common.registry;

import com.aetheriusmmorpg.AetheriusMod;
import com.aetheriusmmorpg.common.entity.hostile.ShadowMinionEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry for all Aetherius entities (mobs, bosses, NPCs, projectiles).
 */
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AetheriusMod.MOD_ID);

    // Shadow Minion - summoned by Shadow Lord Boss
    public static final RegistryObject<EntityType<ShadowMinionEntity>> SHADOW_MINION =
        ENTITIES.register("shadow_minion",
            () -> EntityType.Builder.of(ShadowMinionEntity::new, MobCategory.MONSTER)
                .sized(0.6F, 1.8F)
                .clientTrackingRange(8)
                .build("shadow_minion"));
}
