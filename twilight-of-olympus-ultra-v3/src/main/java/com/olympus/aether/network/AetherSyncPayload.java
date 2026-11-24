package com.olympus.aether.network;

import com.olympus.aether.OlympusMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record AetherSyncPayload(
    boolean unlocked,
    int coreLayer,
    int aether,
    int maxAether,
    int activeRune,
    boolean destructionActive,
    int currentStage
) implements CustomPayload {

    public static final CustomPayload.Id<AetherSyncPayload> ID =
        new CustomPayload.Id<>(Identifier.of(OlympusMod.MOD_ID, "aether_sync"));

    public static final PacketCodec<RegistryByteBuf, AetherSyncPayload> CODEC = PacketCodec.of(
        (value, buf) -> {
            buf.writeBoolean(value.unlocked);
            buf.writeVarInt(value.coreLayer);
            buf.writeVarInt(value.aether);
            buf.writeVarInt(value.maxAether);
            buf.writeVarInt(value.activeRune);
            buf.writeBoolean(value.destructionActive);
            buf.writeVarInt(value.currentStage);
        },
        buf -> new AetherSyncPayload(
            buf.readBoolean(),
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readBoolean(),
            buf.readVarInt()
        )
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
