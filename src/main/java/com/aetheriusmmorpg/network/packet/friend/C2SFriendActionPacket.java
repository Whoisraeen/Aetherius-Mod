package com.aetheriusmmorpg.network.packet.friend;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Client->Server packet for friend actions.
 */
public record C2SFriendActionPacket(
    FriendAction action,
    UUID targetPlayer
) {

    public C2SFriendActionPacket(FriendlyByteBuf buf) {
        this(
            buf.readEnum(FriendAction.class),
            buf.readUUID()
        );
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(action);
        buf.writeUUID(targetPlayer);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ServerPlayer target = player.getServer().getPlayerList().getPlayer(targetPlayer);

            switch (action) {
                case SEND_REQUEST:
                    if (target != null) {
                        target.sendSystemMessage(Component.literal("§e" + player.getName().getString() + " sent you a friend request!"));
                        target.sendSystemMessage(Component.literal("§7Type /friend accept " + player.getName().getString() + " to accept"));
                        player.sendSystemMessage(Component.literal("§aFriend request sent!"));
                    } else {
                        player.sendSystemMessage(Component.literal("§cPlayer not found!"));
                    }
                    break;
                case ACCEPT_REQUEST:
                    if (target != null) {
                        // TODO: Add to friend manager
                        player.sendSystemMessage(Component.literal("§aYou are now friends with " + target.getName().getString()));
                        target.sendSystemMessage(Component.literal("§a" + player.getName().getString() + " accepted your friend request!"));
                    }
                    break;
                case REMOVE_FRIEND:
                    // TODO: Remove from friend manager
                    player.sendSystemMessage(Component.literal("§7Friend removed."));
                    break;
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public enum FriendAction {
        SEND_REQUEST,
        ACCEPT_REQUEST,
        REMOVE_FRIEND
    }
}
