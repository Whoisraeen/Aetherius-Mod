package com.aetheriusmmorpg.network.packet;

import com.aetheriusmmorpg.client.ui.screen.CharacterCreationScreen;
import com.aetheriusmmorpg.common.menu.CharacterCreationMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server->Client packet to open the character creation screen.
 */
public class S2COpenCharacterCreationPacket {

    public S2COpenCharacterCreationPacket() {
    }

    public S2COpenCharacterCreationPacket(FriendlyByteBuf buf) {
        // No data needed
    }

    public void encode(FriendlyByteBuf buf) {
        // No data to encode
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, player) -> new CharacterCreationMenu(containerId, playerInventory),
                    net.minecraft.network.chat.Component.literal("Character Creation")
                ));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
