package net.pixeldreamstudios.summonerlib.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.pixeldreamstudios.summonerlib.data.SummonData;
import net.pixeldreamstudios.summonerlib.tracker.SummonTracker;

import java.util.List;
import java.util.UUID;

/**
 * Utility class for controlling summon behavior and targeting
 */
public class SummonControlUtil {

    /**
     * Force a summon to attack a specific target
     */
    public static boolean forceAttackTarget(LivingEntity summon, LivingEntity target) {
        if (!(summon instanceof MobEntity mob)) {
            return false;
        }

        mob.setTarget(target);
        mob.setAttacking(true);

        return true;
    }

    /**
     * Force all summons of a type to attack a target
     */
    public static void forceAllAttackTarget(PlayerEntity owner, String summonType, LivingEntity target) {
        List<UUID> summons = SummonTracker.getPlayerSummonsByType(owner.getUuid(), summonType);

        for (UUID uuid : summons) {
            SummonData data = SummonTracker.getSummonData(uuid);
            if (data != null && data.getEntity() instanceof LivingEntity living) {
                forceAttackTarget(living, target);
            }
        }
    }

    /**
     * Force all summons in a group to attack a target
     */
    public static void forceGroupAttackTarget(PlayerEntity owner, String groupId, LivingEntity target) {
        List<UUID> summons = SummonTracker.getPlayerSummonsByGroup(owner.getUuid(), groupId);

        for (UUID uuid : summons) {
            SummonData data = SummonTracker.getSummonData(uuid);
            if (data != null && data.getEntity() instanceof LivingEntity living) {
                forceAttackTarget(living, target);
            }
        }
    }

    /**
     * Force the first/oldest summon to attack a target
     */
    public static boolean forceFirstAttackTarget(PlayerEntity owner, String summonType, LivingEntity target) {
        UUID firstUuid = SummonTracker.getOldestSummonByType(owner.getUuid(), summonType);
        if (firstUuid != null) {
            SummonData data = SummonTracker.getSummonData(firstUuid);
            if (data != null && data.getEntity() instanceof LivingEntity living) {
                return forceAttackTarget(living, target);
            }
        }
        return false;
    }

    /**
     * Clear the target from a summon
     */
    public static void clearTarget(LivingEntity summon) {
        if (summon instanceof MobEntity mob) {
            mob.setTarget(null);
            mob.setAttacking(false);
        }
    }

    /**
     * Clear targets from all summons of a type
     */
    public static void clearAllTargets(PlayerEntity owner, String summonType) {
        List<UUID> summons = SummonTracker.getPlayerSummonsByType(owner.getUuid(), summonType);

        for (UUID uuid : summons) {
            SummonData data = SummonTracker.getSummonData(uuid);
            if (data != null && data.getEntity() instanceof LivingEntity living) {
                clearTarget(living);
            }
        }
    }
}