package net.pixeldreamstudios.summonerlib.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.pixeldreamstudios.summonerlib.data.SummonData;
import net.pixeldreamstudios.summonerlib.tracker.SummonTracker;

public class SummonCritUtil {

    private static boolean lastDamageWasCrit = false;
    private static boolean lastDamageWasMagicCrit = true;

    public static boolean wasLastDamageCrit() {
        return lastDamageWasCrit;
    }

    public static boolean wasLastDamageMagicCrit() {
        return lastDamageWasMagicCrit;
    }

    public static void markCrit(boolean isCrit, boolean isMagicCrit) {
        lastDamageWasCrit = isCrit;
        lastDamageWasMagicCrit = isMagicCrit;
    }

    public static void reset() {
        lastDamageWasCrit = false;
        lastDamageWasMagicCrit = true;
    }

    public static boolean checkAndRollSummonCrit(DamageSource source) {
        Entity sourceEntity = source.getSource();
        Entity attacker = source.getAttacker();

        Entity summonEntity = null;

        if (sourceEntity != null && SummonTracker.isSpellSummon(sourceEntity.getUuid())) {
            summonEntity = sourceEntity;
        } else if (attacker != null && SummonTracker.isSpellSummon(attacker.getUuid())) {
            summonEntity = attacker;
        }

        if (summonEntity == null) {
            return false;
        }

        SummonData summonData = SummonTracker.getSummonData(summonEntity.getUuid());
        if (summonData == null) {
            return false;
        }

        if (!(summonEntity.getWorld() instanceof ServerWorld serverWorld)) {
            return false;
        }

        PlayerEntity owner = serverWorld.getPlayerByUuid(summonData.ownerUuid);
        if (owner == null) {
            return false;
        }

        double critChance = SummonAttributeApplicator.getCritChance(owner);
        return serverWorld.random.nextDouble() < critChance;
    }
}