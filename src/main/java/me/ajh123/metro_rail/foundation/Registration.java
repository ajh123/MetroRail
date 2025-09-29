package me.ajh123.metro_rail.foundation;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

import static me.ajh123.metro_rail.MetroRail.MOD_ID;

public class Registration {
    public static void initialise() {
        ModItems.initialise();
        ModBlocks.initialise();
        ModComponents.initialise();
    }

    static <T extends Block & PolymerBlock> T register(String name, Function<AbstractBlock.Settings, T> blockFactory, AbstractBlock.Settings settings, boolean shouldRegisterItem) {
        RegistryKey<Block> blockKey = keyOfBlock(name);
        T block = blockFactory.apply(settings.registryKey(blockKey));

        // Sometimes, you may not want to register an item for the block.
        // Eg: if it's a technical block like `minecraft:moving_piston` or `minecraft:end_gateway`
        if (shouldRegisterItem) {
            RegistryKey<Item> itemKey = keyOfItem(name);
            Item polymerItem = block.getPolymerBlockState(block.getDefaultState(), null).getBlock().asItem();
            Item.Settings itemSettings = new Item.Settings().registryKey(itemKey).useBlockPrefixedTranslationKey();
            PolymerBlockItem blockItem = new PolymerBlockItem(block, itemSettings, polymerItem);
            Registry.register(Registries.ITEM, itemKey, blockItem);
        }

        return Registry.register(Registries.BLOCK, blockKey, block);
    }

    static <T extends Item & PolymerItem> T register(String name, Function<Item.Settings, T> itemFactory, Item.Settings settings) {
        RegistryKey<Item> itemKey = keyOfItem(name);
        T item = itemFactory.apply(settings.registryKey(itemKey));
        Registry.register(Registries.ITEM, itemKey, item);
        return item;
    }

    private static RegistryKey<Block> keyOfBlock(String name) {
        return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, name));
    }

    private static RegistryKey<Item> keyOfItem(String name) {
        return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, name));
    }
}
