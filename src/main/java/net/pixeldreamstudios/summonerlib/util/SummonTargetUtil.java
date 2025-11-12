package net.pixeldreamstudios.summonerlib.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.pixeldreamstudios.summonerlib.data.SummonData;
import net.pixeldreamstudios.summonerlib.tracker.SummonTracker;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Utility class for targeting and manipulating specific summons
 */
public class SummonTargetUtil {

    /**
     * Execute an action on the first summon of a type
     */
    public static void targetFirst(PlayerEntity owner, String summonType, Consumer<Entity> action) {
        UUID firstUuid = SummonTracker.getOldestSummonByType(owner.getUuid(), summonType);
        if (firstUuid != null) {
            SummonData data = SummonTracker.getSummonData(firstUuid);
            if (data != null && data.getEntity() != null) {
                action.accept(data.getEntity());
            }
        }
    }

    /**
     * Execute an action on the last summon of a type
     */
    public static void targetLast(PlayerEntity owner, String summonType, Consumer<Entity> action) {
        UUID lastUuid = SummonTracker.getNewestSummonByType(owner.getUuid(), summonType);
        if (lastUuid != null) {
            SummonData data = SummonTracker.getSummonData(lastUuid);
            if (data != null && data.getEntity() != null) {
                action.accept(data.getEntity());
            }
        }
    }

    /**
     * Execute an action on all summons of a type
     */
    public static void targetAll(PlayerEntity owner, String summonType, Consumer<Entity> action) {
        List<UUID> summons = SummonTracker.getPlayerSummonsByType(owner.getUuid(), summonType);
        for (UUID uuid : summons) {
            SummonData data = SummonTracker.getSummonData(uuid);
            if (data != null && data.getEntity() != null) {
                action.accept(data.getEntity());
            }
        }
    }

    /**
     * Execute an action on a specific summon by index
     */
    public static void targetByIndex(PlayerEntity owner, String summonType, int index, Consumer<Entity> action) {
        UUID summonUuid = SummonTracker.getSummonByTypeAndIndex(owner.getUuid(), summonType, index);
        if (summonUuid != null) {
            SummonData data = SummonTracker.getSummonData(summonUuid);
            if (data != null && data.getEntity() != null) {
                action.accept(data.getEntity());
            }
        }
    }

    /**
     * Execute an action on summons within a group
     */
    public static void targetGroup(PlayerEntity owner, String groupId, Consumer<Entity> action) {
        List<UUID> summons = SummonTracker.getPlayerSummonsByGroup(owner.getUuid(), groupId);
        for (UUID uuid : summons) {
            SummonData data = SummonTracker.getSummonData(uuid);
            if (data != null && data.getEntity() != null) {
                action.accept(data.getEntity());
            }
        }
    }

    /**
     * Execute an action on summons matching a predicate
     */
    public static void targetMatching(PlayerEntity owner, String summonType, Predicate<Entity> predicate, Consumer<Entity> action) {
        List<UUID> summons = SummonTracker.getPlayerSummonsByType(owner.getUuid(), summonType);
        for (UUID uuid : summons) {
            SummonData data = SummonTracker.getSummonData(uuid);
            if (data != null && data.getEntity() != null && predicate.test(data.getEntity())) {
                action.accept(data.getEntity());
            }
        }
    }

    /**
     * Get the closest summon to a position
     */
    public static Entity getClosest(PlayerEntity owner, String summonType, double x, double y, double z) {
        List<UUID> summons = SummonTracker.getPlayerSummonsByType(owner.getUuid(), summonType);
        Entity closest = null;
        double closestDist = Double.MAX_VALUE;

        for (UUID uuid : summons) {
            SummonData data = SummonTracker.getSummonData(uuid);
            if (data != null && data.getEntity() != null) {
                Entity entity = data.getEntity();
                double dist = entity.squaredDistanceTo(x, y, z);
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = entity;
                }
            }
        }

        return closest;
    }

    /**
     * Get the summon with the lowest health percentage
     */
    public static LivingEntity getLowestHealth(PlayerEntity owner, String summonType) {
        List<UUID> summons = SummonTracker.getPlayerSummonsByType(owner.getUuid(), summonType);
        LivingEntity lowest = null;
        float lowestPercent = 1.0f;

        for (UUID uuid : summons) {
            SummonData data = SummonTracker.getSummonData(uuid);
            if (data != null && data.getEntity() instanceof LivingEntity living) {
                float percent = living.getHealth() / living.getMaxHealth();
                if (percent < lowestPercent) {
                    lowestPercent = percent;
                    lowest = living;
                }
            }
        }

        return lowest;
    }

    /**
     * Count summons in a group
     */
    public static int countInGroup(PlayerEntity owner, String groupId) {
        return SummonTracker.getPlayerSummonsByGroup(owner.getUuid(), groupId).size();
    }

    /**
     * Count total slots used by a group
     */
    public static int countSlotsInGroup(PlayerEntity owner, String groupId) {
        List<UUID> summons = SummonTracker.getPlayerSummonsByGroup(owner.getUuid(), groupId);
        int totalSlots = 0;
        for (UUID uuid : summons) {
            SummonData data = SummonTracker.getSummonData(uuid);
            if (data != null) {
                totalSlots += data.slotCost;
            }
        }
        return totalSlots;
    }
}