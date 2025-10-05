package com.aetheriusmmorpg.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Simple health potion that restores health.
 * Server-authoritative healing.
 */
public class HealthPotionItem extends Item {

    private final float healAmount;

    public HealthPotionItem(float healAmount, Properties properties) {
        super(properties);
        this.healAmount = healAmount;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // Server-side: heal player
            float currentHealth = player.getHealth();
            float maxHealth = player.getMaxHealth();

            if (currentHealth < maxHealth) {
                player.heal(healAmount);
                stack.shrink(1);

                player.displayClientMessage(
                    Component.literal("+" + (int)healAmount + " Health")
                        .withStyle(ChatFormatting.GREEN),
                    true // action bar
                );
            }
        } else {
            // Client-side: play sound
            level.playSound(player, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("Restores " + (int)healAmount + " health")
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Right-click to use")
            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }
}
