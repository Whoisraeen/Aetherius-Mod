package com.aetheriusmmorpg.common.guild;

import com.aetheriusmmorpg.AetheriusMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

/**
 * Server-side manager for all guilds.
 * Handles guild creation, invitations, and persistence.
 */
public class GuildManager extends SavedData {

    private static final String DATA_NAME = "aetherius_guilds";
    private final Map<UUID, Guild> guilds = new HashMap<>();
    private final Map<UUID, UUID> playerToGuild = new HashMap<>(); // Player UUID -> Guild UUID
    private final Map<UUID, Set<UUID>> guildInvitations = new HashMap<>(); // Player UUID -> Set of Guild UUIDs

    public GuildManager() {
        super();
    }

    /**
     * Create a new guild.
     */
    public Guild createGuild(ServerPlayer leader, String name, String tag) {
        // Validate name and tag
        if (name.length() < 3 || name.length() > 20) {
            leader.sendSystemMessage(Component.literal("§cGuild name must be between 3-20 characters!"));
            return null;
        }

        if (tag.length() < 2 || tag.length() > 5) {
            leader.sendSystemMessage(Component.literal("§cGuild tag must be between 2-5 characters!"));
            return null;
        }

        // Check if name/tag already exists
        for (Guild guild : guilds.values()) {
            if (guild.getName().equalsIgnoreCase(name)) {
                leader.sendSystemMessage(Component.literal("§cA guild with that name already exists!"));
                return null;
            }
            if (guild.getTag().equalsIgnoreCase(tag)) {
                leader.sendSystemMessage(Component.literal("§cA guild with that tag already exists!"));
                return null;
            }
        }

        // Check if player is already in a guild
        if (playerToGuild.containsKey(leader.getUUID())) {
            leader.sendSystemMessage(Component.literal("§cYou are already in a guild!"));
            return null;
        }

        // Create guild
        UUID guildId = UUID.randomUUID();
        Guild guild = new Guild(guildId, name, tag, leader.getUUID());
        guild.addMember(leader.getUUID(), leader.getName().getString());
        guild.setMemberRank(leader.getUUID(), "Leader");

        guilds.put(guildId, guild);
        playerToGuild.put(leader.getUUID(), guildId);
        setDirty();

        leader.sendSystemMessage(Component.literal("§6§lGUILD CREATED!"));
        leader.sendSystemMessage(Component.literal("§eYou have founded the guild [" + tag + "] " + name + "!"));

        AetheriusMod.LOGGER.info("Player {} created guild: [{}] {}", leader.getName().getString(), tag, name);
        return guild;
    }

    /**
     * Disband a guild.
     */
    public boolean disbandGuild(ServerPlayer player, UUID guildId) {
        Guild guild = guilds.get(guildId);
        if (guild == null) return false;

        // Only leader can disband
        if (!guild.getLeaderId().equals(player.getUUID())) {
            player.sendSystemMessage(Component.literal("§cOnly the guild leader can disband the guild!"));
            return false;
        }

        // Remove all members from guild
        for (UUID memberId : guild.getMembers().keySet()) {
            playerToGuild.remove(memberId);

            // Notify online members
            ServerPlayer member = player.getServer().getPlayerList().getPlayer(memberId);
            if (member != null) {
                member.sendSystemMessage(Component.literal("§c§lGUILD DISBANDED!"));
                member.sendSystemMessage(Component.literal("§7The guild [" + guild.getTag() + "] " + guild.getName() + " has been disbanded."));
            }
        }

        guilds.remove(guildId);
        setDirty();

        AetheriusMod.LOGGER.info("Guild disbanded: [{}] {}", guild.getTag(), guild.getName());
        return true;
    }

    /**
     * Invite a player to a guild.
     */
    public void inviteToGuild(ServerPlayer inviter, ServerPlayer target, UUID guildId) {
        Guild guild = guilds.get(guildId);
        if (guild == null) {
            inviter.sendSystemMessage(Component.literal("§cGuild not found!"));
            return;
        }

        // Check inviter has permission
        if (!guild.hasPermission(inviter.getUUID(), GuildPermission.INVITE_MEMBERS)) {
            inviter.sendSystemMessage(Component.literal("§cYou don't have permission to invite members!"));
            return;
        }

        // Check if target is already in a guild
        if (playerToGuild.containsKey(target.getUUID())) {
            inviter.sendSystemMessage(Component.literal("§cThat player is already in a guild!"));
            return;
        }

        // Check if guild is full
        if (guild.getMembers().size() >= guild.getMaxMembers()) {
            inviter.sendSystemMessage(Component.literal("§cYour guild is full!"));
            return;
        }

        // Send invitation
        guildInvitations.computeIfAbsent(target.getUUID(), k -> new HashSet<>()).add(guildId);
        setDirty();

        inviter.sendSystemMessage(Component.literal("§aGuild invitation sent to " + target.getName().getString()));
        target.sendSystemMessage(Component.literal("§6§lGUILD INVITATION!"));
        target.sendSystemMessage(Component.literal("§e" + inviter.getName().getString() + " has invited you to join [" + guild.getTag() + "] " + guild.getName()));
        target.sendSystemMessage(Component.literal("§7Open the Social menu (F) to accept or decline"));
    }

    /**
     * Accept a guild invitation.
     */
    public void acceptGuildInvitation(ServerPlayer player, UUID guildId) {
        Set<UUID> invites = guildInvitations.get(player.getUUID());
        if (invites == null || !invites.contains(guildId)) {
            player.sendSystemMessage(Component.literal("§cYou have no invitation from that guild!"));
            return;
        }

        Guild guild = guilds.get(guildId);
        if (guild == null) {
            player.sendSystemMessage(Component.literal("§cThat guild no longer exists!"));
            guildInvitations.get(player.getUUID()).remove(guildId);
            return;
        }

        // Check if already in a guild
        if (playerToGuild.containsKey(player.getUUID())) {
            player.sendSystemMessage(Component.literal("§cYou are already in a guild!"));
            return;
        }

        // Check if guild is full
        if (guild.getMembers().size() >= guild.getMaxMembers()) {
            player.sendSystemMessage(Component.literal("§cThat guild is now full!"));
            guildInvitations.get(player.getUUID()).remove(guildId);
            return;
        }

        // Add to guild
        guild.addMember(player.getUUID(), player.getName().getString());
        playerToGuild.put(player.getUUID(), guildId);
        guildInvitations.get(player.getUUID()).remove(guildId);
        setDirty();

        player.sendSystemMessage(Component.literal("§a§lWELCOME!"));
        player.sendSystemMessage(Component.literal("§eYou have joined [" + guild.getTag() + "] " + guild.getName() + "!"));

        // Notify guild members
        broadcastToGuild(guildId, Component.literal("§b[Guild] §f" + player.getName().getString() + " has joined the guild!"), player.getServer());

        AetheriusMod.LOGGER.info("Player {} joined guild: [{}] {}", player.getName().getString(), guild.getTag(), guild.getName());
    }

    /**
     * Decline a guild invitation.
     */
    public void declineGuildInvitation(ServerPlayer player, UUID guildId) {
        Set<UUID> invites = guildInvitations.get(player.getUUID());
        if (invites != null) {
            invites.remove(guildId);
            player.sendSystemMessage(Component.literal("§7Guild invitation declined."));
            setDirty();
        }
    }

    /**
     * Leave a guild.
     */
    public void leaveGuild(ServerPlayer player) {
        UUID guildId = playerToGuild.get(player.getUUID());
        if (guildId == null) {
            player.sendSystemMessage(Component.literal("§cYou are not in a guild!"));
            return;
        }

        Guild guild = guilds.get(guildId);
        if (guild == null) return;

        // Leader cannot leave, must transfer or disband
        if (guild.getLeaderId().equals(player.getUUID())) {
            player.sendSystemMessage(Component.literal("§cGuild leaders must transfer leadership or disband the guild before leaving!"));
            return;
        }

        // Remove from guild
        guild.removeMember(player.getUUID());
        playerToGuild.remove(player.getUUID());
        setDirty();

        player.sendSystemMessage(Component.literal("§7You have left the guild."));
        broadcastToGuild(guildId, Component.literal("§b[Guild] §f" + player.getName().getString() + " has left the guild."), player.getServer());
    }

    /**
     * Kick a member from a guild.
     */
    public void kickMember(ServerPlayer kicker, UUID targetId, UUID guildId) {
        Guild guild = guilds.get(guildId);
        if (guild == null) return;

        if (!guild.hasPermission(kicker.getUUID(), GuildPermission.KICK_MEMBERS)) {
            kicker.sendSystemMessage(Component.literal("§cYou don't have permission to kick members!"));
            return;
        }

        GuildMember targetMember = guild.getMember(targetId);
        if (targetMember == null) {
            kicker.sendSystemMessage(Component.literal("§cThat player is not in your guild!"));
            return;
        }

        // Cannot kick the leader
        if (guild.getLeaderId().equals(targetId)) {
            kicker.sendSystemMessage(Component.literal("§cYou cannot kick the guild leader!"));
            return;
        }

        // Remove from guild
        guild.removeMember(targetId);
        playerToGuild.remove(targetId);
        setDirty();

        kicker.sendSystemMessage(Component.literal("§7" + targetMember.getPlayerName() + " has been kicked from the guild."));

        // Notify target if online
        ServerPlayer target = kicker.getServer().getPlayerList().getPlayer(targetId);
        if (target != null) {
            target.sendSystemMessage(Component.literal("§c§lKICKED!"));
            target.sendSystemMessage(Component.literal("§7You have been kicked from [" + guild.getTag() + "] " + guild.getName()));
        }

        broadcastToGuild(guildId, Component.literal("§b[Guild] §f" + targetMember.getPlayerName() + " was kicked from the guild."), kicker.getServer());
    }

    /**
     * Broadcast a message to all online guild members.
     */
    public void broadcastToGuild(UUID guildId, Component message, MinecraftServer server) {
        Guild guild = guilds.get(guildId);
        if (guild == null) return;

        for (UUID memberId : guild.getMembers().keySet()) {
            ServerPlayer member = server.getPlayerList().getPlayer(memberId);
            if (member != null) {
                member.sendSystemMessage(message);
            }
        }
    }

    /**
     * Get the guild a player is in.
     */
    public Guild getPlayerGuild(UUID playerId) {
        UUID guildId = playerToGuild.get(playerId);
        return guildId != null ? guilds.get(guildId) : null;
    }

    /**
     * Get a guild by ID.
     */
    public Guild getGuild(UUID guildId) {
        return guilds.get(guildId);
    }

    /**
     * Get all guilds.
     */
    public Collection<Guild> getAllGuilds() {
        return new ArrayList<>(guilds.values());
    }

    /**
     * Get pending guild invitations for a player.
     */
    public Set<UUID> getPlayerInvitations(UUID playerId) {
        return guildInvitations.getOrDefault(playerId, new HashSet<>());
    }

    // NBT Serialization
    @Override
    public CompoundTag save(CompoundTag tag) {
        // Save guilds
        ListTag guildsTag = new ListTag();
        for (Guild guild : guilds.values()) {
            guildsTag.add(guild.serializeNBT());
        }
        tag.put("Guilds", guildsTag);

        // Save player->guild mapping
        CompoundTag mappingTag = new CompoundTag();
        for (Map.Entry<UUID, UUID> entry : playerToGuild.entrySet()) {
            mappingTag.putUUID(entry.getKey().toString(), entry.getValue());
        }
        tag.put("PlayerToGuild", mappingTag);

        // Save invitations
        CompoundTag invitationsTag = new CompoundTag();
        for (Map.Entry<UUID, Set<UUID>> entry : guildInvitations.entrySet()) {
            ListTag inviteList = new ListTag();
            for (UUID guildId : entry.getValue()) {
                CompoundTag inviteTag = new CompoundTag();
                inviteTag.putUUID("GuildId", guildId);
                inviteList.add(inviteTag);
            }
            invitationsTag.put(entry.getKey().toString(), inviteList);
        }
        tag.put("Invitations", invitationsTag);

        return tag;
    }

    public void load(CompoundTag tag) {
        guilds.clear();
        playerToGuild.clear();
        guildInvitations.clear();

        // Load guilds
        ListTag guildsTag = tag.getList("Guilds", 10);
        for (int i = 0; i < guildsTag.size(); i++) {
            Guild guild = Guild.deserializeNBT(guildsTag.getCompound(i));
            guilds.put(guild.getGuildId(), guild);

            // Rebuild player->guild mapping
            for (UUID memberId : guild.getMembers().keySet()) {
                playerToGuild.put(memberId, guild.getGuildId());
            }
        }

        // Load player->guild mapping (legacy support)
        if (tag.contains("PlayerToGuild")) {
            CompoundTag mappingTag = tag.getCompound("PlayerToGuild");
            for (String key : mappingTag.getAllKeys()) {
                UUID playerId = UUID.fromString(key);
                UUID guildId = mappingTag.getUUID(key);
                playerToGuild.put(playerId, guildId);
            }
        }

        // Load invitations
        if (tag.contains("Invitations")) {
            CompoundTag invitationsTag = tag.getCompound("Invitations");
            for (String key : invitationsTag.getAllKeys()) {
                UUID playerId = UUID.fromString(key);
                ListTag inviteList = invitationsTag.getList(key, 10);
                Set<UUID> invites = new HashSet<>();
                for (int i = 0; i < inviteList.size(); i++) {
                    CompoundTag inviteTag = inviteList.getCompound(i);
                    invites.add(inviteTag.getUUID("GuildId"));
                }
                guildInvitations.put(playerId, invites);
            }
        }
    }

    // Static access
    public static GuildManager get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
            tag -> {
                GuildManager manager = new GuildManager();
                manager.load(tag);
                return manager;
            },
            GuildManager::new,
            DATA_NAME
        );
    }
}
