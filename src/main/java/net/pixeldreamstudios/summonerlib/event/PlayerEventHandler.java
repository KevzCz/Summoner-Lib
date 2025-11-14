package net.pixeldreamstudios.summonerlib.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.pixeldreamstudios.summonerlib.data.PlayerSummonData;
import net.pixeldreamstudios.summonerlib.tracker.SummonTracker;

/**
 * Handles player connection events
 */
public class PlayerEventHandler {

    public static void register() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            SummonTracker.cleanupPlayer(handler.player.getUuid());
            PlayerSummonData.clearAllSummons(handler.player);
            AttributeChangeHandler.clearPlayerCache(handler.player.getUuid());
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (!alive) {
                SummonTracker.cleanupPlayer(newPlayer.getUuid());
                AttributeChangeHandler.clearPlayerCache(newPlayer.getUuid());
            }
        });
    }
}