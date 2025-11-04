package net.pixeldreamstudios.summonerlib.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Interface for entities that can be summoned through the Summoner Lib system.
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
     * Get the entity instance
     */
    Entity asEntity();
}