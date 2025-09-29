package me.ajh123.metro_rail.content.tickets;

import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import me.ajh123.metro_rail.foundation.ModComponents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public class TicketItem extends SimplePolymerItem {
    public TicketItem(Settings settings) {
        super(settings, Items.NAME_TAG);
    }

    @Override
    public Text getName(ItemStack stack) {
        TicketType.BoughtTicket ticket = stack.get(ModComponents.TICKET_COMPONENT);
        if (ticket != null) {
            return Text.translatable("item.metro_rail.ticket.long", ticket.name());
        }
        return super.getName(stack);
    }
}
