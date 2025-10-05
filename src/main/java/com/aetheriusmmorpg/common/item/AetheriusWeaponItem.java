package com.aetheriusmmorpg.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Base class for Aetherius weapons.
 * Displays custom RPG stats in tooltip.
 */
public class AetheriusWeaponItem extends SwordItem {

    private final double bonusPower;
    private final double bonusCritRate;

    public AetheriusWeaponItem(Tier tier, int attackDamage, float attackSpeed,
                                double bonusPower, double bonusCritRate, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
        this.bonusPower = bonusPower;
        this.bonusCritRate = bonusCritRate;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        if (bonusPower > 0) {
            tooltip.add(Component.literal("+" + (int)bonusPower + " Power")
                .withStyle(ChatFormatting.GREEN));
        }

        if (bonusCritRate > 0) {
            tooltip.add(Component.literal("+" + (int)bonusCritRate + "% Crit Rate")
                .withStyle(ChatFormatting.YELLOW));
        }
    }

    public double getBonusPower() {
        return bonusPower;
    }

    public double getBonusCritRate() {
        return bonusCritRate;
    }
}
