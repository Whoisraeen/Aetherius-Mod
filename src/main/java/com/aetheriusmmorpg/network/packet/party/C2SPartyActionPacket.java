package com.aetheriusmmorpg.network.packet.party;

import com.aetheriusmmorpg.common.party.Party;
import com.aetheriusmmorpg.common.party.PartyManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Client -> Server: Party action request.
 */
public class C2SPartyActionPacket {

    private final PartyAction action;
    private final UUID targetPlayerId; // For invite, kick, transfer
    private final String targetPlayerName; // For invite by name
    private final int lootModeOrdinal; // For loot mode change

    public enum PartyAction {
        CREATE,
        INVITE,
        ACCEPT,
        DECLINE,
        LEAVE,
        KICK,
        TRANSFER_LEADERSHIP,
        SET_LOOT_MODE
    }

    // Constructor for actions without target
    public C2SPartyActionPacket(PartyAction action) {
        this.action = action;
        this.targetPlayerId = null;
        this.targetPlayerName = null;
        this.lootModeOrdinal = 0;
    }

    // Constructor for actions with UUID target
    public C2SPartyActionPacket(PartyAction action, UUID targetPlayerId) {
        this.action = action;
        this.targetPlayerId = targetPlayerId;
        this.targetPlayerName = null;
        this.lootModeOrdinal = 0;
    }

    // Constructor for invite by name
    public C2SPartyActionPacket(PartyAction action, String targetPlayerName) {
        this.action = action;
        this.targetPlayerId = null;
        this.targetPlayerName = targetPlayerName;
        this.lootModeOrdinal = 0;
    }

    // Constructor for loot mode change
    public C2SPartyActionPacket(PartyAction action, int lootModeOrdinal) {
        this.action = action;
        this.targetPlayerId = null;
        this.targetPlayerName = null;
        this.lootModeOrdinal = lootModeOrdinal;
    }

    public C2SPartyActionPacket(FriendlyByteBuf buf) {
        this.action = buf.readEnum(PartyAction.class);
        this.targetPlayerId = buf.readBoolean() ? buf.readUUID() : null;
        this.targetPlayerName = buf.readBoolean() ? buf.readUtf() : null;
        this.lootModeOrdinal = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(action);
        buf.writeBoolean(targetPlayerId != null);
        if (targetPlayerId != null) {
            buf.writeUUID(targetPlayerId);
        }
        buf.writeBoolean(targetPlayerName != null);
        if (targetPlayerName != null) {
            buf.writeUtf(targetPlayerName);
        }
        buf.writeInt(lootModeOrdinal);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            PartyManager manager = PartyManager.get(player.getServer());

            switch (action) {
                case CREATE:
                    manager.createParty(player);
                    break;

                case INVITE:
                    if (targetPlayerName != null) {
                        ServerPlayer target = player.getServer().getPlayerList().getPlayerByName(targetPlayerName);
                        if (target != null) {
                            manager.invitePlayer(player, target);
                        } else {
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cPlayer not found!"));
                        }
                    } else if (targetPlayerId != null) {
                        ServerPlayer target = player.getServer().getPlayerList().getPlayer(targetPlayerId);
                        if (target != null) {
                            manager.invitePlayer(player, target);
                        }
                    }
                    break;

                case ACCEPT:
                    manager.acceptInvite(player);
                    break;

                case DECLINE:
                    manager.declineInvite(player);
                    break;

                case LEAVE:
                    manager.leaveParty(player);
                    break;

                case KICK:
                    if (targetPlayerId != null) {
                        manager.kickPlayer(player, targetPlayerId);
                    }
                    break;

                case TRANSFER_LEADERSHIP:
                    if (targetPlayerId != null) {
                        manager.transferLeadership(player, targetPlayerId);
                    }
                    break;

                case SET_LOOT_MODE:
                    Party.LootMode[] modes = Party.LootMode.values();
                    if (lootModeOrdinal >= 0 && lootModeOrdinal < modes.length) {
                        manager.setLootMode(player, modes[lootModeOrdinal]);
                    }
                    break;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
