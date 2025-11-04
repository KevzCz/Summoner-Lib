package net.pixeldreamstudios.summonerlib.network.payload;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.pixeldreamstudios.summonerlib.SummonerLib;

import java.util.UUID;

public record SummonRemovePayload(UUID entityUuid) implements CustomPayload {

    public static final Id<SummonRemovePayload> ID = new Id<>(Identifier.of(SummonerLib.MOD_ID, "summon_remove"));

    public static final PacketCodec<RegistryByteBuf, SummonRemovePayload> CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, SummonRemovePayload::entityUuid,
            SummonRemovePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}