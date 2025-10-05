package com.aetheriusmmorpg.common.social;

import com.aetheriusmmorpg.AetheriusMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side manager for friend lists.
 * Handles friend requests, acceptance, and synchronization.
 */
public class FriendManager extends SavedData {

    private static final String DATA_NAME = "aetherius_friends";
    private final Map<UUID, FriendList> friendLists = new HashMap<>();

    public FriendManager() {
        super();
    }

    /**
     * Get or create a friend list for a player.
     */
    public FriendList getFriendList(UUID playerId) {
        return friendLists.computeIfAbsent(playerId, FriendList::new);
    }

    /**
     * Send a friend request from one player to another.
     */
    public void sendFriendRequest(ServerPlayer requester, UUID targetId) {
        FriendList requesterList = getFriendList(requester.getUUID());
        FriendList targetList = getFriendList(targetId);

        // Check if target has blocked the requester
        if (targetList.isBlocked(requester.getUUID())) {
            requester.sendSystemMessage(Component.literal("§cCannot send friend request to this player."));
            return;
        }

        // Check if already friends
        if (requesterList.isFriend(targetId)) {
            requester.sendSystemMessage(Component.literal("§cYou are already friends with this player!"));
            return;
        }

        // Send request
        if (requesterList.sendFriendRequest(targetId)) {
            targetList.addIncomingRequest(requester.getUUID());
            setDirty();

            requester.sendSystemMessage(Component.literal("§aFriend request sent!"));

            // Notify target if online
            ServerPlayer target = requester.getServer().getPlayerList().getPlayer(targetId);
            if (target != null) {
                target.sendSystemMessage(Component.literal("§b[Friend] §f" + requester.getName().getString() + " sent you a friend request!"));
                target.sendSystemMessage(Component.literal("§7Open the Social menu (F) to accept or decline"));
            }
        } else {
            requester.sendSystemMessage(Component.literal("§cCould not send friend request."));
        }
    }

    /**
     * Accept a friend request.
     */
    public void acceptFriendRequest(ServerPlayer accepter, UUID requesterId) {
        FriendList accepterList = getFriendList(accepter.getUUID());
        FriendList requesterList = getFriendList(requesterId);

        if (accepterList.acceptFriendRequest(requesterId)) {
            // Add to requester's friend list
            requesterList.getFriends().add(accepter.getUUID());
            requesterList.removeOutgoingRequest(accepter.getUUID());
            setDirty();

            accepter.sendSystemMessage(Component.literal("§aYou are now friends!"));

            // Notify requester if online
            ServerPlayer requester = accepter.getServer().getPlayerList().getPlayer(requesterId);
            if (requester != null) {
                requester.sendSystemMessage(Component.literal("§b[Friend] §f" + accepter.getName().getString() + " accepted your friend request!"));
            }
        } else {
            accepter.sendSystemMessage(Component.literal("§cCould not accept friend request."));
        }
    }

    /**
     * Decline a friend request.
     */
    public void declineFriendRequest(ServerPlayer decliner, UUID requesterId) {
        FriendList declinerList = getFriendList(decliner.getUUID());
        FriendList requesterList = getFriendList(requesterId);

        if (declinerList.declineFriendRequest(requesterId)) {
            requesterList.removeOutgoingRequest(decliner.getUUID());
            setDirty();

            decliner.sendSystemMessage(Component.literal("§cFriend request declined."));
        }
    }

    /**
     * Remove a friend.
     */
    public void removeFriend(ServerPlayer player, UUID friendId) {
        FriendList playerList = getFriendList(player.getUUID());
        FriendList friendList = getFriendList(friendId);

        if (playerList.removeFriend(friendId)) {
            friendList.removeFriend(player.getUUID());
            setDirty();

            player.sendSystemMessage(Component.literal("§7Friend removed."));

            // Notify friend if online
            ServerPlayer friend = player.getServer().getPlayerList().getPlayer(friendId);
            if (friend != null) {
                friend.sendSystemMessage(Component.literal("§b[Friend] §f" + player.getName().getString() + " removed you from their friend list."));
            }
        }
    }

    /**
     * Block a player.
     */
    public void blockPlayer(ServerPlayer blocker, UUID targetId) {
        FriendList blockerList = getFriendList(blocker.getUUID());

        if (blockerList.blockPlayer(targetId)) {
            setDirty();
            blocker.sendSystemMessage(Component.literal("§7Player blocked."));
        }
    }

    /**
     * Unblock a player.
     */
    public void unblockPlayer(ServerPlayer unblocker, UUID targetId) {
        FriendList unblockerList = getFriendList(unblocker.getUUID());

        if (unblockerList.unblockPlayer(targetId)) {
            setDirty();
            unblocker.sendSystemMessage(Component.literal("§7Player unblocked."));
        }
    }

    // NBT Serialization
    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag listsTag = new ListTag();
        for (FriendList list : friendLists.values()) {
            listsTag.add(list.serializeNBT());
        }
        tag.put("FriendLists", listsTag);
        return tag;
    }

    public void load(CompoundTag tag) {
        friendLists.clear();
        if (tag.contains("FriendLists")) {
            ListTag listsTag = tag.getList("FriendLists", 10); // 10 = CompoundTag type
            for (int i = 0; i < listsTag.size(); i++) {
                FriendList list = FriendList.deserializeNBT(listsTag.getCompound(i));
                friendLists.put(list.getOwnerId(), list);
            }
        }
    }

    // Static access
    public static FriendManager get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
            tag -> {
                FriendManager manager = new FriendManager();
                manager.load(tag);
                return manager;
            },
            FriendManager::new,
            DATA_NAME
        );
    }
}
