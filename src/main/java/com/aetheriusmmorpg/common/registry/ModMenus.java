package com.aetheriusmmorpg.common.registry;

import com.aetheriusmmorpg.AetheriusMod;
import com.aetheriusmmorpg.common.menu.CharacterCreationMenu;
import com.aetheriusmmorpg.common.menu.CharacterSheetMenu;
import com.aetheriusmmorpg.common.menu.GuildMenu;
import com.aetheriusmmorpg.common.menu.QuestDialogMenu;
import com.aetheriusmmorpg.common.menu.MerchantMenu;
import com.aetheriusmmorpg.common.menu.SkillTrainerMenu;
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

    public static final RegistryObject<MenuType<GuildMenu>> GUILD =
        MENUS.register("guild",
            () -> IForgeMenuType.create((windowId, inv, data) -> new GuildMenu(windowId, inv)));

    public static final RegistryObject<MenuType<QuestDialogMenu>> QUEST_DIALOG =
        MENUS.register("quest_dialog",
            () -> IForgeMenuType.create((windowId, inv, data) -> new QuestDialogMenu(windowId, inv)));

    public static final RegistryObject<MenuType<MerchantMenu>> MERCHANT =
        MENUS.register("merchant",
            () -> IForgeMenuType.create((windowId, inv, data) -> new MerchantMenu(windowId, inv)));

    public static final RegistryObject<MenuType<SkillTrainerMenu>> SKILL_TRAINER =
        MENUS.register("skill_trainer",
            () -> IForgeMenuType.create((windowId, inv, data) -> new SkillTrainerMenu(windowId, inv)));
}
