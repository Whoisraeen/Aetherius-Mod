package com.aetheriusmmorpg.common.entity.npc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

/**
 * Merchant NPC that sells items and equipment.
 * Uses Minecraft's trading system with custom gold currency.
 */
public class MerchantNPC extends AetheriusNPC {

    private final MerchantOffers offers = new MerchantOffers();
    private String shopName = "General Goods";
    private MerchantType merchantType = MerchantType.GENERAL;

    public MerchantNPC(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setNPCType(NPCType.MERCHANT);
    }

    public enum MerchantType {
        GENERAL,        // General goods and consumables
        WEAPONS,        // Weapons and combat gear
        ARMOR,          // Armor and defensive equipment
        POTIONS,        // Potions and consumables
        MATERIALS,      // Crafting materials
        RARE_GOODS,     // Rare and legendary items
        SKILL_BOOKS     // Skill books and scrolls
    }

    /**
     * Add a merchant offer.
     */
    public void addOffer(ItemStack result, ItemStack cost1, ItemStack cost2, int maxUses, int xpReward) {
        MerchantOffer offer = new MerchantOffer(cost1, cost2, result, maxUses, xpReward, 0.05f);
        offers.add(offer);
    }

    /**
     * Add a simple offer (item for gold).
     */
    public void addSimpleOffer(ItemStack result, int goldCost, int maxUses) {
        ItemStack gold = new ItemStack(com.aetheriusmmorpg.common.registry.ModItems.GOLD_COIN.get(), goldCost);
        addOffer(result, gold, ItemStack.EMPTY, maxUses, 0);
    }

    /**
     * Get merchant type.
     */
    public MerchantType getMerchantType() {
        return merchantType;
    }

    public void setMerchantType(MerchantType type) {
        this.merchantType = type;
    }

    /**
     * Get shop name.
     */
    public String getShopName() {
        return shopName;
    }

    public void setShopName(String name) {
        this.shopName = name;
    }

    @Override
    protected void openMerchantDialog(Player player) {
        if (!this.level().isClientSide) {
            player.sendSystemMessage(Component.literal("§6" + getNpcDisplayName() + "§f: Welcome to " + shopName + "!"));

            // In full implementation, would open custom trading GUI
            if (offers.isEmpty()) {
                player.sendSystemMessage(Component.literal("§7I have no goods available at this time."));
            } else {
                player.sendSystemMessage(Component.literal("§7I have " + offers.size() + " items for sale."));
                // TODO: Open custom merchant GUI
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("ShopName", shopName);
        tag.putString("MerchantType", merchantType.name());

        // Save merchant offers
        net.minecraft.nbt.ListTag offersTag = new net.minecraft.nbt.ListTag();
        for (MerchantOffer offer : offers) {
            offersTag.add(offer.createTag());
        }
        tag.put("Offers", offersTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("ShopName")) {
            shopName = tag.getString("ShopName");
        }
        if (tag.contains("MerchantType")) {
            try {
                merchantType = MerchantType.valueOf(tag.getString("MerchantType"));
            } catch (IllegalArgumentException ignored) {}
        }

        // Load merchant offers
        if (tag.contains("Offers")) {
            net.minecraft.nbt.ListTag offersTag = tag.getList("Offers", 10); // 10 = CompoundTag
            offers.clear();
            for (int i = 0; i < offersTag.size(); i++) {
                MerchantOffer offer = new MerchantOffer(offersTag.getCompound(i));
                offers.add(offer);
            }
        }
    }

    /**
     * Initialize default shop inventory based on merchant type.
     */
    public void initializeDefaultInventory() {
        offers.clear();

        switch (merchantType) {
            case GENERAL:
                // Basic consumables
                addSimpleOffer(new ItemStack(net.minecraft.world.item.Items.BREAD, 16), 10, 999);
                addSimpleOffer(new ItemStack(net.minecraft.world.item.Items.COOKED_BEEF, 8), 15, 999);
                addSimpleOffer(new ItemStack(net.minecraft.world.item.Items.TORCH, 32), 5, 999);
                break;

            case WEAPONS:
                addSimpleOffer(new ItemStack(net.minecraft.world.item.Items.IRON_SWORD), 100, 10);
                addSimpleOffer(new ItemStack(net.minecraft.world.item.Items.IRON_AXE), 100, 10);
                addSimpleOffer(new ItemStack(net.minecraft.world.item.Items.BOW), 150, 10);
                break;

            case ARMOR:
                addSimpleOffer(new ItemStack(net.minecraft.world.item.Items.IRON_HELMET), 80, 10);
                addSimpleOffer(new ItemStack(net.minecraft.world.item.Items.IRON_CHESTPLATE), 120, 10);
                addSimpleOffer(new ItemStack(net.minecraft.world.item.Items.IRON_LEGGINGS), 100, 10);
                addSimpleOffer(new ItemStack(net.minecraft.world.item.Items.IRON_BOOTS), 70, 10);
                break;

            case POTIONS:
                // TODO: Add custom potions
                addSimpleOffer(new ItemStack(net.minecraft.world.item.Items.POTION), 50, 999);
                break;

            case MATERIALS:
                addSimpleOffer(new ItemStack(net.minecraft.world.item.Items.IRON_INGOT, 4), 50, 999);
                addSimpleOffer(new ItemStack(net.minecraft.world.item.Items.GOLD_INGOT, 2), 100, 999);
                addSimpleOffer(new ItemStack(net.minecraft.world.item.Items.DIAMOND), 500, 10);
                break;

            case RARE_GOODS:
                addSimpleOffer(new ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD), 1000, 1);
                addSimpleOffer(new ItemStack(net.minecraft.world.item.Items.ENCHANTED_BOOK), 750, 5);
                addSimpleOffer(new ItemStack(net.minecraft.world.item.Items.TOTEM_OF_UNDYING), 5000, 1);
                break;

            case SKILL_BOOKS:
                // TODO: Add custom skill books
                addSimpleOffer(new ItemStack(net.minecraft.world.item.Items.BOOK), 200, 10);
                break;
        }
    }
}
