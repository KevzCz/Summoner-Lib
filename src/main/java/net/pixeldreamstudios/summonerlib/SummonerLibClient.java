package net.pixeldreamstudios.summonerlib;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.pixeldreamstudios.summonerlib.network.payload.SummonRemovePayload;
import net.pixeldreamstudios.summonerlib.network.payload.SummonSyncPayload;
import net.pixeldreamstudios.summonerlib.tracker.ClientSummonTracker;

@Environment(EnvType.CLIENT)
public class SummonerLibClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(SummonSyncPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (payload.isRegistering()) {
                    ClientSummonTracker.registerSummon(payload.entityUuid(), payload.summonType());
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SummonRemovePayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClientSummonTracker.unregisterSummon(payload.entityUuid());
            });
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ClientSummonTracker.clearAll();
        });

        SummonerLib.LOGGER.info("Summoner Lib client initialized!");
    }
}