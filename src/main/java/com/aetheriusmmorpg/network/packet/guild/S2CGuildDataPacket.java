package com.aetheriusmmorpg.network.packet.guild;

import com.aetheriusmmorpg.client.ClientGuildData;
import com.aetheriusmmorpg.common.guild.Guild;
import com.aetheriusmmorpg.common.guild.GuildMember;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.*;
import java.util.function.Supplier;

/**
 * Server->Client packet to sync guild data.
 */
public record S2CGuildDataPacket(
    boolean hasGuild,
    UUID guildId,
    String guildName,
    String guildTag,
    UUID leaderId,
    int level,
    long experience,
    long expForNextLevel,
    String announcement,
    int guildGold,
    int maxMembers,
    Map<UUID, MemberData> members
) {

    public S2CGuildDataPacket(Guild guild) {
        this(
            true,
            guild.getGuildId(),
            guild.getName(),
            guild.getTag(),
            guild.getLeaderId(),
            guild.getLevel(),
            guild.getExperience(),
            guild.getRequiredExperienceForNextLevel(),
            guild.getAnnouncement(),
            guild.getGuildGold(),
            guild.getMaxMembers(),
            convertMembers(guild.getMembers())
        );
    }

    // Empty guild (player not in guild)
    public S2CGuildDataPacket() {
        this(false, UUID.randomUUID(), "", "", UUID.randomUUID(), 1, 0, 0, "", 0, 0, new HashMap<>());
    }

    private static Map<UUID, MemberData> convertMembers(Map<UUID, GuildMember> members) {
        Map<UUID, MemberData> result = new HashMap<>();
        for (Map.Entry<UUID, GuildMember> entry : members.entrySet()) {
            GuildMember member = entry.getValue();
            result.put(entry.getKey(), new MemberData(
                member.getPlayerName(),
                member.getRank(),
                member.getContribution()
            ));
        }
        return result;
    }

    public S2CGuildDataPacket(FriendlyByteBuf buf) {
        this(
            buf.readBoolean(),
            buf.readUUID(),
            buf.readUtf(),
            buf.readUtf(),
            buf.readUUID(),
            buf.readInt(),
            buf.readLong(),
            buf.readLong(),
            buf.readUtf(),
            buf.readInt(),
            buf.readInt(),
            readMembers(buf)
        );
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(hasGuild);
        buf.writeUUID(guildId);
        buf.writeUtf(guildName);
        buf.writeUtf(guildTag);
        buf.writeUUID(leaderId);
        buf.writeInt(level);
        buf.writeLong(experience);
        buf.writeLong(expForNextLevel);
        buf.writeUtf(announcement);
        buf.writeInt(guildGold);
        buf.writeInt(maxMembers);
        writeMembers(buf, members);
    }

    private static Map<UUID, MemberData> readMembers(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Map<UUID, MemberData> members = new HashMap<>();
        for (int i = 0; i < size; i++) {
            UUID id = buf.readUUID();
            String name = buf.readUtf();
            String rank = buf.readUtf();
            long contribution = buf.readLong();
            members.put(id, new MemberData(name, rank, contribution));
        }
        return members;
    }

    private static void writeMembers(FriendlyByteBuf buf, Map<UUID, MemberData> members) {
        buf.writeInt(members.size());
        for (Map.Entry<UUID, MemberData> entry : members.entrySet()) {
            buf.writeUUID(entry.getKey());
            buf.writeUtf(entry.getValue().name);
            buf.writeUtf(entry.getValue().rank);
            buf.writeLong(entry.getValue().contribution);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (hasGuild) {
                ClientGuildData.setGuildData(
                    guildId,
                    guildName,
                    guildTag,
                    leaderId,
                    level,
                    experience,
                    expForNextLevel,
                    announcement,
                    guildGold,
                    maxMembers,
                    members
                );
            } else {
                ClientGuildData.clearGuildData();
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public record MemberData(String name, String rank, long contribution) {}
}

