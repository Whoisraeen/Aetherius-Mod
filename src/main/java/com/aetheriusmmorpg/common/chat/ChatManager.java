package com.aetheriusmmorpg.common.chat;

import com.aetheriusmmorpg.AetheriusMod;
import com.aetheriusmmorpg.common.guild.Guild;
import com.aetheriusmmorpg.common.guild.GuildManager;
import com.aetheriusmmorpg.common.guild.GuildPermission;
import com.aetheriusmmorpg.common.party.Party;
import com.aetheriusmmorpg.common.party.PartyManager;
import com.aetheriusmmorpg.network.NetworkHandler;
import com.aetheriusmmorpg.network.packet.chat.S2CChatMessagePacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;

/**
 * Server-side chat manager for routing messages through different channels.
 */
public class ChatManager {

    /**
     * Process and route a chat message from a player.
     */
    public static void sendMessage(ServerPlayer sender, ChatChannel channel, String message, UUID recipientId) {
        // Validate message
        if (message == null || message.trim().isEmpty()) {
            return;
        }

        // Profanity filter
        String filteredMessage = filterProfanity(message);

        // Create chat message
        ChatMessage chatMessage = new ChatMessage(
            sender.getUUID(),
            sender.getName().getString(),
            channel,
            filteredMessage,
            recipientId
        );

        // Route based on channel
        switch (channel) {
            case GLOBAL -> broadcastGlobal(sender.getServer(), chatMessage);
            case LOCAL -> broadcastLocal(sender, chatMessage);
            case GUILD -> broadcastGuild(sender, chatMessage);
            case PARTY -> broadcastParty(sender, chatMessage);
            case TRADE -> broadcastTrade(sender.getServer(), chatMessage);
            case PM -> sendPrivateMessage(sender, chatMessage, recipientId);
            case SYSTEM -> {} // System messages are server-only
            case FACTION -> broadcastFaction(sender, chatMessage);
        }
    }

    /**
     * Broadcast to all players on the server.
     */
    private static void broadcastGlobal(MinecraftServer server, ChatMessage message) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        S2CChatMessagePacket packet = new S2CChatMessagePacket(message);

        for (ServerPlayer player : players) {
            NetworkHandler.sendToPlayer(packet, player);
        }

        AetheriusMod.LOGGER.info("[Global] <{}> {}", message.getSenderName(), message.getMessage());
    }

    /**
     * Broadcast to players within range.
     */
    private static void broadcastLocal(ServerPlayer sender, ChatMessage message) {
        int range = ChatChannel.LOCAL.getRange();
        List<ServerPlayer> players = sender.getServer().getPlayerList().getPlayers();
        S2CChatMessagePacket packet = new S2CChatMessagePacket(message);

        for (ServerPlayer player : players) {
            // Check if in same dimension and within range
            if (player.level() == sender.level()) {
                double distance = player.distanceTo(sender);
                if (distance <= range) {
                    NetworkHandler.sendToPlayer(packet, player);
                }
            }
        }
    }

    /**
     * Broadcast to guild members.
     */
    private static void broadcastGuild(ServerPlayer sender, ChatMessage message) {
        GuildManager guildManager = GuildManager.get(sender.getServer());
        Guild guild = guildManager.getPlayerGuild(sender.getUUID());

        if (guild == null) {
            sender.sendSystemMessage(Component.literal("§cYou are not in a guild!"));
            return;
        }

        // Check permission
        if (!guild.hasPermission(sender.getUUID(), GuildPermission.GUILD_CHAT)) {
            sender.sendSystemMessage(Component.literal("§cYou don't have permission to use guild chat!"));
            return;
        }

        S2CChatMessagePacket packet = new S2CChatMessagePacket(message);

        // Send to all online guild members
        for (UUID memberId : guild.getMembers().keySet()) {
            ServerPlayer member = sender.getServer().getPlayerList().getPlayer(memberId);
            if (member != null) {
                NetworkHandler.sendToPlayer(packet, member);
            }
        }

        AetheriusMod.LOGGER.info("[Guild:{}] <{}> {}", guild.getTag(), message.getSenderName(), message.getMessage());
    }

    /**
     * Broadcast to party members.
     */
    private static void broadcastParty(ServerPlayer sender, ChatMessage message) {
        PartyManager partyManager = PartyManager.get(sender.getServer());
        Party party = partyManager.getPlayerParty(sender.getUUID());

        if (party == null) {
            sender.sendSystemMessage(Component.literal("§cYou are not in a party!"));
            return;
        }

        S2CChatMessagePacket packet = new S2CChatMessagePacket(message);

        // Send to all online party members
        for (UUID memberId : party.getMembers()) {
            ServerPlayer member = sender.getServer().getPlayerList().getPlayer(memberId);
            if (member != null) {
                NetworkHandler.sendToPlayer(packet, member);
            }
        }
    }

    /**
     * Broadcast to trade channel (all players).
     */
    private static void broadcastTrade(MinecraftServer server, ChatMessage message) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        S2CChatMessagePacket packet = new S2CChatMessagePacket(message);

        for (ServerPlayer player : players) {
            NetworkHandler.sendToPlayer(packet, player);
        }
    }

    /**
     * Send a private message to a specific player.
     */
    private static void sendPrivateMessage(ServerPlayer sender, ChatMessage message, UUID recipientId) {
        if (recipientId == null) {
            sender.sendSystemMessage(Component.literal("§cNo recipient specified for private message!"));
            return;
        }

        ServerPlayer recipient = sender.getServer().getPlayerList().getPlayer(recipientId);
        if (recipient == null) {
            sender.sendSystemMessage(Component.literal("§cThat player is not online!"));
            return;
        }

        S2CChatMessagePacket packet = new S2CChatMessagePacket(message);

        // Send to recipient
        NetworkHandler.sendToPlayer(packet, recipient);

        // Send to sender as confirmation
        NetworkHandler.sendToPlayer(packet, sender);

        AetheriusMod.LOGGER.info("[PM] {} -> {}: {}", message.getSenderName(), recipient.getName().getString(), message.getMessage());
    }

    /**
     * Broadcast to faction/alliance members.
     */
    private static void broadcastFaction(ServerPlayer sender, ChatMessage message) {
        // TODO: Implement faction/alliance system
        sender.sendSystemMessage(Component.literal("§cFaction chat is not yet implemented!"));
    }

    /**
     * Simple profanity filter (basic implementation).
     */
    private static String filterProfanity(String message) {
        // TODO: Implement comprehensive profanity filter
        // For now, just basic filtering
        String[] badWords = {"badword1", "badword2"}; // Replace with actual list
        String filtered = message;

        for (String word : badWords) {
            if (filtered.toLowerCase().contains(word.toLowerCase())) {
                filtered = filtered.replaceAll("(?i)" + word, "*".repeat(word.length()));
            }
        }

        return filtered;
    }

    /**
     * Broadcast a system message to all players.
     */
    public static void broadcastSystemMessage(MinecraftServer server, String message) {
        ChatMessage chatMessage = new ChatMessage(
            UUID.randomUUID(),
            "System",
            ChatChannel.SYSTEM,
            message
        );

        S2CChatMessagePacket packet = new S2CChatMessagePacket(chatMessage);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            NetworkHandler.sendToPlayer(packet, player);
        }

        AetheriusMod.LOGGER.info("[System] {}", message);
    }

    /**
     * Broadcast a world event message to all players.
     */
    public static void broadcastWorldEvent(MinecraftServer server, String message) {
        ChatMessage chatMessage = new ChatMessage(
            UUID.randomUUID(),
            "Event",
            ChatChannel.WORLD_EVENT,
            message
        );

        S2CChatMessagePacket packet = new S2CChatMessagePacket(chatMessage);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            NetworkHandler.sendToPlayer(packet, player);
        }

        AetheriusMod.LOGGER.info("[World Event] {}", message);
    }
}
