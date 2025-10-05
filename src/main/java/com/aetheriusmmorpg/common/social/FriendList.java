package com.aetheriusmmorpg.common.social;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.*;

/**
 * Represents a player's friend list.
 * Stores friends and pending friend requests.
 */
public class FriendList {

    private final UUID ownerId;
    private final Set<UUID> friends = new HashSet<>();
    private final Set<UUID> pendingIncoming = new HashSet<>();  // Incoming friend requests
    private final Set<UUID> pendingOutgoing = new HashSet<>();  // Outgoing friend requests
    private final Set<UUID> blocked = new HashSet<>();

    private static final int MAX_FRIENDS = 100; // PWI-style limit

    public FriendList(UUID ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * Send a friend request to another player.
     */
    public boolean sendFriendRequest(UUID targetId) {
        if (friends.contains(targetId)) {
            return false; // Already friends
        }
        if (pendingOutgoing.contains(targetId)) {
            return false; // Request already sent
        }
        if (blocked.contains(targetId)) {
            return false; // Target is blocked
        }
        if (friends.size() >= MAX_FRIENDS) {
            return false; // Friend list full
        }

        pendingOutgoing.add(targetId);
        return true;
    }

    /**
     * Accept an incoming friend request.
     */
    public boolean acceptFriendRequest(UUID requesterId) {
        if (!pendingIncoming.contains(requesterId)) {
            return false; // No request from this player
        }
        if (friends.size() >= MAX_FRIENDS) {
            return false; // Friend list full
        }

        pendingIncoming.remove(requesterId);
        friends.add(requesterId);
        return true;
    }

    /**
     * Decline an incoming friend request.
     */
    public boolean declineFriendRequest(UUID requesterId) {
        return pendingIncoming.remove(requesterId);
    }

    /**
     * Remove a friend.
     */
    public boolean removeFriend(UUID friendId) {
        return friends.remove(friendId);
    }

    /**
     * Block a player.
     */
    public boolean blockPlayer(UUID playerId) {
        // Remove from friends if already friends
        friends.remove(playerId);
        // Remove any pending requests
        pendingIncoming.remove(playerId);
        pendingOutgoing.remove(playerId);
        // Add to blocked list
        return blocked.add(playerId);
    }

    /**
     * Unblock a player.
     */
    public boolean unblockPlayer(UUID playerId) {
        return blocked.remove(playerId);
    }

    /**
     * Check if a player is a friend.
     */
    public boolean isFriend(UUID playerId) {
        return friends.contains(playerId);
    }

    /**
     * Check if a player is blocked.
     */
    public boolean isBlocked(UUID playerId) {
        return blocked.contains(playerId);
    }

    /**
     * Add an incoming friend request (called by manager).
     */
    public void addIncomingRequest(UUID requesterId) {
        if (!blocked.contains(requesterId)) {
            pendingIncoming.add(requesterId);
        }
    }

    /**
     * Remove an outgoing request (called when accepted/declined).
     */
    public void removeOutgoingRequest(UUID targetId) {
        pendingOutgoing.remove(targetId);
    }

    // Getters
    public UUID getOwnerId() {
        return ownerId;
    }

    public Set<UUID> getFriends() {
        return new HashSet<>(friends);
    }

    public Set<UUID> getPendingIncoming() {
        return new HashSet<>(pendingIncoming);
    }

    public Set<UUID> getPendingOutgoing() {
        return new HashSet<>(pendingOutgoing);
    }

    public Set<UUID> getBlocked() {
        return new HashSet<>(blocked);
    }

    public int getFriendCount() {
        return friends.size();
    }

    public int getMaxFriends() {
        return MAX_FRIENDS;
    }

    // NBT Serialization
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putString("Owner", ownerId.toString());

        // Friends
        ListTag friendsTag = new ListTag();
        for (UUID friendId : friends) {
            friendsTag.add(StringTag.valueOf(friendId.toString()));
        }
        tag.put("Friends", friendsTag);

        // Pending incoming
        ListTag incomingTag = new ListTag();
        for (UUID id : pendingIncoming) {
            incomingTag.add(StringTag.valueOf(id.toString()));
        }
        tag.put("PendingIncoming", incomingTag);

        // Pending outgoing
        ListTag outgoingTag = new ListTag();
        for (UUID id : pendingOutgoing) {
            outgoingTag.add(StringTag.valueOf(id.toString()));
        }
        tag.put("PendingOutgoing", outgoingTag);

        // Blocked
        ListTag blockedTag = new ListTag();
        for (UUID id : blocked) {
            blockedTag.add(StringTag.valueOf(id.toString()));
        }
        tag.put("Blocked", blockedTag);

        return tag;
    }

    public static FriendList deserializeNBT(CompoundTag tag) {
        UUID ownerId = UUID.fromString(tag.getString("Owner"));
        FriendList list = new FriendList(ownerId);

        // Friends
        if (tag.contains("Friends")) {
            ListTag friendsTag = tag.getList("Friends", 8); // 8 = String type
            for (int i = 0; i < friendsTag.size(); i++) {
                list.friends.add(UUID.fromString(friendsTag.getString(i)));
            }
        }

        // Pending incoming
        if (tag.contains("PendingIncoming")) {
            ListTag incomingTag = tag.getList("PendingIncoming", 8);
            for (int i = 0; i < incomingTag.size(); i++) {
                list.pendingIncoming.add(UUID.fromString(incomingTag.getString(i)));
            }
        }

        // Pending outgoing
        if (tag.contains("PendingOutgoing")) {
            ListTag outgoingTag = tag.getList("PendingOutgoing", 8);
            for (int i = 0; i < outgoingTag.size(); i++) {
                list.pendingOutgoing.add(UUID.fromString(outgoingTag.getString(i)));
            }
        }

        // Blocked
        if (tag.contains("Blocked")) {
            ListTag blockedTag = tag.getList("Blocked", 8);
            for (int i = 0; i < blockedTag.size(); i++) {
                list.blocked.add(UUID.fromString(blockedTag.getString(i)));
            }
        }

        return list;
    }
}
