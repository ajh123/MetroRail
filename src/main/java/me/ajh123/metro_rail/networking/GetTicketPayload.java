package me.ajh123.metro_rail.networking;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.security.*;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import static me.ajh123.metro_rail.MetroRail.MOD_ID;

public record GetTicketPayload(
    BlockPos dispenserPos,
    int ticketId,
    UUID playerUuid
) {
    public static final Identifier GET_TICKET_PAYLOAD_IDENTIFIER = Identifier.of(MOD_ID, "get_ticket");

    public static GetTicketPayload load(NbtElement nbt) {
        Optional<NbtCompound> data = nbt.asCompound();
        if (data.isEmpty()) {
            throw new IllegalArgumentException("Payload must be a compound for GetTicketPayload");
        }
        NbtCompound dataCompound = data.get();

        Optional<NbtCompound> dispenserPosNbtO = dataCompound.getCompound("dispenserPos");

        if (dispenserPosNbtO.isEmpty()) {
            throw new IllegalArgumentException("Invalid NBT data for GetTicketPayload: missing dispenserPos");
        }

        NbtCompound dispenserPosNbt = dispenserPosNbtO.get();
        Optional<Integer> x = dispenserPosNbt.getInt("x");
        Optional<Integer> y = dispenserPosNbt.getInt("y");
        Optional<Integer> z = dispenserPosNbt.getInt("z");
        Optional<Integer> ticket_id = dataCompound.getInt("ticketId");
        Optional<long[]> uuidArray = dataCompound.getLongArray("playerUuid");
        if (dispenserPosNbt.isEmpty() || x.isEmpty() || y.isEmpty() || z.isEmpty() || ticket_id.isEmpty() || uuidArray.isEmpty() || uuidArray.get().length != 2) {
            throw new IllegalArgumentException("Invalid NBT data for GetTicketPayload");
        }
        BlockPos dispenserPos = new BlockPos(x.get(), y.get(), z.get());
        UUID playerUuid = new UUID(uuidArray.get()[0], uuidArray.get()[1]);
        return new GetTicketPayload(dispenserPos, ticket_id.get(), playerUuid);
    }

    public static GetTicketPayload loadSigned(NbtElement signedNbt, KeyPair keyPair) {
        try {
            NbtCompound signedCompound = signedNbt.asCompound().orElseThrow();
            NbtCompound payload = signedCompound.getCompound("payload").orElseThrow();
            byte[] signature = signedCompound.getByteArray("signature").orElseThrow();

            // Serialize payload to bytes using proper NBT binary format
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            payload.write(dos);
            byte[] payloadBytes = baos.toByteArray();

            // Verify signature
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(keyPair.getPublic());
            verifier.update(payloadBytes);
            if (!verifier.verify(signature)) {
                throw new SecurityException("Payload signature is invalid!");
            }

            return GetTicketPayload.load(payload);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public NbtElement write() {
        NbtCompound nbt = new NbtCompound();
        NbtCompound dispenserPosNbt = new NbtCompound();
        dispenserPosNbt.putInt("x", this.dispenserPos.getX());
        dispenserPosNbt.putInt("y", this.dispenserPos.getY());
        dispenserPosNbt.putInt("z", this.dispenserPos.getZ());
        nbt.put("dispenserPos", dispenserPosNbt);
        nbt.putInt("ticketId", this.ticketId);
        NbtLongArray uuidArray = new NbtLongArray(new long[]{
            this.playerUuid.getMostSignificantBits(),
            this.playerUuid.getLeastSignificantBits()
        });
        nbt.put("playerUuid", uuidArray);
        return nbt;
    }

    public NbtElement writeSigned(KeyPair keyPair) {
        try {
            NbtElement nbt = write();

            // Serialize NBT to canonical binary format
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            nbt.write(dos);
            byte[] payloadBytes = baos.toByteArray();

            // Sign payload
            Signature signer = Signature.getInstance("SHA256withRSA");
            signer.initSign(keyPair.getPrivate());
            signer.update(payloadBytes);
            byte[] signature = signer.sign();

            NbtCompound signedNbt = new NbtCompound();
            signedNbt.put("payload", nbt);
            signedNbt.putByteArray("signature", signature);
            return signedNbt;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
