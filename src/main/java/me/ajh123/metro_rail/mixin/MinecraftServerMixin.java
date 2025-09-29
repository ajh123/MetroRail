package me.ajh123.metro_rail.mixin;

import me.ajh123.metro_rail.foundation.ModItems;
import me.ajh123.metro_rail.networking.GetTicketPayload;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "handleCustomClickAction", at = @At("HEAD"))
    public void handleCustomClickAction(Identifier id, Optional<NbtElement> payload, CallbackInfo ci) {
        MinecraftServer self = (MinecraftServer)(Object)this;

        if (id.equals(GetTicketPayload.GET_TICKET_PAYLOAD_IDENTIFIER)) {
            NbtElement preNbt = payload.orElseThrow(() -> new IllegalArgumentException("Payload is required for GetTicketPayload"));
            Optional<NbtCompound> nbt = preNbt.asCompound();
            if (nbt.isEmpty()) {
                throw new IllegalArgumentException("Payload must be a compound for GetTicketPayload");
            }
            GetTicketPayload getTicketPayload = GetTicketPayload.load(nbt.get());

            UUID playerUUID = getTicketPayload.playerUuid();
            ServerPlayerEntity player = self.getPlayerManager().getPlayer(playerUUID);

            if (player != null) {
                player.getInventory().insertStack(new ItemStack(ModItems.TICKET));
            }
        }
    }
}
