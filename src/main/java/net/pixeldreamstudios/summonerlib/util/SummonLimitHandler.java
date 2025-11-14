package net.pixeldreamstudios.summonerlib.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.pixeldreamstudios.summonerlib.data.SummonData;
import net.pixeldreamstudios.summonerlib.manager.SummonManager;
import net.pixeldreamstudios.summonerlib.registry.SummonerRegistry;
import net.pixeldreamstudios.summonerlib.tracker.SummonTracker;

import java.util.UUID;

public class SummonLimitHandler {

    public static boolean handleSummonLimit(PlayerEntity summoner, String summonType) {
        return handleSummonLimit(summoner, summonType, 1, null);
    }

    public static boolean handleSummonLimit(PlayerEntity summoner, String summonType, int slotCost) {
        return handleSummonLimit(summoner, summonType, slotCost, null);
    }

    public static boolean handleSummonLimit(PlayerEntity summoner, String summonType, Runnable onRemoveOldest) {
        return handleSummonLimit(summoner, summonType, 1, onRemoveOldest);
    }

    public static boolean handleSummonLimit(PlayerEntity summoner, String summonType, int slotCost, Runnable onRemoveOldest) {

        var registeredType = SummonerRegistry.getSummonType(summonType);
        if (registeredType != null && registeredType.maxCount() >= 0) {
            int currentCount = SummonTracker.getPlayerSummonCountByType(summoner.getUuid(), summonType);

            if (currentCount >= registeredType.maxCount()) {
                removeOldestSummon(summoner, summonType, onRemoveOldest);
                return true;
            }

            if (slotCost == 0) {
                return false;
            }
        }

        int maxSummons = SummonManager.getMaxSummons(summoner);
        int currentSlots = SummonTracker.getPlayerSummonSlotsByType(summoner.getUuid(), summonType);

        if (currentSlots + slotCost > maxSummons) {
            int slotsToFree = (currentSlots + slotCost) - maxSummons;
            removeOldestSummonsUntilSlots(summoner, summonType, slotsToFree, onRemoveOldest);
            return true;
        }

        return false;
    }

    public static void removeOldestSummon(PlayerEntity summoner, String summonType) {
        removeOldestSummon(summoner, summonType, null);
    }

    public static void removeOldestSummon(PlayerEntity summoner, String summonType, Runnable callback) {
        UUID oldestUuid = SummonTracker.getOldestSummonByType(summoner.getUuid(), summonType);

        if (oldestUuid != null) {
            SummonData oldData = SummonTracker.getSummonData(oldestUuid);

            if (oldData != null) {
                Entity oldEntity = oldData.getEntity();

                if (oldEntity != null) {
                    oldEntity.discard();
                }
            }

            SummonTracker.unregisterSummon(oldestUuid);

            if (callback != null) {
                callback.run();
            }
        }
    }

    private static void removeOldestSummonsUntilSlots(PlayerEntity summoner, String summonType, int slotsNeeded, Runnable callback) {
        int freedSlots = 0;

        while (freedSlots < slotsNeeded) {
            UUID oldestUuid = SummonTracker.getOldestSummonByType(summoner.getUuid(), summonType);

            if (oldestUuid == null) break;

            SummonData oldData = SummonTracker.getSummonData(oldestUuid);

            if (oldData != null) {
                freedSlots += oldData.slotCost;

                Entity oldEntity = oldData.getEntity();
                if (oldEntity != null) {
                    oldEntity.discard();
                }
            }

            SummonTracker.unregisterSummon(oldestUuid);
        }

        if (callback != null) {
            callback.run();
        }
    }

    public static boolean canSummon(PlayerEntity summoner, String summonType) {
        return canSummon(summoner, summonType, 1);
    }

    public static boolean canSummon(PlayerEntity summoner, String summonType, int slotCost) {
        var registeredType = SummonerRegistry.getSummonType(summonType);
        if (registeredType != null && registeredType.maxCount() >= 0) {
            int currentCount = SummonTracker.getPlayerSummonCountByType(summoner.getUuid(), summonType);

            if (currentCount >= registeredType.maxCount()) {
                return false;
            }

            if (slotCost == 0) {
                return true;
            }
        }

        // Standard slot check
        int maxSummons = SummonManager.getMaxSummons(summoner);
        int currentSlots = SummonTracker.getPlayerSummonSlotsByType(summoner.getUuid(), summonType);
        return currentSlots + slotCost <= maxSummons;
    }

    public static int getAvailableSummonSlots(PlayerEntity summoner, String summonType) {
        int maxSummons = SummonManager.getMaxSummons(summoner);
        int currentSlots = SummonTracker.getPlayerSummonSlotsByType(summoner.getUuid(), summonType);
        return Math.max(0, maxSummons - currentSlots);
    }

    public static int getCurrentSummonCount(PlayerEntity summoner, String summonType) {
        return SummonTracker.getPlayerSummonCountByType(summoner.getUuid(), summonType);
    }

    public static int getCurrentSummonSlots(PlayerEntity summoner, String summonType) {
        return SummonTracker.getPlayerSummonSlotsByType(summoner.getUuid(), summonType);
    }
}