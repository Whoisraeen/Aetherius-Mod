package com.aetheriusmmorpg.network.packet;

import com.aetheriusmmorpg.AetheriusMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server-to-Client handshake packet.
 * Sent when a player joins to confirm protocol version and server state.
 */
public class S2CHandshakePacket {
    private final String serverVersion;
    private final long serverTime;

    public S2CHandshakePacket(String serverVersion, long serverTime) {
        this.serverVersion = serverVersion;
        this.serverTime = serverTime;
    }

    public S2CHandshakePacket(FriendlyByteBuf buf) {
        this.serverVersion = buf.readUtf();
        this.serverTime = buf.readLong();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(serverVersion);
        buf.writeLong(serverTime);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            // Client-side handling
            AetheriusMod.LOGGER.info("Received handshake from server: version={}, time={}",
                serverVersion, serverTime);
        });
        return true;
    }
}
