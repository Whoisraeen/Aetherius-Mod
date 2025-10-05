package com.aetheriusmmorpg.client;

import java.util.*;

/**
 * Client-side storage for friend list data.
 * Synced from server via S2CFriendListSyncPacket.
 */
public class ClientFriendData {

    private static final Set<UUID> friends = new HashSet<>();
    private static final Set<UUID> pendingIncoming = new HashSet<>();
    private static final Set<UUID> pendingOutgoing = new HashSet<>();
    private static final Set<UUID> blocked = new HashSet<>();

    // Friends
    public static Set<UUID> getFriends() {
        return new HashSet<>(friends);
    }

    public static void setFriends(Set<UUID> newFriends) {
        friends.clear();
        friends.addAll(newFriends);
    }

    public static boolean isFriend(UUID playerId) {
        return friends.contains(playerId);
    }

    public static int getFriendCount() {
        return friends.size();
    }

    // Pending Incoming Requests
    public static Set<UUID> getPendingIncoming() {
        return new HashSet<>(pendingIncoming);
    }

    public static void setPendingIncoming(Set<UUID> incoming) {
        pendingIncoming.clear();
        pendingIncoming.addAll(incoming);
    }

    public static boolean hasPendingIncoming(UUID playerId) {
        return pendingIncoming.contains(playerId);
    }

    // Pending Outgoing Requests
    public static Set<UUID> getPendingOutgoing() {
        return new HashSet<>(pendingOutgoing);
    }

    public static void setPendingOutgoing(Set<UUID> outgoing) {
        pendingOutgoing.clear();
        pendingOutgoing.addAll(outgoing);
    }

    public static boolean hasPendingOutgoing(UUID playerId) {
        return pendingOutgoing.contains(playerId);
    }

    // Blocked
    public static Set<UUID> getBlocked() {
        return new HashSet<>(blocked);
    }

    public static void setBlocked(Set<UUID> newBlocked) {
        blocked.clear();
        blocked.addAll(newBlocked);
    }

    public static boolean isBlocked(UUID playerId) {
        return blocked.contains(playerId);
    }

    // Clear all data
    public static void clear() {
        friends.clear();
        pendingIncoming.clear();
        pendingOutgoing.clear();
        blocked.clear();
    }
}
