package me.ajh123.metro_rail.foundation;

import me.ajh123.metro_rail.content.tickets.TicketItem;
import net.minecraft.item.Item;

public class ModItems {
    public static final TicketItem TICKET = Registration.register("ticket", TicketItem::new,
            new Item.Settings().maxCount(1));

    public static void initialise() {}
}