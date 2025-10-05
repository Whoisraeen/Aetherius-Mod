package com.aetheriusmmorpg.network.packet.party;

import com.aetheriusmmorpg.client.ui.PartyInviteOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Server -> Client: Notify player of a party invitation.
 */
public class S2CPartyInvitePacket {

    private final UUID inviterUUID;
    private final String inviterName;

    public S2CPartyInvitePacket(UUID inviterUUID, String inviterName) {
        this.inviterUUID = inviterUUID;
        this.inviterName = inviterName;
    }

    public S2CPartyInvitePacket(FriendlyByteBuf buf) {
        this.inviterUUID = buf.readUUID();
        this.inviterName = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(inviterUUID);
        buf.writeUtf(inviterName);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Client-side only
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                PartyInviteOverlay.showInvite(inviterUUID, inviterName);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
