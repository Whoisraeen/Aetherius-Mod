package com.aetheriusmmorpg.common.registry;

import com.aetheriusmmorpg.AetheriusMod;
import com.aetheriusmmorpg.common.menu.CharacterCreationMenu;
import com.aetheriusmmorpg.common.menu.CharacterSheetMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry for all Aetherius menus/containers (character sheet, skill tree, quest log, etc).
 */
public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(ForgeRegistries.MENU_TYPES, AetheriusMod.MOD_ID);

    public static final RegistryObject<MenuType<CharacterSheetMenu>> CHARACTER_SHEET =
        MENUS.register("character_sheet",
            () -> IForgeMenuType.create((windowId, inv, data) -> new CharacterSheetMenu(windowId, inv)));

    public static final RegistryObject<MenuType<CharacterCreationMenu>> CHARACTER_CREATION =
        MENUS.register("character_creation",
            () -> IForgeMenuType.create((windowId, inv, data) -> new CharacterCreationMenu(windowId, inv)));
}
