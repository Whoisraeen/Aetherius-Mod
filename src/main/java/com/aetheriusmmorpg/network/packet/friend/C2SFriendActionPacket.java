package com.aetheriusmmorpg.network.packet.friend;

import com.aetheriusmmorpg.common.social.FriendManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Client->Server packet for friend actions.
 */
public record C2SFriendActionPacket(
    FriendAction action,
    UUID targetId
) {

    public enum FriendAction {
        SEND_REQUEST,
        ACCEPT_REQUEST,
        DECLINE_REQUEST,
        REMOVE_FRIEND,
        BLOCK_PLAYER,
        UNBLOCK_PLAYER
    }

    public C2SFriendActionPacket(FriendlyByteBuf buf) {
        this(
            buf.readEnum(FriendAction.class),
            buf.readUUID()
        );
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(action);
        buf.writeUUID(targetId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                FriendManager manager = FriendManager.get(player.getServer());

                switch (action) {
                    case SEND_REQUEST:
                        manager.sendFriendRequest(player, targetId);
                        break;

                    case ACCEPT_REQUEST:
                        manager.acceptFriendRequest(player, targetId);
                        break;

                    case DECLINE_REQUEST:
                        manager.declineFriendRequest(player, targetId);
                        break;

                    case REMOVE_FRIEND:
                        manager.removeFriend(player, targetId);
                        break;

                    case BLOCK_PLAYER:
                        manager.blockPlayer(player, targetId);
                        break;

                    case UNBLOCK_PLAYER:
                        manager.unblockPlayer(player, targetId);
                        break;
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
