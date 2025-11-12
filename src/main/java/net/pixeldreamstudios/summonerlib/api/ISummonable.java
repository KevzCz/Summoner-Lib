package net.pixeldreamstudios.summonerlib.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Extended interface for entities that can be summoned through the Summoner Lib system.
 * Implement this on your entity classes to integrate with the summoning framework.
 */
public interface ISummonable {

    /**
     * Called when the summon is first created
     * @param summoner The player who summoned this entity
     */
    default void onSummoned(PlayerEntity summoner) {}

    /**
     * Called every tick while the summon is active
     * @param ticksAlive How many ticks this summon has been alive
     */
    default void onSummonTick(int ticksAlive) {}

    /**
     * Called when the summon is about to expire
     * @param ticksRemaining How many ticks until expiration
     */
    default void onSummonExpiring(int ticksRemaining) {}

    /**
     * Called when the summon expires naturally (not killed)
     */
    default void onSummonExpired() {}

    /**
     * Called when the summon successfully hits a target
     * @param target The entity that was hit
     * @param damage The damage dealt
     * @param wasCritical Whether the hit was a critical strike
     */
    default void onSummonHit(LivingEntity target, float damage, boolean wasCritical) {}

    /**
     * Called when the summon is hit by another entity
     * @param attacker The entity that attacked this summon
     * @param source The damage source
     * @param damage The damage taken
     */
    default void onSummonDamaged(Entity attacker, DamageSource source, float damage) {}

    /**
     * Called when the summon dies (killed, not expired)
     * @param killer The entity that killed this summon (can be null)
     * @param source The damage source that caused death
     */
    default void onSummonDeath(Entity killer, DamageSource source) {}

    /**
     * Called when the summon kills another entity
     * @param victim The entity that was killed
     */
    default void onSummonKill(LivingEntity victim) {}

    /**
     * Called when the summon's owner changes or is updated
     * @param newOwner The new owner
     * @param oldOwner The previous owner (can be null)
     */
    default void onOwnerChanged(PlayerEntity newOwner, PlayerEntity oldOwner) {}

    /**
     * Get the summon type identifier
     */
    String getSummonType();

    /**
     * Whether this summon should persist when the owner logs out
     */
    default boolean shouldPersist() {
        return false;
    }

    /**
     * Whether this summon can be interacted with by players
     */
    default boolean allowInteraction() {
        return false;
    }

    /**
     * How many summon slots this entity occupies
     * @return The number of slots (default 1)
     */
    default int getSlotCost() {
        return 1;
    }

    /**
     * Get the entity instance
     */
    Entity asEntity();
}