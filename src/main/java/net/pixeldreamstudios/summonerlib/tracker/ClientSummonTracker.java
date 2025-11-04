package net.pixeldreamstudios.summonerlib.tracker;

import net.pixeldreamstudios.summonerlib.SummonerLib;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientSummonTracker {

    private static final Map<UUID, String> CLIENT_SUMMONS = new HashMap<>();

    public static void registerSummon(UUID entityUuid, String summonType) {
        CLIENT_SUMMONS.put(entityUuid, summonType);
        SummonerLib.LOGGER.debug("[CLIENT] Registered summon: {} of type {}", entityUuid, summonType);
    }

    public static void unregisterSummon(UUID entityUuid) {
        String type = CLIENT_SUMMONS.remove(entityUuid);
        if (type != null) {
            SummonerLib.LOGGER.debug("[CLIENT] Unregistered summon: {} of type {}", entityUuid, type);
        }
    }

    public static boolean isSpellSummon(UUID entityUuid) {
        return CLIENT_SUMMONS.containsKey(entityUuid);
    }

    public static String getSummonType(UUID entityUuid) {
        return CLIENT_SUMMONS.get(entityUuid);
    }

    public static void clearAll() {
        CLIENT_SUMMONS.clear();
        SummonerLib.LOGGER.debug("[CLIENT] Cleared all summons");
    }

    public static int getCount() {
        return CLIENT_SUMMONS.size();
    }

    public static int getCountByType(String summonType) {
        return (int) CLIENT_SUMMONS.values().stream()
                .filter(type -> type.equals(summonType))
                .count();
    }
}