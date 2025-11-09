package net.pixeldreamstudios.summonerlib.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.pixeldreamstudios.summonerlib.tracker.SummonTracker;
import net.pixeldreamstudios.summonerlib.data.SummonData;
import net.pixeldreamstudios.summonerlib.util.SummonAttributeApplicator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @ModifyVariable(
            method = "damage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"
            ),
            ordinal = 0
    )
    private float applySummonCritDamage(float amount, DamageSource source) {
        Entity attacker = source.getAttacker();

        if (attacker == null) {
            return amount;
        }

        if (!SummonTracker.isSpellSummon(attacker.getUuid())) {
            return amount;
        }

        SummonData summonData = SummonTracker.getSummonData(attacker.getUuid());
        if (summonData == null) {
            return amount;
        }

        if (!(attacker.getWorld() instanceof ServerWorld serverWorld)) {
            return amount;
        }

        PlayerEntity owner = serverWorld.getPlayerByUuid(summonData.ownerUuid);
        if (owner == null) {
            return amount;
        }

        double critChance = SummonAttributeApplicator.getCritChance(owner);

        if (serverWorld.random.nextDouble() < critChance) {
            double critMultiplier = SummonAttributeApplicator.getCritDamage(owner);
            float newAmount = (float) (amount * critMultiplier);

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