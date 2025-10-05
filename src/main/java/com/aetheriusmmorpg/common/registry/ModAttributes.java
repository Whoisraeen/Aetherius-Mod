package com.aetheriusmmorpg.common.registry;

import com.aetheriusmmorpg.AetheriusMod;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry for custom RPG attributes (Power, Spirit, Agility, Defense, CritRate, Haste).
 * These extend vanilla's attribute system for MMO-style stats.
 */
public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
        DeferredRegister.create(ForgeRegistries.ATTRIBUTES, AetheriusMod.MOD_ID);

    // Custom RPG attributes - will be implemented in M2
    public static final RegistryObject<Attribute> POWER = ATTRIBUTES.register("power",
        () -> new RangedAttribute("attribute.aetherius.power", 10.0, 0.0, 10000.0).setSyncable(true));

    public static final RegistryObject<Attribute> SPIRIT = ATTRIBUTES.register("spirit",
        () -> new RangedAttribute("attribute.aetherius.spirit", 10.0, 0.0, 10000.0).setSyncable(true));

    public static final RegistryObject<Attribute> AGILITY = ATTRIBUTES.register("agility",
        () -> new RangedAttribute("attribute.aetherius.agility", 10.0, 0.0, 10000.0).setSyncable(true));

    public static final RegistryObject<Attribute> DEFENSE = ATTRIBUTES.register("defense",
        () -> new RangedAttribute("attribute.aetherius.defense", 10.0, 0.0, 10000.0).setSyncable(true));

    public static final RegistryObject<Attribute> CRIT_RATE = ATTRIBUTES.register("crit_rate",
        () -> new RangedAttribute("attribute.aetherius.crit_rate", 5.0, 0.0, 100.0).setSyncable(true));

    public static final RegistryObject<Attribute> HASTE = ATTRIBUTES.register("haste",
        () -> new RangedAttribute("attribute.aetherius.haste", 0.0, 0.0, 100.0).setSyncable(true));
}
