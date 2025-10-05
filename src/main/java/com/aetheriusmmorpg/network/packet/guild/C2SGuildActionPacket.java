package com.aetheriusmmorpg.network.packet.guild;

import com.aetheriusmmorpg.AetheriusMod;
import com.aetheriusmmorpg.common.guild.GuildManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Client->Server packet for guild actions (create, invite, leave, kick, etc.)
 */
public record C2SGuildActionPacket(
    GuildAction action,
    String guildName,
    String guildTag,
    UUID targetPlayer,
    UUID guildId,
    String newRank
) {

    public C2SGuildActionPacket(FriendlyByteBuf buf) {
        this(
            buf.readEnum(GuildAction.class),
            buf.readUtf(),
            buf.readUtf(),
            buf.readUUID(),
            buf.readUUID(),
            buf.readUtf()
        );
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(action);
        buf.writeUtf(guildName);
        buf.writeUtf(guildTag);
        buf.writeUUID(targetPlayer);
        buf.writeUUID(guildId);
        buf.writeUtf(newRank);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            GuildManager manager = GuildManager.get(player.getServer());

            switch (action) {
                case CREATE:
                    manager.createGuild(player, guildName, guildTag);
                    break;

                case INVITE:
                    ServerPlayer target = player.getServer().getPlayerList().getPlayer(targetPlayer);
                    if (target != null) {
                        manager.inviteToGuild(player, target, guildId);
                    }
                    break;

                case ACCEPT_INVITE:
                    manager.acceptGuildInvitation(player, guildId);
                    break;

                case DECLINE_INVITE:
                    manager.declineGuildInvitation(player, guildId);
                    break;

                case LEAVE:
                    manager.leaveGuild(player);
                    break;

                case KICK_MEMBER:
                    manager.kickMember(player, targetPlayer, guildId);
                    break;

                case DISBAND:
                    manager.disbandGuild(player, guildId);
                    break;

                case PROMOTE:
                    // TODO: Implement rank promotion
                    AetheriusMod.LOGGER.info("Guild promotion not yet implemented");
                    break;
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public enum GuildAction {
        CREATE,
        INVITE,
        ACCEPT_INVITE,
        DECLINE_INVITE,
        LEAVE,
        KICK_MEMBER,
        DISBAND,
        PROMOTE
    }

    // Convenience constructors
    public static C2SGuildActionPacket create(String name, String tag) {
        return new C2SGuildActionPacket(GuildAction.CREATE, name, tag, UUID.randomUUID(), UUID.randomUUID(), "");
    }

    public static C2SGuildActionPacket invite(UUID targetPlayer, UUID guildId) {
        return new C2SGuildActionPacket(GuildAction.INVITE, "", "", targetPlayer, guildId, "");
    }

    public static C2SGuildActionPacket acceptInvite(UUID guildId) {
        return new C2SGuildActionPacket(GuildAction.ACCEPT_INVITE, "", "", UUID.randomUUID(), guildId, "");
    }

    public static C2SGuildActionPacket declineInvite(UUID guildId) {
        return new C2SGuildActionPacket(GuildAction.DECLINE_INVITE, "", "", UUID.randomUUID(), guildId, "");
    }

    public static C2SGuildActionPacket leave() {
        return new C2SGuildActionPacket(GuildAction.LEAVE, "", "", UUID.randomUUID(), UUID.randomUUID(), "");
    }

    public static C2SGuildActionPacket kick(UUID targetPlayer, UUID guildId) {
        return new C2SGuildActionPacket(GuildAction.KICK_MEMBER, "", "", targetPlayer, guildId, "");
    }

    public static C2SGuildActionPacket disband(UUID guildId) {
        return new C2SGuildActionPacket(GuildAction.DISBAND, "", "", UUID.randomUUID(), guildId, "");
    }
}

