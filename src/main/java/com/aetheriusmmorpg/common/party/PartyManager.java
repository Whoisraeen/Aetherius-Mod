package com.aetheriusmmorpg.common.party;

import com.aetheriusmmorpg.network.NetworkHandler;
import com.aetheriusmmorpg.network.packet.party.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

/**
 * Server-side manager for all parties.
 * Handles party creation, invitations, and member management.
 */
public class PartyManager extends SavedData {

    private static final String DATA_NAME = "aetherius_parties";

    // Active parties
    private final Map<UUID, Party> parties = new HashMap<>();

    // Player to Party mapping
    private final Map<UUID, UUID> playerToParty = new HashMap<>();

    // Pending invitations (invitee UUID -> Party ID)
    private final Map<UUID, UUID> pendingInvites = new HashMap<>();

    public PartyManager() {
        super();
    }

    /**
     * Get or create the party manager for a server.
     */
    public static PartyManager get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
            PartyManager::load,
            PartyManager::new,
            DATA_NAME
        );
    }

    /**
     * Create a new party with the given player as leader.
     */
    public Party createParty(ServerPlayer leader) {
        // Check if already in a party
        if (isInParty(leader.getUUID())) {
            return null;
        }

        Party party = new Party(leader.getUUID());
        parties.put(party.getPartyId(), party);
        playerToParty.put(leader.getUUID(), party.getPartyId());

        setDirty();

        // Notify the leader
        leader.sendSystemMessage(Component.literal("§aYou have created a party!"));

        // Sync party data to leader
        syncPartyToMembers(party, leader.getServer());

        return party;
    }

    /**
     * Invite a player to a party.
     */
    public boolean invitePlayer(ServerPlayer inviter, ServerPlayer invitee) {
        Party party = getPlayerParty(inviter.getUUID());

        if (party == null) {
            inviter.sendSystemMessage(Component.literal("§cYou are not in a party!"));
            return false;
        }

        if (!party.isLeader(inviter.getUUID())) {
            inviter.sendSystemMessage(Component.literal("§cOnly the party leader can invite players!"));
            return false;
        }

        if (party.isFull()) {
            inviter.sendSystemMessage(Component.literal("§cYour party is full!"));
            return false;
        }

        if (isInParty(invitee.getUUID())) {
            inviter.sendSystemMessage(Component.literal("§c" + invitee.getName().getString() + " is already in a party!"));
            return false;
        }

        if (pendingInvites.containsKey(invitee.getUUID())) {
            inviter.sendSystemMessage(Component.literal("§c" + invitee.getName().getString() + " already has a pending invitation!"));
            return false;
        }

        // Send invitation
        pendingInvites.put(invitee.getUUID(), party.getPartyId());

        inviter.sendSystemMessage(Component.literal("§aInvited " + invitee.getName().getString() + " to the party."));

        // Send invitation packet to invitee
        NetworkHandler.sendToPlayer(
            new S2CPartyInvitePacket(inviter.getUUID(), inviter.getName().getString()),
            invitee
        );

        setDirty();
        return true;
    }

    /**
     * Accept a party invitation.
     */
    public boolean acceptInvite(ServerPlayer player) {
        UUID partyId = pendingInvites.remove(player.getUUID());

        if (partyId == null) {
            player.sendSystemMessage(Component.literal("§cYou have no pending party invitations!"));
            return false;
        }

        Party party = parties.get(partyId);
        if (party == null) {
            player.sendSystemMessage(Component.literal("§cThat party no longer exists!"));
            return false;
        }

        if (party.isFull()) {
            player.sendSystemMessage(Component.literal("§cThat party is now full!"));
            return false;
        }

        if (isInParty(player.getUUID())) {
            player.sendSystemMessage(Component.literal("§cYou are already in a party!"));
            return false;
        }

        // Add to party
        party.addMember(player.getUUID());
        playerToParty.put(player.getUUID(), party.getPartyId());

        setDirty();

        // Notify all party members
        notifyPartyMembers(party, Component.literal("§a" + player.getName().getString() + " has joined the party!"), player.getServer());

        // Sync party data to all members
        syncPartyToMembers(party, player.getServer());

        return true;
    }

    /**
     * Decline a party invitation.
     */
    public boolean declineInvite(ServerPlayer player) {
        UUID partyId = pendingInvites.remove(player.getUUID());

        if (partyId == null) {
            return false;
        }

        Party party = parties.get(partyId);
        if (party != null) {
            ServerPlayer leader = player.getServer().getPlayerList().getPlayer(party.getLeaderId());
            if (leader != null) {
                leader.sendSystemMessage(Component.literal("§c" + player.getName().getString() + " declined the party invitation."));
            }
        }

        setDirty();
        return true;
    }

    /**
     * Leave a party.
     */
    public boolean leaveParty(ServerPlayer player) {
        Party party = getPlayerParty(player.getUUID());

        if (party == null) {
            player.sendSystemMessage(Component.literal("§cYou are not in a party!"));
            return false;
        }

        UUID playerId = player.getUUID();
        boolean wasLeader = party.isLeader(playerId);

        party.removeMember(playerId);
        playerToParty.remove(playerId);

        player.sendSystemMessage(Component.literal("§aYou have left the party."));

        // If party is now empty, disband it
        if (party.getMemberCount() == 0) {
            parties.remove(party.getPartyId());
        } else {
            // Notify remaining members
            String message = wasLeader ?
                "§c" + player.getName().getString() + " (Leader) has left the party. Leadership transferred." :
                "§c" + player.getName().getString() + " has left the party.";
            notifyPartyMembers(party, Component.literal(message), player.getServer());

            // Sync updated party
            syncPartyToMembers(party, player.getServer());
        }

        // Clear party data for the player who left
        NetworkHandler.sendToPlayer(new S2CPartyUpdatePacket(null, Collections.emptyList()), player);

        setDirty();
        return true;
    }

    /**
     * Kick a player from the party.
     */
    public boolean kickPlayer(ServerPlayer kicker, UUID targetId) {
        Party party = getPlayerParty(kicker.getUUID());

        if (party == null) {
            kicker.sendSystemMessage(Component.literal("§cYou are not in a party!"));
            return false;
        }

        if (!party.isLeader(kicker.getUUID())) {
            kicker.sendSystemMessage(Component.literal("§cOnly the party leader can kick members!"));
            return false;
        }

        if (!party.isMember(targetId)) {
            kicker.sendSystemMessage(Component.literal("§cThat player is not in your party!"));
            return false;
        }

        if (targetId.equals(kicker.getUUID())) {
            kicker.sendSystemMessage(Component.literal("§cYou cannot kick yourself! Use /party leave instead."));
            return false;
        }

        ServerPlayer target = kicker.getServer().getPlayerList().getPlayer(targetId);
        String targetName = target != null ? target.getName().getString() : "Unknown";

        party.removeMember(targetId);
        playerToParty.remove(targetId);

        if (target != null) {
            target.sendSystemMessage(Component.literal("§cYou have been kicked from the party!"));
            NetworkHandler.sendToPlayer(new S2CPartyUpdatePacket(null, Collections.emptyList()), target);
        }

        notifyPartyMembers(party, Component.literal("§c" + targetName + " has been kicked from the party."), kicker.getServer());
        syncPartyToMembers(party, kicker.getServer());

        setDirty();
        return true;
    }

    /**
     * Transfer party leadership.
     */
    public boolean transferLeadership(ServerPlayer leader, UUID newLeaderId) {
        Party party = getPlayerParty(leader.getUUID());

        if (party == null) {
            leader.sendSystemMessage(Component.literal("§cYou are not in a party!"));
            return false;
        }

        if (!party.isLeader(leader.getUUID())) {
            leader.sendSystemMessage(Component.literal("§cYou are not the party leader!"));
            return false;
        }

        if (!party.isMember(newLeaderId)) {
            leader.sendSystemMessage(Component.literal("§cThat player is not in your party!"));
            return false;
        }

        ServerPlayer newLeader = leader.getServer().getPlayerList().getPlayer(newLeaderId);
        if (newLeader == null) {
            leader.sendSystemMessage(Component.literal("§cThat player is not online!"));
            return false;
        }

        party.transferLeadership(newLeaderId);

        notifyPartyMembers(party, Component.literal("§6" + newLeader.getName().getString() + " is now the party leader!"), leader.getServer());
        syncPartyToMembers(party, leader.getServer());

        setDirty();
        return true;
    }

    /**
     * Set party loot mode.
     */
    public boolean setLootMode(ServerPlayer leader, Party.LootMode mode) {
        Party party = getPlayerParty(leader.getUUID());

        if (party == null || !party.isLeader(leader.getUUID())) {
            return false;
        }

        party.setLootMode(mode);
        notifyPartyMembers(party, Component.literal("§6Loot mode changed to: " + mode.name()), leader.getServer());
        syncPartyToMembers(party, leader.getServer());

        setDirty();
        return true;
    }

    /**
     * Check if a player is in a party.
     */
    public boolean isInParty(UUID playerId) {
        return playerToParty.containsKey(playerId);
    }

    /**
     * Get a player's party.
     */
    public Party getPlayerParty(UUID playerId) {
        UUID partyId = playerToParty.get(playerId);
        return partyId != null ? parties.get(partyId) : null;
    }

    /**
     * Get all online party members.
     */
    public List<ServerPlayer> getOnlineMembers(Party party, MinecraftServer server) {
        List<ServerPlayer> online = new ArrayList<>();
        for (UUID memberId : party.getMembers()) {
            ServerPlayer player = server.getPlayerList().getPlayer(memberId);
            if (player != null) {
                online.add(player);
            }
        }
        return online;
    }

    /**
     * Notify all party members with a message.
     */
    private void notifyPartyMembers(Party party, Component message, MinecraftServer server) {
        for (ServerPlayer member : getOnlineMembers(party, server)) {
            member.sendSystemMessage(message);
        }
    }

    /**
     * Sync party data to all members.
     */
    private void syncPartyToMembers(Party party, MinecraftServer server) {
        List<String> memberNames = new ArrayList<>();
        for (UUID memberId : party.getMembers()) {
            ServerPlayer player = server.getPlayerList().getPlayer(memberId);
            memberNames.add(player != null ? player.getName().getString() : "Offline");
        }

        for (ServerPlayer member : getOnlineMembers(party, server)) {
            NetworkHandler.sendToPlayer(new S2CPartyUpdatePacket(party, memberNames), member);
        }
    }

    // NBT Serialization
    public static PartyManager load(CompoundTag tag) {
        PartyManager manager = new PartyManager();

        if (tag.contains("Parties")) {
            ListTag partiesTag = tag.getList("Parties", Tag.TAG_COMPOUND);
            for (int i = 0; i < partiesTag.size(); i++) {
                Party party = Party.deserializeNBT(partiesTag.getCompound(i));
                manager.parties.put(party.getPartyId(), party);

                // Rebuild player to party mapping
                for (UUID memberId : party.getMembers()) {
                    manager.playerToParty.put(memberId, party.getPartyId());
                }
            }
        }

        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag partiesTag = new ListTag();
        for (Party party : parties.values()) {
            partiesTag.add(party.serializeNBT());
        }
        tag.put("Parties", partiesTag);

        return tag;
    }
}
