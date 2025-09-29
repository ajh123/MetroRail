package me.ajh123.metro_rail.content.tickets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.concurrent.atomic.AtomicInteger;

public record Ticket(
    int id,
    String name,
    TicketPrice price
) {

    public record BoughtTicket(
        String name
    ) {
        public static final Codec<BoughtTicket> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.STRING.fieldOf("name").forGetter(BoughtTicket::name)
        ).apply(builder, BoughtTicket::new));
    }

    public MutableText displayPrice() {
        return price.displayPrice(this);
    }

    public MutableText displayInsufficientFunds() {
        return price.displayInsufficientFunds(this);
    }

    public abstract static class TicketPrice {
        public abstract boolean purchase(PlayerEntity player, boolean simulate);
        public abstract MutableText displayPrice(Ticket ticket);
        public abstract MutableText displayInsufficientFunds(Ticket ticket);
    }

    public static class FreePrice extends TicketPrice {
        @Override
        public boolean purchase(PlayerEntity player, boolean simulate) {
            return true;
        }

        @Override
        public MutableText displayPrice(Ticket ticket) {
            return Text.translatable("gui.metro_rail.ticket.free", ticket.name);
        }

        @Override
        public MutableText displayInsufficientFunds(Ticket ticket) {
            return Text.translatable("gui.metro_rail.ticket.free.insufficient_funds", ticket.name);
        }
    }

    public static class ItemPrice extends TicketPrice {
        private final ItemStack price;

        public ItemPrice(ItemStack price) {
            this.price = price;
        }

        @Override
        public boolean purchase(PlayerEntity player, boolean simulate) {
            AtomicInteger storedCount = new AtomicInteger();
            player.getInventory().forEach((itemStack) -> {
                if (
                    itemStack.getItem().equals(price.getItem()) &&
                    itemStack.getComponents().equals(price.getComponents())
                ) {
                    storedCount.addAndGet(itemStack.getCount());
                }
            });

            boolean canAfford = storedCount.get() >= price.getCount();

            if (!simulate && canAfford) {
                int toRemove = price.getCount();
                for (int i = 0; i < player.getInventory().size(); i++) {
                    ItemStack itemStack = player.getInventory().getStack(i);
                    if (
                        itemStack.getItem().equals(price.getItem()) &&
                        itemStack.getComponents().equals(price.getComponents())
                    ) {
                        int removed = Math.min(itemStack.getCount(), toRemove);
                        itemStack.decrement(removed);
                        toRemove -= removed;
                        if (toRemove <= 0) {
                            break;
                        }
                    }
                }
            }

            return canAfford;
        }

        @Override
        public MutableText displayPrice(Ticket ticket) {
            return Text.translatable("gui.metro_rail.ticket.itemPrice", ticket.name, price.getCount(), price.getName());
        }

        @Override
        public MutableText displayInsufficientFunds(Ticket ticket) {
            return Text.translatable("gui.metro_rail.ticket.itemPrice.insufficient_funds", ticket.name, price.getCount(), price.getName());
        }
    }

    public static TicketPrice of(ItemStack price) {
        if (price.isEmpty()) {
            return new FreePrice();
        } else {
            return new ItemPrice(price);
        }
    }
}
