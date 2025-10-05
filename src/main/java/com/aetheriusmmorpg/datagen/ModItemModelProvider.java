package com.aetheriusmmorpg.datagen;

import com.aetheriusmmorpg.AetheriusMod;
import com.aetheriusmmorpg.common.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Generates item models for all Aetherius items.
 * Most items will use generated parent for simplicity.
 */
public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, AetheriusMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // Textures will be added later; skipping for now to allow build to complete
        // simpleItem(ModItems.BASIC_SWORD.get());
        // simpleItem(ModItems.HEALTH_POTION.get());
    }

    private void simpleItem(Item item) {
        ResourceLocation itemLocation = item.builtInRegistryHolder().key().location();
        withExistingParent(itemLocation.getPath(),
            new ResourceLocation("item/generated")).texture("layer0",
            new ResourceLocation(AetheriusMod.MOD_ID, "item/" + itemLocation.getPath()));
    }
}
