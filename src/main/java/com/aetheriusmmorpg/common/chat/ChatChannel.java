package com.aetheriusmmorpg.common.chat;

/**
 * PWI-style chat channels for multi-channel communication.
 */
public enum ChatChannel {
    GLOBAL("Global", "§e", true, 0),           // Server-wide, yellow
    LOCAL("Local", "§f", true, 50),            // Range-based (50 blocks), white
    GUILD("Guild", "§b", false, 0),            // Guild members only, aqua
    PARTY("Party", "§d", false, 0),            // Party members only, light purple
    TRADE("Trade", "§6", true, 0),             // Trading channel, gold
    PM("PM", "§7", false, 0),                  // Private messages, gray
    SYSTEM("System", "§7", false, 0),          // System announcements, gray
    FACTION("Faction", "§5", false, 0),        // Faction/alliance channel, dark purple
    WORLD_EVENT("Event", "§c", false, 0);      // World events, red

    private final String displayName;
    private final String colorCode;
    private final boolean requiresRange;
    private final int range;

    ChatChannel(String displayName, String colorCode, boolean requiresRange, int range) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.requiresRange = requiresRange;
        this.range = range;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public boolean requiresRange() {
        return requiresRange;
    }

    public int getRange() {
        return range;
    }

    public String formatMessage(String senderName, String message) {
        return switch (this) {
            case GLOBAL -> colorCode + "[Global] §f<" + senderName + "> " + message;
            case LOCAL -> colorCode + "<" + senderName + "> " + message;
            case GUILD -> colorCode + "[Guild] §f<" + senderName + "> " + message;
            case PARTY -> colorCode + "[Party] §f<" + senderName + "> " + message;
            case TRADE -> colorCode + "[Trade] §f<" + senderName + "> " + message;
            case PM -> colorCode + "[PM] §f<" + senderName + "> §7" + message;
            case SYSTEM -> colorCode + "[System] " + message;
            case FACTION -> colorCode + "[Faction] §f<" + senderName + "> " + message;
            case WORLD_EVENT -> colorCode + "§l[Event] §r§c" + message;
        };
    }

    public static ChatChannel fromString(String name) {
        for (ChatChannel channel : values()) {
            if (channel.name().equalsIgnoreCase(name)) {
                return channel;
            }
        }
        return LOCAL; // Default to local
    }
}
