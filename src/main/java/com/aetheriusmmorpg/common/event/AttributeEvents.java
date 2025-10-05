package com.aetheriusmmorpg.common.event;

import com.aetheriusmmorpg.AetheriusMod;
import com.aetheriusmmorpg.common.capability.player.PlayerRpgDataProvider;
import com.aetheriusmmorpg.common.registry.ModAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Event handlers for applying custom attribute values from PlayerRpgData
 * to the actual entity attribute instances.
 */
@Mod.EventBusSubscriber(modid = AetheriusMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AttributeEvents {

    /**
     * When player joins the world, ensure their custom attributes are applied.
     */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) {
            player.getCapability(PlayerRpgDataProvider.PLAYER_RPG_DATA).ifPresent(data -> {
                applyAttributesFromData(player, data);
            });
        }
    }

    /**
     * Periodically sync attributes (every 20 ticks = 1 second).
     * This ensures attributes stay in sync if modified via capability.
     */
    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player player && !player.level().isClientSide) {
            if (player.tickCount % 20 == 0) { // Once per second
                player.getCapability(PlayerRpgDataProvider.PLAYER_RPG_DATA).ifPresent(data -> {
                    if (data.isDirty()) {
                        applyAttributesFromData(player, data);
                        data.clearDirty();
                    }
                });
            }
        }
    }

    /**
     * Apply attribute values from capability data to player attribute instances.
     */
    private static void applyAttributesFromData(Player player, com.aetheriusmmorpg.common.capability.player.PlayerRpgData data) {
        setAttributeBase(player, ModAttributes.POWER.get(), data.getPower());
        setAttributeBase(player, ModAttributes.SPIRIT.get(), data.getSpirit());
        setAttributeBase(player, ModAttributes.AGILITY.get(), data.getAgility());
        setAttributeBase(player, ModAttributes.DEFENSE.get(), data.getDefense());
        setAttributeBase(player, ModAttributes.CRIT_RATE.get(), data.getCritRate());
        setAttributeBase(player, ModAttributes.HASTE.get(), data.getHaste());
    }

    /**
     * Safely set an attribute's base value.
     */
    private static void setAttributeBase(Player player, net.minecraft.world.entity.ai.attributes.Attribute attribute, double value) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }
}
