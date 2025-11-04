package net.pixeldreamstudios.summonerlib.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.pixeldreamstudios.summonerlib.network.payload.SummonRemovePayload;
import net.pixeldreamstudios.summonerlib.network.payload.SummonSyncPayload;

import java.util.UUID;

public class NetworkHandler {

    public static void sendSummonSync(ServerPlayerEntity player, UUID entityUuid, String summonType, boolean isRegistering) {
        SummonSyncPayload payload = new SummonSyncPayload(entityUuid, summonType, isRegistering);
        ServerPlayNetworking.send(player, payload);
    }

    public static void sendSummonSyncToAll(ServerWorld world, UUID entityUuid, String summonType, boolean isRegistering) {
        SummonSyncPayload payload = new SummonSyncPayload(entityUuid, summonType, isRegistering);
        for (ServerPlayerEntity player : world.getPlayers()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void sendSummonRemove(ServerPlayerEntity player, UUID entityUuid) {
        SummonRemovePayload payload = new SummonRemovePayload(entityUuid);
        ServerPlayNetworking.send(player, payload);
    }
    public static void sendSummonRemoveToAll(ServerWorld world, UUID entityUuid) {
        SummonRemovePayload payload = new SummonRemovePayload(entityUuid);
        for (ServerPlayerEntity player : world.getPlayers()) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}