package com.aetheriusmmorpg;

import com.aetheriusmmorpg.common.registry.*;
import com.aetheriusmmorpg.network.NetworkHandler;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Main mod class for Aetherius MMO.
 * Server-authoritative MMORPG mod with races, classes, skills, dungeons, and territory wars.
 */
@Mod(AetheriusMod.MOD_ID)
public class AetheriusMod {
    public static final String MOD_ID = "aetherius";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AetheriusMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register all deferred registers
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModMobEffects.MOB_EFFECTS.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        ModParticles.PARTICLES.register(modEventBus);
        ModAttributes.ATTRIBUTES.register(modEventBus);

        // Setup lifecycle events
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        // Register ourselves for server and other game events
        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("Aetherius MMO initializing...");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Initialize networking
            NetworkHandler.register();
            LOGGER.info("Aetherius networking initialized");
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Client-side initialization will go here
            LOGGER.info("Aetherius client initialized");
        });
    }
}
