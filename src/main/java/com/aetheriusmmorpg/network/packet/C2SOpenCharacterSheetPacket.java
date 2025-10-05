package com.aetheriusmmorpg.network.packet;

import com.aetheriusmmorpg.common.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

/**
 * Client-to-Server packet requesting to open the character sheet.
 * Server validates and opens the GUI.
 */
public class C2SOpenCharacterSheetPacket {

    public C2SOpenCharacterSheetPacket() {
    }

    public C2SOpenCharacterSheetPacket(FriendlyByteBuf buf) {
        // No data to read
    }

    public void toBytes(FriendlyByteBuf buf) {
        // No data to write
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                // Open the character sheet menu
                NetworkHooks.openScreen(player,
                    new SimpleMenuProvider(
                        (containerId, inventory, p) -> ModMenus.CHARACTER_SHEET.get().create(containerId, inventory),
                        Component.translatable("gui.aetherius.character_sheet")
                    )
                );
            }
        });
        return true;
    }
}
