package net.pixeldreamstudios.summonerlib.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.pixeldreamstudios.summonerlib.compat.RPGSystemsCritCompat;
import net.pixeldreamstudios.summonerlib.data.SummonData;
import net.pixeldreamstudios.summonerlib.tracker.SummonTracker;
import net.pixeldreamstudios.summonerlib.util.SummonAttributeApplicator;
import net.pixeldreamstudios.summonerlib.util.SummonCritUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @ModifyVariable(
            method = "damage",
            at = @At("HEAD"),
            argsOnly = true
    )
    private float applySummonCritDamage(float amount, DamageSource source) {
        SummonCritUtil.reset();

        Entity attacker = source.getAttacker();
        Entity sourceEntity = source.getSource();

        Entity summonEntity = null;

        if (sourceEntity != null && SummonTracker.isSpellSummon(sourceEntity.getUuid())) {
            summonEntity = sourceEntity;
        }
        else if (attacker != null && SummonTracker.isSpellSummon(attacker.getUuid())) {
            summonEntity = attacker;
        }

        if (summonEntity == null) {
            return amount;
        }

        SummonData summonData = SummonTracker.getSummonData(summonEntity.getUuid());
        if (summonData == null) {
            return amount;
        }

        if (!(summonEntity.getWorld() instanceof ServerWorld serverWorld)) {
            return amount;
        }

        PlayerEntity owner = serverWorld.getPlayerByUuid(summonData.ownerUuid);
        if (owner == null) {
            return amount;
        }

        double critChance = SummonAttributeApplicator.getCritChance(owner);
        double critDamage = SummonAttributeApplicator.getCritDamage(owner);

        double roll = serverWorld.random.nextDouble();

        if (roll < critChance) {
            float newAmount = (float) (amount * critDamage);

            SummonCritUtil.markCrit(true, true);

            if (FabricLoader.getInstance().isModLoaded("rpg-systems")) {
                RPGSystemsCritCompat.markSummonCrit(source);
            }

            serverWorld.spawnParticles(
                    net.minecraft.particle.ParticleTypes.CRIT,
                    ((LivingEntity)(Object)this).getX(),
                    ((LivingEntity)(Object)this).getY() + ((LivingEntity)(Object)this).getHeight() / 2,
                    ((LivingEntity)(Object)this).getZ(),
                    15,
                    0.3, 0.3, 0.3,
                    0.1
            );

            return newAmount;
        }

        return amount;
    }
}