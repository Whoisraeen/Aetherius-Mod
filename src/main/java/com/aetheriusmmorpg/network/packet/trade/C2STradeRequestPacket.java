package com.aetheriusmmorpg.network.packet.trade;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Client->Server packet for trade requests.
 */
public record C2STradeRequestPacket(
    TradeAction action,
    UUID targetPlayer
) {

    public C2STradeRequestPacket(FriendlyByteBuf buf) {
        this(
            buf.readEnum(TradeAction.class),
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
            if (target == null) {
                player.sendSystemMessage(Component.literal("§cPlayer not found!"));
                return;
            }

            switch (action) {
                case REQUEST:
                    target.sendSystemMessage(Component.literal("§e" + player.getName().getString() + " wants to trade with you!"));
                    target.sendSystemMessage(Component.literal("§7Right-click them to accept"));
                    player.sendSystemMessage(Component.literal("§aTrade request sent to " + target.getName().getString()));
                    break;
                case ACCEPT:
                    // TODO: Open trade window for both players
                    player.sendSystemMessage(Component.literal("§aTrade accepted!"));
                    target.sendSystemMessage(Component.literal("§a" + player.getName().getString() + " accepted your trade!"));
                    break;
                case DECLINE:
                    target.sendSystemMessage(Component.literal("§c" + player.getName().getString() + " declined your trade request."));
                    player.sendSystemMessage(Component.literal("§7Trade declined."));
                    break;
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public enum TradeAction {
        REQUEST,
        ACCEPT,
        DECLINE
    }

    public static C2STradeRequestPacket request(UUID targetPlayer) {
        return new C2STradeRequestPacket(TradeAction.REQUEST, targetPlayer);
    }

    public static C2STradeRequestPacket accept(UUID targetPlayer) {
        return new C2STradeRequestPacket(TradeAction.ACCEPT, targetPlayer);
    }

    public static C2STradeRequestPacket decline(UUID targetPlayer) {
        return new C2STradeRequestPacket(TradeAction.DECLINE, targetPlayer);
    }
}

