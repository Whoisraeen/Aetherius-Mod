package com.aetheriusmmorpg.network.packet.party;

import com.aetheriusmmorpg.client.ClientPartyData;
import com.aetheriusmmorpg.common.party.Party;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Server -> Client: Update party data.
 */
public class S2CPartyUpdatePacket {

    private final boolean hasParty;
    private final UUID partyId;
    private final UUID leaderId;
    private final List<UUID> memberIds;
    private final List<String> memberNames;
    private final int lootModeOrdinal;
    private final boolean shareExperience;

    public S2CPartyUpdatePacket(Party party, List<String> memberNames) {
        if (party != null) {
            this.hasParty = true;
            this.partyId = party.getPartyId();
            this.leaderId = party.getLeaderId();
            this.memberIds = party.getMembers();
            this.memberNames = memberNames;
            this.lootModeOrdinal = party.getLootMode().ordinal();
            this.shareExperience = party.isShareExperience();
        } else {
            this.hasParty = false;
            this.partyId = null;
            this.leaderId = null;
            this.memberIds = new ArrayList<>();
            this.memberNames = new ArrayList<>();
            this.lootModeOrdinal = 0;
            this.shareExperience = false;
        }
    }

    public S2CPartyUpdatePacket(FriendlyByteBuf buf) {
        this.hasParty = buf.readBoolean();

        if (hasParty) {
            this.partyId = buf.readUUID();
            this.leaderId = buf.readUUID();

            int memberCount = buf.readInt();
            this.memberIds = new ArrayList<>();
            this.memberNames = new ArrayList<>();

            for (int i = 0; i < memberCount; i++) {
                memberIds.add(buf.readUUID());
                memberNames.add(buf.readUtf());
            }

            this.lootModeOrdinal = buf.readInt();
            this.shareExperience = buf.readBoolean();
        } else {
            this.partyId = null;
            this.leaderId = null;
            this.memberIds = new ArrayList<>();
            this.memberNames = new ArrayList<>();
            this.lootModeOrdinal = 0;
            this.shareExperience = false;
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(hasParty);

        if (hasParty) {
            buf.writeUUID(partyId);
            buf.writeUUID(leaderId);
            buf.writeInt(memberIds.size());

            for (int i = 0; i < memberIds.size(); i++) {
                buf.writeUUID(memberIds.get(i));
                buf.writeUtf(memberNames.get(i));
            }

            buf.writeInt(lootModeOrdinal);
            buf.writeBoolean(shareExperience);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (hasParty) {
                ClientPartyData.updateParty(partyId, leaderId, memberIds, memberNames, lootModeOrdinal, shareExperience);
            } else {
                ClientPartyData.clearParty();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
