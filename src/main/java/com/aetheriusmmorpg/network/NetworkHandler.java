package com.aetheriusmmorpg.network;

import com.aetheriusmmorpg.AetheriusMod;
import com.aetheriusmmorpg.network.packet.C2SCreateCharacterPacket;
import com.aetheriusmmorpg.network.packet.C2SOpenCharacterSheetPacket;
import com.aetheriusmmorpg.network.packet.C2SUseSkillPacket;
import com.aetheriusmmorpg.network.packet.S2CHandshakePacket;
import com.aetheriusmmorpg.network.packet.S2COpenCharacterCreationPacket;
import com.aetheriusmmorpg.network.packet.S2COpenIntroVideoPacket;
import com.aetheriusmmorpg.network.packet.S2CStatSyncPacket;
import com.aetheriusmmorpg.network.packet.S2CCooldownPacket;
import com.aetheriusmmorpg.network.packet.S2CSkillBarPacket;
import com.aetheriusmmorpg.network.packet.party.C2SPartyActionPacket;
import com.aetheriusmmorpg.network.packet.party.S2CPartyInvitePacket;
import com.aetheriusmmorpg.network.packet.party.S2CPartyUpdatePacket;
import com.aetheriusmmorpg.network.packet.friend.C2SFriendActionPacket;
import com.aetheriusmmorpg.network.packet.friend.S2CFriendListSyncPacket;
import com.aetheriusmmorpg.network.packet.guild.C2SGuildActionPacket;
import com.aetheriusmmorpg.network.packet.guild.S2CGuildDataPacket;
import com.aetheriusmmorpg.network.packet.chat.C2SChatMessagePacket;
import com.aetheriusmmorpg.network.packet.chat.S2CChatMessagePacket;
import com.aetheriusmmorpg.network.packet.quest.C2SQuestActionPacket;
import com.aetheriusmmorpg.network.packet.quest.S2CQuestUpdatePacket;
import com.aetheriusmmorpg.network.packet.quest.S2CQuestCompletePacket;
import com.aetheriusmmorpg.network.packet.quest.S2CQuestAbandonPacket;
import com.aetheriusmmorpg.network.packet.trade.C2STradeRequestPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Centralized networking handler for all Aetherius packets.
 * Uses Forge's SimpleChannel with versioned protocol.
 * All client actions must be validated server-side.
 */
public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(AetheriusMod.MOD_ID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    /**
     * Registers all packets with the network channel.
     * Called during common setup.
     */
    public static void register() {
        AetheriusMod.LOGGER.info("Registering network packets...");

        // Server -> Client packets
        INSTANCE.messageBuilder(S2CHandshakePacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(S2CHandshakePacket::new)
            .encoder(S2CHandshakePacket::toBytes)
            .consumerMainThread(S2CHandshakePacket::handle)
            .add();

        INSTANCE.messageBuilder(S2CStatSyncPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(S2CStatSyncPacket::new)
            .encoder(S2CStatSyncPacket::toBytes)
            .consumerMainThread(S2CStatSyncPacket::handle)
            .add();

        INSTANCE.messageBuilder(S2CCooldownPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(S2CCooldownPacket::new)
            .encoder(S2CCooldownPacket::toBytes)
            .consumerMainThread(S2CCooldownPacket::handle)
            .add();

        INSTANCE.messageBuilder(S2CSkillBarPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(S2CSkillBarPacket::new)
            .encoder(S2CSkillBarPacket::toBytes)
            .consumerMainThread(S2CSkillBarPacket::handle)
            .add();

        INSTANCE.messageBuilder(S2COpenCharacterCreationPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(S2COpenCharacterCreationPacket::new)
            .encoder(S2COpenCharacterCreationPacket::encode)
            .consumerMainThread(S2COpenCharacterCreationPacket::handle)
            .add();

        INSTANCE.messageBuilder(S2COpenIntroVideoPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(S2COpenIntroVideoPacket::new)
            .encoder(S2COpenIntroVideoPacket::encode)
            .consumerMainThread(S2COpenIntroVideoPacket::handle)
            .add();

        // Client -> Server packets
        INSTANCE.messageBuilder(C2SOpenCharacterSheetPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(C2SOpenCharacterSheetPacket::new)
            .encoder(C2SOpenCharacterSheetPacket::toBytes)
            .consumerMainThread(C2SOpenCharacterSheetPacket::handle)
            .add();

        INSTANCE.messageBuilder(C2SUseSkillPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(C2SUseSkillPacket::new)
            .encoder(C2SUseSkillPacket::toBytes)
            .consumerMainThread(C2SUseSkillPacket::handle)
            .add();

        INSTANCE.messageBuilder(C2SCreateCharacterPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(C2SCreateCharacterPacket::new)
            .encoder(C2SCreateCharacterPacket::encode)
            .consumerMainThread(C2SCreateCharacterPacket::handle)
            .add();

        // Party System packets
        INSTANCE.messageBuilder(S2CPartyInvitePacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(S2CPartyInvitePacket::new)
            .encoder(S2CPartyInvitePacket::encode)
            .consumerMainThread(S2CPartyInvitePacket::handle)
            .add();

        INSTANCE.messageBuilder(S2CPartyUpdatePacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(S2CPartyUpdatePacket::new)
            .encoder(S2CPartyUpdatePacket::encode)
            .consumerMainThread(S2CPartyUpdatePacket::handle)
            .add();

        INSTANCE.messageBuilder(C2SPartyActionPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(C2SPartyActionPacket::new)
            .encoder(C2SPartyActionPacket::encode)
            .consumerMainThread(C2SPartyActionPacket::handle)
            .add();

        // Friend System packets
        INSTANCE.messageBuilder(S2CFriendListSyncPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(S2CFriendListSyncPacket::new)
            .encoder(S2CFriendListSyncPacket::encode)
            .consumerMainThread(S2CFriendListSyncPacket::handle)
            .add();

        INSTANCE.messageBuilder(C2SFriendActionPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(C2SFriendActionPacket::new)
            .encoder(C2SFriendActionPacket::encode)
            .consumerMainThread(C2SFriendActionPacket::handle)
            .add();

        // Guild System packets
        INSTANCE.messageBuilder(S2CGuildDataPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(S2CGuildDataPacket::new)
            .encoder(S2CGuildDataPacket::encode)
            .consumerMainThread(S2CGuildDataPacket::handle)
            .add();

        INSTANCE.messageBuilder(C2SGuildActionPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(C2SGuildActionPacket::new)
            .encoder(C2SGuildActionPacket::encode)
            .consumerMainThread(C2SGuildActionPacket::handle)
            .add();

        // Chat System packets
        INSTANCE.messageBuilder(S2CChatMessagePacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(S2CChatMessagePacket::new)
            .encoder(S2CChatMessagePacket::encode)
            .consumerMainThread(S2CChatMessagePacket::handle)
            .add();

        INSTANCE.messageBuilder(C2SChatMessagePacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(C2SChatMessagePacket::new)
            .encoder(C2SChatMessagePacket::encode)
            .consumerMainThread(C2SChatMessagePacket::handle)
            .add();

        // Quest System packets
        INSTANCE.messageBuilder(C2SQuestActionPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(C2SQuestActionPacket::new)
            .encoder(C2SQuestActionPacket::encode)
            .consumerMainThread(C2SQuestActionPacket::handle)
            .add();

        INSTANCE.messageBuilder(S2CQuestUpdatePacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(S2CQuestUpdatePacket::new)
            .encoder(S2CQuestUpdatePacket::encode)
            .consumerMainThread(S2CQuestUpdatePacket::handle)
            .add();

        INSTANCE.messageBuilder(S2CQuestCompletePacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(S2CQuestCompletePacket::new)
            .encoder(S2CQuestCompletePacket::encode)
            .consumerMainThread(S2CQuestCompletePacket::handle)
            .add();

        INSTANCE.messageBuilder(S2CQuestAbandonPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(S2CQuestAbandonPacket::new)
            .encoder(S2CQuestAbandonPacket::encode)
            .consumerMainThread(S2CQuestAbandonPacket::handle)
            .add();

        // Trade System packets
        INSTANCE.messageBuilder(C2STradeRequestPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(C2STradeRequestPacket::new)
            .encoder(C2STradeRequestPacket::encode)
            .consumerMainThread(C2STradeRequestPacket::handle)
            .add();

        // Friend System packets
        INSTANCE.messageBuilder(C2SFriendActionPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(C2SFriendActionPacket::new)
            .encoder(C2SFriendActionPacket::encode)
            .consumerMainThread(C2SFriendActionPacket::handle)
            .add();

        AetheriusMod.LOGGER.info("Registered {} network packets", packetId);
    }

    /**
     * Sends a packet to a specific player.
     */
    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    /**
     * Sends a packet to all players.
     */
    public static <MSG> void sendToAllPlayers(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }

    /**
     * Sends a packet to the server.
     */
    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }
}
