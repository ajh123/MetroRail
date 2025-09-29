package me.ajh123.metro_rail.foundation;

import me.ajh123.metro_rail.content.tickets.TicketDispenser;
import me.ajh123.metro_rail.content.tickets.TicketGate;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;

public class ModBlocks {
    public static final TicketGate TICKET_GATE = Registration.register("ticket_gate", TicketGate::new,
            AbstractBlock.Settings.copy(Blocks.OAK_FENCE_GATE), true);

    public static final TicketDispenser TICKET_DISPENSER = Registration.register("ticket_dispenser", TicketDispenser::new,
            AbstractBlock.Settings.copy(Blocks.DISPENSER), true);

    public static void initialise() {}
}