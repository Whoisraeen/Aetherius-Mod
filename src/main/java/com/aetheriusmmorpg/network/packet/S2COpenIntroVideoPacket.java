package com.aetheriusmmorpg.network.packet;

import com.aetheriusmmorpg.client.ui.screen.CharacterCreationScreen;
import com.aetheriusmmorpg.client.ui.screen.IntroVideoScreen;
import com.aetheriusmmorpg.common.menu.CharacterCreationMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server->Client packet to open the intro video screen.
 * Shown to new players before character creation.
 */
public class S2COpenIntroVideoPacket {

    private final boolean showCharacterCreationAfter;

    public S2COpenIntroVideoPacket(boolean showCharacterCreationAfter) {
        this.showCharacterCreationAfter = showCharacterCreationAfter;
    }

    public S2COpenIntroVideoPacket(FriendlyByteBuf buf) {
        this.showCharacterCreationAfter = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(showCharacterCreationAfter);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                // Create the next screen (character creation if needed)
                net.minecraft.client.gui.screens.Screen nextScreen = null;

                if (showCharacterCreationAfter) {
                    // Character creation screen will open after video
                    nextScreen = new CharacterCreationScreen(
                        new CharacterCreationMenu(0, mc.player.getInventory()),
                        mc.player.getInventory(),
                        net.minecraft.network.chat.Component.literal("Character Creation")
                    );
                }

                // Open intro video screen
                mc.setScreen(new IntroVideoScreen(nextScreen));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
