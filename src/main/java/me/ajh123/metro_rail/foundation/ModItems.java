package me.ajh123.metro_rail.foundation;

import eu.pb4.polymer.core.api.item.PolymerItem;
import me.ajh123.metro_rail.content.tickets.TicketItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

import static me.ajh123.metro_rail.MetroRail.MOD_ID;

public class ModItems {
    public static final TicketItem TICKET = register("ticket", TicketItem::new,
            new Item.Settings().maxCount(1));

    public static <T extends Item & PolymerItem> T register(String name, Function<Item.Settings, T> itemFactory, Item.Settings settings) {
        // Create the item key.
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, name));

        // Create the item instance.
        T item = itemFactory.apply(settings.registryKey(itemKey));

        // Register the item.
        Registry.register(Registries.ITEM, itemKey, item);

        return item;
    }

    public static void initialise() {}
}