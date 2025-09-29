package me.ajh123.metro_rail.mixin;

import me.ajh123.metro_rail.content.tickets.Ticket;
import me.ajh123.metro_rail.content.tickets.TicketDispenser;
import me.ajh123.metro_rail.foundation.ModItems;
import me.ajh123.metro_rail.networking.GetTicketPayload;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.security.KeyPair;
import java.util.Optional;
import java.util.UUID;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "handleCustomClickAction", at = @At("HEAD"))
    public void handleCustomClickAction(Identifier id, Optional<NbtElement> payload, CallbackInfo ci) {
        MinecraftServer self = (MinecraftServer)(Object)this;
        KeyPair keyPair = self.getKeyPair();

        if (id.equals(GetTicketPayload.GET_TICKET_PAYLOAD_IDENTIFIER)) {
            NbtElement nbt = payload.orElseThrow(() -> new IllegalArgumentException("Payload is required for GetTicketPayload"));

            GetTicketPayload getTicketPayload = GetTicketPayload.loadSigned(nbt, keyPair);

            UUID playerUUID = getTicketPayload.playerUuid();
            ServerPlayerEntity player = self.getPlayerManager().getPlayer(playerUUID);

            if (player != null) {
                Ticket ticket = TicketDispenser.getTicketById(player.getWorld(), getTicketPayload.dispenserPos(), getTicketPayload.ticketId());
                if (ticket == null) {
                    player.sendMessage(Text.translatable("gui.metro_rail.ticket.invalid_ticket").formatted(Formatting.RED), true);
                    return;
                }
                boolean purchased = ticket.price().purchase(player, false);
                if (!purchased) {
                    player.sendMessage(ticket.displayInsufficientFunds().formatted(Formatting.RED), true);
                    return;
                }
                player.getInventory().insertStack(new ItemStack(ModItems.TICKET));
            }
        }
    }
}
