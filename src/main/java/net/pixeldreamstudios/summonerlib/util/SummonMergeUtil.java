package net.pixeldreamstudios.summonerlib.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.pixeldreamstudios.summonerlib.data.SummonData;
import net.pixeldreamstudios.summonerlib.manager.SummonManager;
import net.pixeldreamstudios.summonerlib.tracker.SummonTracker;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

    public class SummonMergeUtil {

    /**
     * Merge all summons of a type into the oldest one
     * @return The merged summon entity, or null if no summons
     */
    public static <T extends Entity> T mergeAllIntoOldest(
            PlayerEntity owner,
            String summonType,
            ServerWorld world,
            BiConsumer<T, Integer> onMerge) {

        List<UUID> summons = SummonTracker.getPlayerSummonsByType(owner.getUuid(), summonType);

        if (summons.isEmpty()) {
            return null;
        }

        if (summons.size() == 1) {
            UUID uuid = summons.get(0);
            SummonData data = SummonTracker.getSummonData(uuid);
            return data != null ? (T) data.getEntity() : null;
        }

        UUID oldestUuid = SummonTracker.getOldestSummonByType(owner.getUuid(), summonType);
        SummonData oldestData = SummonTracker.getSummonData(oldestUuid);

        if (oldestData == null) {
            return null;
        }

        T keepEntity = (T) oldestData.getEntity();
        int totalSlots = oldestData.slotCost;
        int totalCount = 1;

        for (UUID uuid : summons) {
            if (uuid.equals(oldestUuid)) {
                continue;
            }

            SummonData data = SummonTracker.getSummonData(uuid);
            if (data != null) {
                totalSlots += data.slotCost;
                totalCount++;

                Entity entity = data.getEntity();
                if (entity != null) {
                    entity.discard();
                }

                SummonManager.unregisterSummon(owner, uuid, summonType);
            }
        }

        if (keepEntity != null) {

            SummonManager.unregisterSummon(owner, oldestUuid, summonType);


            SummonManager.registerSummon(
                    owner,
                    keepEntity,
                    world,
                    oldestData.lifetimeTicks,
                    oldestData.allowInteraction,
                    summonType,
                    oldestData.persistent,
                    totalSlots,
                    oldestData.groupId
            );

            if (onMerge != null) {
                onMerge.accept(keepEntity, totalSlots);
            }
        }

        return keepEntity;
    }

    /**
     * Merge the newest summon into the oldest
     * @return The merged summon, or null
     */
    public static <T extends Entity> T mergeNewestIntoOldest(
            PlayerEntity owner,
            String summonType,
            ServerWorld world,
            BiConsumer<T, Integer> onMerge) {

        UUID oldestUuid = SummonTracker.getOldestSummonByType(owner.getUuid(), summonType);
        UUID newestUuid = SummonTracker.getNewestSummonByType(owner.getUuid(), summonType);

        if (oldestUuid == null || newestUuid == null || oldestUuid.equals(newestUuid)) {
            return null;
        }

        SummonData oldestData = SummonTracker.getSummonData(oldestUuid);
        SummonData newestData = SummonTracker.getSummonData(newestUuid);

        if (oldestData == null || newestData == null) {
            return null;
        }

        T keepEntity = (T) oldestData.getEntity();
        Entity removeEntity = newestData.getEntity();

        int newSlotCost = oldestData.slotCost + newestData.slotCost;

        if (removeEntity != null) {
            removeEntity.discard();
        }
        SummonManager.unregisterSummon(owner, newestUuid, summonType);

        SummonManager.unregisterSummon(owner, oldestUuid, summonType);
        SummonManager.registerSummon(
                owner,
                keepEntity,
                world,
                oldestData.lifetimeTicks,
                oldestData.allowInteraction,
                summonType,
                oldestData.persistent,
                newSlotCost,
                oldestData.groupId
        );

        if (onMerge != null) {
            onMerge.accept(keepEntity, newSlotCost);
        }

        return keepEntity;
    }

    /**
     * Get the total "power level" of all summons (sum of slot costs)
     */
    public static int getTotalPower(PlayerEntity owner, String summonType) {
        return SummonTracker.getPlayerSummonSlotsByType(owner.getUuid(), summonType);
    }
}