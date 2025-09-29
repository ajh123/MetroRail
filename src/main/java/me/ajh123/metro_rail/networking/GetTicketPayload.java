package me.ajh123.metro_rail.networking;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import static me.ajh123.metro_rail.MetroRail.MOD_ID;

public record GetTicketPayload(
    BlockPos dispenserPos,
    int ticket_id,
    UUID playerUuid
) {
    public static final Identifier GET_TICKET_PAYLOAD_IDENTIFIER = Identifier.of(MOD_ID, "get_ticket");

    public static GetTicketPayload load(NbtCompound nbt) {
        Optional<NbtCompound> dispenserPosNbtO = nbt.getCompound("dispenserPos");

        if (dispenserPosNbtO.isEmpty()) {
            throw new IllegalArgumentException("Invalid NBT data for GetTicketPayload: missing dispenserPos");
        }

        NbtCompound dispenserPosNbt = dispenserPosNbtO.get();
        Optional<Integer> x = dispenserPosNbt.getInt("x");
        Optional<Integer> y = dispenserPosNbt.getInt("y");
        Optional<Integer> z = dispenserPosNbt.getInt("z");
        Optional<Integer> ticket_id = nbt.getInt("ticket_id");
        Optional<long[]> uuidArray = nbt.getLongArray("playerUuid");
        System.out.println(
            "DispenserPos: " + dispenserPosNbt +
            ", x: " + x +
            ", y: " + y +
            ", z: " + z +
            ", ticket_id: " + ticket_id +
            ", uuidArray: " + uuidArray
        );
        if (dispenserPosNbt.isEmpty() || x.isEmpty() || y.isEmpty() || z.isEmpty() || ticket_id.isEmpty() || uuidArray.isEmpty() || uuidArray.get().length != 2) {
            throw new IllegalArgumentException("Invalid NBT data for GetTicketPayload");
        }
        BlockPos dispenserPos = new BlockPos(x.get(), y.get(), z.get());
        UUID playerUuid = new UUID(uuidArray.get()[0], uuidArray.get()[1]);
        return new GetTicketPayload(dispenserPos, ticket_id.get(), playerUuid);
    }

    public void write(NbtCompound nbt) {
        NbtCompound dispenserPosNbt = new NbtCompound();
        dispenserPosNbt.putInt("x", this.dispenserPos.getX());
        dispenserPosNbt.putInt("y", this.dispenserPos.getY());
        dispenserPosNbt.putInt("z", this.dispenserPos.getZ());
        nbt.put("dispenserPos", dispenserPosNbt);
        nbt.putInt("ticket_id", this.ticket_id);
        NbtLongArray uuidArray = new NbtLongArray(new long[]{
            this.playerUuid.getMostSignificantBits(),
            this.playerUuid.getLeastSignificantBits()
        });
        nbt.put("playerUuid", uuidArray);
    }
}
