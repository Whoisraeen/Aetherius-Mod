package com.aetheriusmmorpg.common.event;

import com.aetheriusmmorpg.AetheriusMod;
import com.aetheriusmmorpg.common.dungeon.DungeonManager;
import com.aetheriusmmorpg.common.quest.QuestManager;
import com.aetheriusmmorpg.common.rpg.clazz.ClassManager;
import com.aetheriusmmorpg.common.rpg.race.RaceManager;
import com.aetheriusmmorpg.common.rpg.skill.SkillManager;
import com.aetheriusmmorpg.common.world.StartingCityManager;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Registers datapack reload listeners for races, classes, and skills.
 */
@Mod.EventBusSubscriber(modid = AetheriusMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DatapackEvents {

    public static final RaceManager RACE_MANAGER = new RaceManager();
    public static final ClassManager CLASS_MANAGER = new ClassManager();
    public static final SkillManager SKILL_MANAGER = new SkillManager();
    public static final QuestManager QUEST_MANAGER = new QuestManager();
    public static final DungeonManager.DungeonReloadListener DUNGEON_MANAGER = new DungeonManager.DungeonReloadListener();
    public static final StartingCityManager STARTING_CITY_MANAGER = new StartingCityManager();

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(RACE_MANAGER);
        event.addListener(CLASS_MANAGER);
        event.addListener(SKILL_MANAGER);
        event.addListener(QUEST_MANAGER);
        event.addListener(DUNGEON_MANAGER);
        event.addListener(STARTING_CITY_MANAGER);
        AetheriusMod.LOGGER.info("Registered datapack reload listeners");
    }
}
