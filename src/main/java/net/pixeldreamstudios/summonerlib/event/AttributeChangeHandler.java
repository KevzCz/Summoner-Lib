package net.pixeldreamstudios.summonerlib.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.pixeldreamstudios.summonerlib.SummonerLib;
import net.pixeldreamstudios.summonerlib.manager.SummonManager;
import net.pixeldreamstudios.summonerlib.util.SummonLimitEnforcer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Monitors attribute changes and enforces summon limits
 */
public class AttributeChangeHandler {

    private static final Map<UUID, Integer> PLAYER_MAX_SUMMONS_CACHE = new HashMap<>();
    private static final int CHECK_INTERVAL = 20; // Check every second
    private static int tickCounter = 0;

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(AttributeChangeHandler::tick);
        SummonerLib.LOGGER.info("Registered Attribute Change Handler");
    }

    private static void tick(ServerWorld world) {
        tickCounter++;

        if (tickCounter % CHECK_INTERVAL != 0) {
            return;
        }

        for (var player : world.getPlayers()) {
            checkPlayerLimits(player, world);
        }
    }

    private static void checkPlayerLimits(PlayerEntity player, ServerWorld world) {
        UUID playerUuid = player.getUuid();
        int currentMaxSummons = SummonManager.getMaxSummons(player);

        Integer cachedMax = PLAYER_MAX_SUMMONS_CACHE.get(playerUuid);

        // First time seeing this player or max changed
        if (cachedMax == null || cachedMax != currentMaxSummons) {
            if (cachedMax != null && cachedMax > currentMaxSummons) {
                // Max decreased - enforce limits
                SummonerLib.LOGGER.debug("Player {} max summons decreased from {} to {}",
                        player.getName().getString(), cachedMax, currentMaxSummons);

                SummonLimitEnforcer.enforceGlobalLimit(player, world);
            }

            PLAYER_MAX_SUMMONS_CACHE.put(playerUuid, currentMaxSummons);
        }
    }

    /**
     * Clear cache for a player (called on disconnect)
     */
    public static void clearPlayerCache(UUID playerUuid) {
        PLAYER_MAX_SUMMONS_CACHE.remove(playerUuid);
    }

    /**
     * Force immediate check for a player
     */
    public static void forceCheck(PlayerEntity player, ServerWorld world) {
        PLAYER_MAX_SUMMONS_CACHE.remove(player.getUuid());
        checkPlayerLimits(player, world);
    }

    /**
     * Get cached max for testing
     */
    public static Integer getCachedMax(UUID playerUuid) {
        return PLAYER_MAX_SUMMONS_CACHE.get(playerUuid);
    }
}