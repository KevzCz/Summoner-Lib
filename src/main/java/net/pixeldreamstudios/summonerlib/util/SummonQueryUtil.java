package net.pixeldreamstudios.summonerlib.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.pixeldreamstudios.summonerlib.data.SummonData;
import net.pixeldreamstudios.summonerlib.tracker.SummonTracker;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility for querying and analyzing player summons
 */
public class SummonQueryUtil {

    /**
     * Get all unique summon types the player currently has
     * @return Set of summon type strings
     */
    public static Set<String> getUniqueSummonTypes(PlayerEntity player) {
        List<UUID> allSummons = SummonTracker.getPlayerSummons(player.getUuid());
        return allSummons.stream()
                .map(SummonTracker::getSummonData)
                .filter(Objects::nonNull)
                .map(data -> data.summonType)
                .collect(Collectors.toSet());
    }

    /**
     * Get all unique groups the player has summons in
     * @return Set of group IDs
     */
    public static Set<String> getUniqueGroups(PlayerEntity player) {
        List<UUID> allSummons = SummonTracker.getPlayerSummons(player.getUuid());
        return allSummons.stream()
                .map(SummonTracker::getSummonData)
                .filter(Objects::nonNull)
                .map(data -> data.groupId)
                .collect(Collectors.toSet());
    }

    /**
     * Get one entity of each unique summon type
     * @return Map of summon type -> entity instance
     */
    public static Map<String, Entity> getOneOfEachType(PlayerEntity player) {
        List<UUID> allSummons = SummonTracker.getPlayerSummons(player.getUuid());
        Map<String, Entity> uniqueSummons = new HashMap<>();

        for (UUID uuid : allSummons) {
            SummonData data = SummonTracker.getSummonData(uuid);
            if (data != null && !uniqueSummons.containsKey(data.summonType)) {
                Entity entity = data.getEntity();
                if (entity != null) {
                    uniqueSummons.put(data.summonType, entity);
                }
            }
        }

        return uniqueSummons;
    }

    /**
     * Get count of each summon type
     * @return Map of summon type -> count
     */
    public static Map<String, Integer> getSummonTypeCounts(PlayerEntity player) {
        List<UUID> allSummons = SummonTracker.getPlayerSummons(player.getUuid());
        Map<String, Integer> counts = new HashMap<>();

        for (UUID uuid : allSummons) {
            SummonData data = SummonTracker.getSummonData(uuid);
            if (data != null) {
                counts.merge(data.summonType, 1, Integer::sum);
            }
        }

        return counts;
    }

    /**
     * Get total slots used by each summon type
     * @return Map of summon type -> total slots
     */
    public static Map<String, Integer> getSummonTypeSlots(PlayerEntity player) {
        List<UUID> allSummons = SummonTracker.getPlayerSummons(player.getUuid());
        Map<String, Integer> slots = new HashMap<>();

        for (UUID uuid : allSummons) {
            SummonData data = SummonTracker.getSummonData(uuid);
            if (data != null) {
                slots.merge(data.summonType, data.slotCost, Integer::sum);
            }
        }

        return slots;
    }

    /**
     * Get all entities of a specific entity class type
     */
    public static <T extends Entity> List<T> getSummonsByClass(PlayerEntity player, Class<T> entityClass) {
        List<UUID> allSummons = SummonTracker.getPlayerSummons(player.getUuid());
        List<T> results = new ArrayList<>();

        for (UUID uuid : allSummons) {
            SummonData data = SummonTracker.getSummonData(uuid);
            if (data != null) {
                Entity entity = data.getEntity();
                if (entityClass.isInstance(entity)) {
                    results.add(entityClass.cast(entity));
                }
            }
        }

        return results;
    }

    /**
     * Get the most common summon type
     */
    public static String getMostCommonType(PlayerEntity player) {
        return getSummonTypeCounts(player).entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Get the summon type using the most slots
     */
    public static String getHighestSlotType(PlayerEntity player) {
        return getSummonTypeSlots(player).entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Check if player has any summons of a specific type
     */
    public static boolean hasAnyOfType(PlayerEntity player, String summonType) {
        return SummonTracker.getPlayerSummonCountByType(player.getUuid(), summonType) > 0;
    }

    /**
     * Check if player has multiple different summon types active
     */
    public static boolean hasMixedSummons(PlayerEntity player) {
        return getUniqueSummonTypes(player).size() > 1;
    }

    /**
     * Get total count of all summons regardless of type
     */
    public static int getTotalSummonCount(PlayerEntity player) {
        return SummonTracker.getPlayerSummonCount(player.getUuid());
    }

    /**
     * Get all summon data grouped by type
     */
    public static Map<String, List<SummonData>> getSummonsGroupedByType(PlayerEntity player) {
        List<UUID> allSummons = SummonTracker.getPlayerSummons(player.getUuid());
        Map<String, List<SummonData>> grouped = new HashMap<>();

        for (UUID uuid : allSummons) {
            SummonData data = SummonTracker.getSummonData(uuid);
            if (data != null) {
                grouped.computeIfAbsent(data.summonType, k -> new ArrayList<>()).add(data);
            }
        }

        return grouped;
    }

    /**
     * Get all summon data grouped by group ID
     */
    public static Map<String, List<SummonData>> getSummonsGroupedByGroupId(PlayerEntity player) {
        List<UUID> allSummons = SummonTracker.getPlayerSummons(player.getUuid());
        Map<String, List<SummonData>> grouped = new HashMap<>();

        for (UUID uuid : allSummons) {
            SummonData data = SummonTracker.getSummonData(uuid);
            if (data != null) {
                grouped.computeIfAbsent(data.groupId, k -> new ArrayList<>()).add(data);
            }
        }

        return grouped;
    }

    /**
     * Get average lifetime progress across all summons of a type
     */
    public static float getAverageLifetimeProgress(PlayerEntity player, String summonType, long currentTick) {
        List<UUID> summons = SummonTracker.getPlayerSummonsByType(player.getUuid(), summonType);
        if (summons.isEmpty()) return 0f;

        float totalProgress = 0f;
        int count = 0;

        for (UUID uuid : summons) {
            SummonData data = SummonTracker.getSummonData(uuid);
            if (data != null) {
                totalProgress += data.getLifetimeProgress(currentTick);
                count++;
            }
        }

        return count > 0 ? totalProgress / count : 0f;
    }

    /**
     * Get the oldest summon across all types
     */
    public static UUID getOldestSummonOverall(PlayerEntity player) {
        List<UUID> allSummons = SummonTracker.getPlayerSummons(player.getUuid());

        return allSummons.stream()
                .map(SummonTracker::getSummonData)
                .filter(Objects::nonNull)
                .min(Comparator.comparingLong(data -> data.spawnTick))
                .map(data -> data.entityUuid)
                .orElse(null);
    }

    /**
     * Get the newest summon across all types
     */
    public static UUID getNewestSummonOverall(PlayerEntity player) {
        List<UUID> allSummons = SummonTracker.getPlayerSummons(player.getUuid());

        return allSummons.stream()
                .map(SummonTracker::getSummonData)
                .filter(Objects::nonNull)
                .max(Comparator.comparingLong(data -> data.spawnTick))
                .map(data -> data.entityUuid)
                .orElse(null);
    }
}