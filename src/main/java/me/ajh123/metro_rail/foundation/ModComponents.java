package me.ajh123.metro_rail.foundation;

import me.ajh123.metro_rail.MetroRail;
import me.ajh123.metro_rail.content.tickets.Ticket;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModComponents {
    public static final ComponentType<Ticket.BoughtTicket> TICKET_COMPONENT = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(MetroRail.MOD_ID, "bought_ticket"),
            ComponentType.<Ticket.BoughtTicket>builder().codec(Ticket.BoughtTicket.CODEC).build()
    );

    public static void initialise() {}
}
