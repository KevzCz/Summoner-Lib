package net.pixeldreamstudios.summonerlib.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.pixeldreamstudios.summonerlib.manager.SummonManager;

import java.util.function.Consumer;

public class SummonBuilder<T extends Entity> {

    private final PlayerEntity owner;
    private final T entity;
    private final ServerWorld world;
    private String summonType;
    private int lifetimeTicks = -1;
    private boolean allowInteraction = false;
    private boolean persistent = false;
    private Consumer<T> onSpawn;
    private Consumer<T> onExpire;

    private SummonBuilder(PlayerEntity owner, T entity, ServerWorld world) {
        this.owner = owner;
        this.entity = entity;
        this.world = world;
    }

    public static <T extends Entity> SummonBuilder<T> create(PlayerEntity owner, T entity, ServerWorld world) {
        return new SummonBuilder<>(owner, entity, world);
    }

    public SummonBuilder<T> withType(String summonType) {
        this.summonType = summonType;
        return this;
    }

    public SummonBuilder<T> withLifetime(int ticks) {
        this.lifetimeTicks = ticks;
        return this;
    }

    public SummonBuilder<T> withLifetime(double seconds) {
        this.lifetimeTicks = (int) (seconds * 20);
        return this;
    }

    public SummonBuilder<T> allowInteraction(boolean allow) {
        this.allowInteraction = allow;
        return this;
    }

    public SummonBuilder<T> persistent(boolean persist) {
        this.persistent = persist;
        return this;
    }

    public SummonBuilder<T> onSpawn(Consumer<T> callback) {
        this.onSpawn = callback;
        return this;
    }

    public SummonBuilder<T> onExpire(Consumer<T> callback) {
        this.onExpire = callback;
        return this;
    }

    public T build() {
        if (summonType == null) {
            summonType = entity.getType().getTranslationKey();
        }

        if (onSpawn != null) {
            onSpawn.accept(entity);
        }

        boolean spawned = world.spawnEntity(entity);
        if (!spawned) {
            return null;
        }

        SummonManager.registerSummon(
                owner,
                entity,
                world,
                lifetimeTicks,
                allowInteraction,
                summonType,
                persistent
        );

        return entity;
    }
}