package net.pixeldreamstudios.summonerlib.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.pixeldreamstudios.summonerlib.data.SummonData;
import net.pixeldreamstudios.summonerlib.manager.SummonManager;
import net.pixeldreamstudios.summonerlib.tracker.SummonTracker;

import java.util.*;
import java.util.stream.Collectors;

public class SummonLimitEnforcer {

    /**
     * Enforce summon limits for a player across all summon types
     * Removes excess summons if current total exceeds new max
     * Priority: highest slot cost first, then oldest
     */
    public static void enforceGlobalLimit(PlayerEntity player, ServerWorld world) {
        int maxSlots = SummonManager.getMaxSummons(player);
        int currentSlots = SummonTracker.getTotalPlayerSummonSlots(player.getUuid());

        if (currentSlots <= maxSlots) {
            return; // Within limit
        }

        int slotsToFree = currentSlots - maxSlots;
        removeExcessSummons(player, world, slotsToFree);
    }

    /**
     * Enforce summon limits for a specific summon type
     */
    public static void enforceTypeLimit(PlayerEntity player, String summonType, ServerWorld world) {
        int maxSlots = SummonManager.getMaxSummons(player);
        int currentSlots = SummonTracker.getPlayerSummonSlotsByType(player.getUuid(), summonType);

        if (currentSlots <= maxSlots) {
            return;
        }

        int slotsToFree = currentSlots - maxSlots;
        removeExcessSummonsForType(player, summonType, world, slotsToFree);
    }

    /**
     * Remove excess summons across all types
     * Priority: highest slot cost first, then oldest within same slot cost
     */
    private static void removeExcessSummons(PlayerEntity player, ServerWorld world, int slotsToFree) {
        List<UUID> allSummons = SummonTracker.getPlayerSummons(player.getUuid());

        // Get all summon data and sort by priority
        List<SummonData> sortedSummons = allSummons.stream()
                .map(SummonTracker::getSummonData)
                .filter(Objects::nonNull)
                .sorted(getRemovalComparator())
                .collect(Collectors.toList());

        int freedSlots = 0;
        for (SummonData data : sortedSummons) {
            if (freedSlots >= slotsToFree) {
                break;
            }

            removeSummon(player, data, world);
            freedSlots += data.slotCost;
        }

    }

    /**
     * Remove excess summons for a specific type
     */
    private static void removeExcessSummonsForType(PlayerEntity player, String summonType, ServerWorld world, int slotsToFree) {
        List<UUID> typeSummons = SummonTracker.getPlayerSummonsByType(player.getUuid(), summonType);

        List<SummonData> sortedSummons = typeSummons.stream()
                .map(SummonTracker::getSummonData)
                .filter(Objects::nonNull)
                .sorted(getRemovalComparator())
                .collect(Collectors.toList());

        int freedSlots = 0;
        for (SummonData data : sortedSummons) {
            if (freedSlots >= slotsToFree) {
                break;
            }

            removeSummon(player, data, world);
            freedSlots += data.slotCost;
        }
    }

    /**
     * Get comparator for removal priority
     * Priority: highest slot cost first (merged summons), then oldest
     */
    private static Comparator<SummonData> getRemovalComparator() {
        return Comparator
                .comparingInt((SummonData data) -> -data.slotCost)
                .thenComparingLong(data -> data.spawnTick);
    }

    /**
     * Remove a single summon
     */
    private static void removeSummon(PlayerEntity player, SummonData data, ServerWorld world) {
        Entity entity = data.getEntity();

        if (entity != null && !entity.isRemoved()) {
            entity.discard();
        }

        SummonManager.unregisterSummon(player, data.entityUuid, data.summonType);

    }

    /**
     * Check if player is over limit and needs enforcement
     */
    public static boolean isOverLimit(PlayerEntity player) {
        int maxSlots = SummonManager.getMaxSummons(player);
        int currentSlots = SummonTracker.getTotalPlayerSummonSlots(player.getUuid());
        return currentSlots > maxSlots;
    }

    /**
     * Get how many slots over the limit a player is
     */
    public static int getSlotsOverLimit(PlayerEntity player) {
        int maxSlots = SummonManager.getMaxSummons(player);
        int currentSlots = SummonTracker.getTotalPlayerSummonSlots(player.getUuid());
        return Math.max(0, currentSlots - maxSlots);
    }

    /**
     * Preview which summons would be removed without actually removing them
     */
    public static List<UUID> previewRemoval(PlayerEntity player) {
        int slotsToFree = getSlotsOverLimit(player);
        if (slotsToFree <= 0) {
            return Collections.emptyList();
        }

        List<UUID> allSummons = SummonTracker.getPlayerSummons(player.getUuid());
        List<SummonData> sortedSummons = allSummons.stream()
                .map(SummonTracker::getSummonData)
                .filter(Objects::nonNull)
                .sorted(getRemovalComparator())
                .collect(Collectors.toList());

        List<UUID> toRemove = new ArrayList<>();
        int freedSlots = 0;

        for (SummonData data : sortedSummons) {
            if (freedSlots >= slotsToFree) {
                break;
            }
            toRemove.add(data.entityUuid);
            freedSlots += data.slotCost;
        }

        return toRemove;
    }
}