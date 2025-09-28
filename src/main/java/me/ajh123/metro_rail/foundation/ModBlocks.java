package me.ajh123.metro_rail.foundation;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import me.ajh123.metro_rail.content.tickets.TicketDispenser;
import me.ajh123.metro_rail.content.tickets.TicketGate;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

import static me.ajh123.metro_rail.MetroRail.MOD_ID;

public class ModBlocks {
    public static final TicketGate TICKET_GATE = register("ticket_gate", TicketGate::new,
            AbstractBlock.Settings.copy(Blocks.OAK_FENCE_GATE), true);

    public static final TicketDispenser TICKET_DISPENSER = register("ticket_dispenser", TicketDispenser::new,
            AbstractBlock.Settings.copy(Blocks.DISPENSER), true);

    private static <T extends Block & PolymerBlock> T register(String name, Function<AbstractBlock.Settings, T> blockFactory, AbstractBlock.Settings settings, boolean shouldRegisterItem) {
        // Create a registry key for the block
        RegistryKey<Block> blockKey = keyOfBlock(name);
        // Create the block instance
        T block = blockFactory.apply(settings.registryKey(blockKey));

        // Sometimes, you may not want to register an item for the block.
        // Eg: if it's a technical block like `minecraft:moving_piston` or `minecraft:end_gateway`
        if (shouldRegisterItem) {
            // Items need to be registered with a different type of registry key, but the ID
            // can be the same.
            RegistryKey<Item> itemKey = keyOfItem(name);

            Item polymerItem = block.getPolymerBlockState(block.getDefaultState(), null).getBlock().asItem();

            PolymerBlockItem blockItem = new PolymerBlockItem(
                    block,
                    new Item.Settings().registryKey(itemKey).useBlockPrefixedTranslationKey(),
                    polymerItem
            );
            Registry.register(Registries.ITEM, itemKey, blockItem);
        }

        return Registry.register(Registries.BLOCK, blockKey, block);
    }

    private static RegistryKey<Block> keyOfBlock(String name) {
        return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, name));
    }

    private static RegistryKey<Item> keyOfItem(String name) {
        return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, name));
    }

    public static void initialise() {}
}