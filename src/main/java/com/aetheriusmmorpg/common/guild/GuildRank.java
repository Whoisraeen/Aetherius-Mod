package com.aetheriusmmorpg.common.guild;

import net.minecraft.nbt.CompoundTag;

import java.util.EnumSet;

/**
 * Represents a rank within a guild with associated permissions.
 */
public class GuildRank {

    private String name;
    private int priority; // Higher number = higher rank
    private EnumSet<GuildPermission> permissions;

    public GuildRank(String name, int priority, EnumSet<GuildPermission> permissions) {
        this.name = name;
        this.priority = priority;
        this.permissions = permissions;
    }

    public boolean hasPermission(GuildPermission permission) {
        return permissions.contains(permission);
    }

    public void addPermission(GuildPermission permission) {
        permissions.add(permission);
    }

    public void removePermission(GuildPermission permission) {
        permissions.remove(permission);
    }

    // NBT Serialization
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Name", name);
        tag.putInt("Priority", priority);

        // Store permissions as a bit field
        long permissionBits = 0;
        for (GuildPermission permission : permissions) {
            permissionBits |= (1L << permission.ordinal());
        }
        tag.putLong("Permissions", permissionBits);

        return tag;
    }

    public static GuildRank deserializeNBT(CompoundTag tag) {
        String name = tag.getString("Name");
        int priority = tag.getInt("Priority");
        long permissionBits = tag.getLong("Permissions");

        // Reconstruct permission set from bit field
        EnumSet<GuildPermission> permissions = EnumSet.noneOf(GuildPermission.class);
        for (GuildPermission permission : GuildPermission.values()) {
            if ((permissionBits & (1L << permission.ordinal())) != 0) {
                permissions.add(permission);
            }
        }

        return new GuildRank(name, priority, permissions);
    }

    // Getters
    public String getName() { return name; }
    public int getPriority() { return priority; }
    public EnumSet<GuildPermission> getPermissions() { return EnumSet.copyOf(permissions); }
}
