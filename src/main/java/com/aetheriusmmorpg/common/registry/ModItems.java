package com.aetheriusmmorpg.common.registry;

import com.aetheriusmmorpg.AetheriusMod;
import com.aetheriusmmorpg.common.item.AetheriusWeaponItem;
import com.aetheriusmmorpg.common.item.HealthPotionItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry for all Aetherius items (weapons, armor, consumables, crafting materials).
 */
public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, AetheriusMod.MOD_ID);

    // Starter Weapon
    public static final RegistryObject<Item> BASIC_SWORD = ITEMS.register("basic_sword",
        () -> new AetheriusWeaponItem(
            Tiers.IRON,
            3,  // attack damage
            -2.4F,  // attack speed
            5.0,  // bonus power
            2.0,  // bonus crit rate
            new Item.Properties()
        ));

    // Currency
    public static final RegistryObject<Item> GOLD_COIN = ITEMS.register("gold_coin",
        () -> new Item(new Item.Properties().stacksTo(999)));

    // Crafting Materials
    public static final RegistryObject<Item> SPECTRAL_ESSENCE = ITEMS.register("spectral_essence",
        () -> new Item(new Item.Properties().stacksTo(64)));

    // Consumables
    public static final RegistryObject<Item> HEALTH_POTION = ITEMS.register("health_potion",
        () -> new HealthPotionItem(
            10.0F,  // heal amount
            new Item.Properties().stacksTo(16)
        ));
}
