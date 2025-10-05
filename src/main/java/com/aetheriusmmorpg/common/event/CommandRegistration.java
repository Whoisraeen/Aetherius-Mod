package com.aetheriusmmorpg.common.event;

import com.aetheriusmmorpg.AetheriusMod;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles command registration.
 */
@Mod.EventBusSubscriber(modid = AetheriusMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandRegistration {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        // Party commands have been deprecated in favor of GUI-based interactions
        // PartyCommand.register(event.getDispatcher());
        // AetheriusMod.LOGGER.info("Registered party commands");
    }
}
