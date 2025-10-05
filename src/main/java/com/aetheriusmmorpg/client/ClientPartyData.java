package com.aetheriusmmorpg.client;

import com.aetheriusmmorpg.common.party.Party;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Client-side storage for party data.
 */
public class ClientPartyData {

    private static boolean hasParty = false;
    private static UUID partyId = null;
    private static UUID leaderId = null;
    private static List<UUID> memberIds = new ArrayList<>();
    private static List<String> memberNames = new ArrayList<>();
    private static Party.LootMode lootMode = Party.LootMode.FREE_FOR_ALL;
    private static boolean shareExperience = true;

    /**
     * Update party data from server.
     */
    public static void updateParty(UUID partyId, UUID leaderId, List<UUID> memberIds, List<String> memberNames, int lootModeOrdinal, boolean shareExperience) {
        ClientPartyData.hasParty = true;
        ClientPartyData.partyId = partyId;
        ClientPartyData.leaderId = leaderId;
        ClientPartyData.memberIds = new ArrayList<>(memberIds);
        ClientPartyData.memberNames = new ArrayList<>(memberNames);

        Party.LootMode[] modes = Party.LootMode.values();
        if (lootModeOrdinal >= 0 && lootModeOrdinal < modes.length) {
            ClientPartyData.lootMode = modes[lootModeOrdinal];
        }

        ClientPartyData.shareExperience = shareExperience;
    }

    /**
     * Clear party data.
     */
    public static void clearParty() {
        hasParty = false;
        partyId = null;
        leaderId = null;
        memberIds.clear();
        memberNames.clear();
        lootMode = Party.LootMode.FREE_FOR_ALL;
        shareExperience = true;
    }

    /**
     * Check if player is in a party.
     */
    public static boolean hasParty() {
        return hasParty;
    }

    /**
     * Get party ID.
     */
    public static UUID getPartyId() {
        return partyId;
    }

    /**
     * Get leader ID.
     */
    public static UUID getLeaderId() {
        return leaderId;
    }

    /**
     * Check if local player is the leader.
     */
    public static boolean isLeader(UUID localPlayerId) {
        return leaderId != null && leaderId.equals(localPlayerId);
    }

    /**
     * Get member IDs.
     */
    public static List<UUID> getMemberIds() {
        return new ArrayList<>(memberIds);
    }

    /**
     * Get member names.
     */
    public static List<String> getMemberNames() {
        return new ArrayList<>(memberNames);
    }

    /**
     * Get member count.
     */
    public static int getMemberCount() {
        return memberIds.size();
    }

    /**
     * Get loot mode.
     */
    public static Party.LootMode getLootMode() {
        return lootMode;
    }

    /**
     * Check if experience sharing is enabled.
     */
    public static boolean isShareExperience() {
        return shareExperience;
    }
}
