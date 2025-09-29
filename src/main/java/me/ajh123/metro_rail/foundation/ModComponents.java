package me.ajh123.metro_rail.foundation;

import me.ajh123.metro_rail.MetroRail;
import me.ajh123.metro_rail.content.tickets.TicketType;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModComponents {
    public static final ComponentType<TicketType.BoughtTicket> TICKET_COMPONENT = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(MetroRail.MOD_ID, "bought_ticket"),
            ComponentType.<TicketType.BoughtTicket>builder().codec(TicketType.BoughtTicket.CODEC).build()
    );

    public static void initialise() {}
}
