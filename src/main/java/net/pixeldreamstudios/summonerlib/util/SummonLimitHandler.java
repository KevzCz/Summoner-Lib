package net.pixeldreamstudios.summonerlib.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.pixeldreamstudios.summonerlib.data.SummonData;
import net.pixeldreamstudios.summonerlib.manager.SummonManager;
import net.pixeldreamstudios.summonerlib.tracker.SummonTracker;

import java.util.UUID;

public class SummonLimitHandler {

    public static boolean handleSummonLimit(PlayerEntity summoner, String summonType) {
        return handleSummonLimit(summoner, summonType, null);
    }

    public static boolean handleSummonLimit(PlayerEntity summoner, String summonType, Runnable onRemoveOldest) {
        int maxSummons = SummonManager.getMaxSummons(summoner);
        int currentCount = SummonTracker.getPlayerSummonCountByType(summoner.getUuid(), summonType);

        if (currentCount >= maxSummons) {
            removeOldestSummon(summoner, summonType, onRemoveOldest);
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

    public static boolean canSummon(PlayerEntity summoner, String summonType) {
        int maxSummons = SummonManager.getMaxSummons(summoner);
        int currentCount = SummonTracker.getPlayerSummonCountByType(summoner.getUuid(), summonType);
        return currentCount < maxSummons;
    }

    public static int getAvailableSummonSlots(PlayerEntity summoner, String summonType) {
        int maxSummons = SummonManager.getMaxSummons(summoner);
        int currentCount = SummonTracker.getPlayerSummonCountByType(summoner.getUuid(), summonType);
        return Math.max(0, maxSummons - currentCount);
    }

    public static int getCurrentSummonCount(PlayerEntity summoner, String summonType) {
        return SummonTracker.getPlayerSummonCountByType(summoner.getUuid(), summonType);
    }
}