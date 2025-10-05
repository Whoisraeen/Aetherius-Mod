package com.aetheriusmmorpg.network.packet.friend;

import com.aetheriusmmorpg.client.ClientFriendData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Server->Client packet to sync friend list data.
 */
public record S2CFriendListSyncPacket(
    Set<UUID> friends,
    Set<UUID> pendingIncoming,
    Set<UUID> pendingOutgoing,
    Set<UUID> blocked
) {

    public S2CFriendListSyncPacket(FriendlyByteBuf buf) {
        this(
            readUUIDSet(buf),
            readUUIDSet(buf),
            readUUIDSet(buf),
            readUUIDSet(buf)
        );
    }

    public void encode(FriendlyByteBuf buf) {
        writeUUIDSet(buf, friends);
        writeUUIDSet(buf, pendingIncoming);
        writeUUIDSet(buf, pendingOutgoing);
        writeUUIDSet(buf, blocked);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientFriendData.setFriends(friends);
            ClientFriendData.setPendingIncoming(pendingIncoming);
            ClientFriendData.setPendingOutgoing(pendingOutgoing);
            ClientFriendData.setBlocked(blocked);
        });
        ctx.get().setPacketHandled(true);
    }

    private static void writeUUIDSet(FriendlyByteBuf buf, Set<UUID> set) {
        buf.writeInt(set.size());
        for (UUID uuid : set) {
            buf.writeUUID(uuid);
        }
    }

    private static Set<UUID> readUUIDSet(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Set<UUID> set = new HashSet<>();
        for (int i = 0; i < size; i++) {
            set.add(buf.readUUID());
        }
        return set;
    }
}
