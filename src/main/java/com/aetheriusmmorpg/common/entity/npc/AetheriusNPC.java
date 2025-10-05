package com.aetheriusmmorpg.common.entity.npc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Base class for all Aetherius NPCs.
 * NPCs can give quests, trade, train skills, and provide information.
 */
public class AetheriusNPC extends PathfinderMob {

    private static final EntityDataAccessor<String> NPC_ID =
        SynchedEntityData.defineId(AetheriusNPC.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<String> NPC_TYPE =
        SynchedEntityData.defineId(AetheriusNPC.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<String> DISPLAY_NAME =
        SynchedEntityData.defineId(AetheriusNPC.class, EntityDataSerializers.STRING);

    public AetheriusNPC(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired(); // NPCs don't despawn
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(NPC_ID, "");
        this.entityData.define(NPC_TYPE, NPCType.GENERIC.name());
        this.entityData.define(DISPLAY_NAME, "NPC");
    }

    @Override
    protected void registerGoals() {
        // NPCs are mostly stationary but can wander
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 0.5D));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.5D)
            .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    // NPC Properties
    public String getNPCId() {
        return this.entityData.get(NPC_ID);
    }

    public void setNPCId(String id) {
        this.entityData.set(NPC_ID, id);
    }

    public NPCType getNPCType() {
        try {
            return NPCType.valueOf(this.entityData.get(NPC_TYPE));
        } catch (IllegalArgumentException e) {
            return NPCType.GENERIC;
        }
    }

    public void setNPCType(NPCType type) {
        this.entityData.set(NPC_TYPE, type.name());
    }

    public String getNpcDisplayName() {
        return this.entityData.get(DISPLAY_NAME);
    }

    public void setNpcDisplayName(String name) {
        this.entityData.set(DISPLAY_NAME, name);
    }

    @Override
    public Component getName() {
        String name = getNpcDisplayName();
        String typePrefix = switch (getNPCType()) {
            case QUEST_GIVER -> "§e[!] ";
            case MERCHANT -> "§6[Shop] ";
            case TRAINER -> "§b[Trainer] ";
            case GUILD_MASTER -> "§5[Guild] ";
            default -> "";
        };
        return Component.literal(typePrefix + "§f" + name);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide) {
            // Server-side interaction handling
            handleInteraction(player);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    /**
     * Handle player interaction with NPC.
     * Override in subclasses for specific behavior.
     */
    protected void handleInteraction(Player player) {
        switch (getNPCType()) {
            case QUEST_GIVER:
                openQuestDialog(player);
                break;
            case MERCHANT:
                openMerchantDialog(player);
                break;
            case TRAINER:
                openTrainerDialog(player);
                break;
            case GUILD_MASTER:
                openGuildDialog(player);
                break;
            default:
                openGenericDialog(player);
                break;
        }
    }

    protected void openQuestDialog(Player player) {
        // Open quest dialog GUI
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            String npcId = getNPCId();
            net.minecraftforge.network.NetworkHooks.openScreen(serverPlayer,
                new net.minecraft.world.SimpleMenuProvider(
                    (containerId, inventory, p) -> new com.aetheriusmmorpg.common.menu.QuestDialogMenu(containerId, inventory, npcId),
                    Component.translatable("gui.aetherius.quest_dialog")
                )
            );
        }
    }

    protected void openMerchantDialog(Player player) {
        // Open merchant GUI
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            String npcId = getNPCId();
            net.minecraftforge.network.NetworkHooks.openScreen(serverPlayer,
                new net.minecraft.world.SimpleMenuProvider(
                    (containerId, inventory, p) -> new com.aetheriusmmorpg.common.menu.MerchantMenu(containerId, inventory, npcId),
                    Component.translatable("gui.aetherius.merchant")
                )
            );
        }
    }

    protected void openTrainerDialog(Player player) {
        // Open skill trainer GUI
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            String npcId = getNPCId();
            net.minecraftforge.network.NetworkHooks.openScreen(serverPlayer,
                new net.minecraft.world.SimpleMenuProvider(
                    (containerId, inventory, p) -> new com.aetheriusmmorpg.common.menu.SkillTrainerMenu(containerId, inventory, npcId),
                    Component.translatable("gui.aetherius.skill_trainer")
                )
            );
        }
    }

    protected void openGuildDialog(Player player) {
        // Open guild management GUI
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            net.minecraftforge.network.NetworkHooks.openScreen(serverPlayer,
                new net.minecraft.world.SimpleMenuProvider(
                    (containerId, inventory, p) -> new com.aetheriusmmorpg.common.menu.GuildMenu(containerId, inventory),
                    Component.translatable("gui.aetherius.guild")
                )
            );
            
            // Sync guild data to client
            com.aetheriusmmorpg.common.guild.GuildManager manager = com.aetheriusmmorpg.common.guild.GuildManager.get(serverPlayer.getServer());
            com.aetheriusmmorpg.common.guild.Guild guild = manager.getPlayerGuild(serverPlayer.getUUID());
            
            if (guild != null) {
                com.aetheriusmmorpg.network.NetworkHandler.sendToPlayer(
                    new com.aetheriusmmorpg.network.packet.guild.S2CGuildDataPacket(guild), 
                    serverPlayer
                );
            } else {
                com.aetheriusmmorpg.network.NetworkHandler.sendToPlayer(
                    new com.aetheriusmmorpg.network.packet.guild.S2CGuildDataPacket(), 
                    serverPlayer
                );
            }
        }
    }

    protected void openGenericDialog(Player player) {
        player.sendSystemMessage(Component.literal("§f" + getNpcDisplayName() + "§f: Hello, traveler!"));
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false; // NPCs never despawn
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return false; // NPCs cannot be leashed
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("NPCId", getNPCId());
        tag.putString("NPCType", getNPCType().name());
        tag.putString("DisplayName", getNpcDisplayName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("NPCId")) {
            setNPCId(tag.getString("NPCId"));
        }
        if (tag.contains("NPCType")) {
            try {
                setNPCType(NPCType.valueOf(tag.getString("NPCType")));
            } catch (IllegalArgumentException ignored) {}
        }
        if (tag.contains("DisplayName")) {
            setNpcDisplayName(tag.getString("DisplayName"));
        }
    }

    /**
     * NPC type classification.
     */
    public enum NPCType {
        GENERIC,        // General NPC with dialogue
        QUEST_GIVER,    // Gives and completes quests
        MERCHANT,       // Sells items and equipment
        TRAINER,        // Trains skills and abilities
        GUILD_MASTER,   // Manages guild operations
        BANKER,         // Bank and storage
        INNKEEPER,      // Rest and recovery
        BLACKSMITH,     // Repairs and upgrades equipment
        GUARD           // City guards and protectors
    }
}
