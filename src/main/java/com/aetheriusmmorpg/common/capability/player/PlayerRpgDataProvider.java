package com.aetheriusmmorpg.common.capability.player;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Capability provider for PlayerRpgData.
 * Attaches to player entities to store RPG state.
 */
public class PlayerRpgDataProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static final Capability<PlayerRpgData> PLAYER_RPG_DATA = PlayerRpgData.CAPABILITY;

    private PlayerRpgData data = null;
    private final LazyOptional<PlayerRpgData> optional = LazyOptional.of(this::createData);

    private PlayerRpgData createData() {
        if (data == null) {
            data = new PlayerRpgDataImpl();
        }
        return data;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == PLAYER_RPG_DATA) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        createData().serializeNBT();
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        createData().deserializeNBT(tag);
    }
}
