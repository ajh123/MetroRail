package me.ajh123.metro_rail.networking;

import java.util.UUID;

import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import static me.ajh123.metro_rail.MetroRail.MOD_ID;

public record GetTicketS2CPayload(
    BlockPos dispenserPos,
    int ticket_id,
    UUID playerUuid
) implements CustomPayload {
    public static final Identifier GET_TICKET_PAYLOAD_IDENTIFIER = Identifier.of(MOD_ID, "get_ticket");
    public static final CustomPayload.Id<GetTicketS2CPayload> ID = new CustomPayload.Id<>(GET_TICKET_PAYLOAD_IDENTIFIER);
    // public static final PacketCodec<RegistryByteBuf, GetTicketS2CPayload> CODEC = PacketCodec.tuple(
	// 	BlockPos.PACKET_CODEC, GetTicketS2CPayload::dispenserPos,
	// 	PacketCodecs.INTEGER, GetTicketS2CPayload::ticket_id,
	// 	PacketCodecs.UUID, GetTicketS2CPayload::playerUuid, //TODO: figure out correct type for UUID
	// 	GetTicketS2CPayload::new
	// );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

}
