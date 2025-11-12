package net.pixeldreamstudios.summonerlib.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.pixeldreamstudios.summonerlib.api.ISummonable;
import net.pixeldreamstudios.summonerlib.util.SummonCritUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityOnSummonDoMixin {

    @Inject(method = "damage", at = @At("RETURN"))
    private void onDamageReturn(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (cir.getReturnValue() && self.isDead()) {
            // Check if attacker is a summon
            if (source.getAttacker() instanceof ISummonable summon) {
                summon.onSummonKill(self);
            }

            // Check if victim is a summon
            if (self instanceof ISummonable summon) {
                summon.onSummonDeath(source.getAttacker(), source);
            }
        }

        // Call onSummonHit for summons
        if (source.getAttacker() instanceof ISummonable summon && self != source.getAttacker()) {
            boolean wasCrit = SummonCritUtil.wasLastDamageCrit();
            summon.onSummonHit(self, amount, wasCrit);
        }

        // Call onSummonDamaged for summons
        if (self instanceof ISummonable summon && cir.getReturnValue()) {
            summon.onSummonDamaged(source.getAttacker(), source, amount);
        }
    }
}