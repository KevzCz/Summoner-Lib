package net.pixeldreamstudios.summonerlib.network.payload;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.pixeldreamstudios.summonerlib.SummonerLib;

public record UnsummonAllPayload() implements CustomPayload {
    public static final CustomPayload.Id<UnsummonAllPayload> ID = new CustomPayload.Id<>(
            Identifier.of(SummonerLib.MOD_ID, "unsummon_all")
    );

    public static final PacketCodec<RegistryByteBuf, UnsummonAllPayload> CODEC = PacketCodec.of(
            (value, buf) -> {},
            buf -> new UnsummonAllPayload()
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}