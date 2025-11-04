package net.pixeldreamstudios.summonerlib.manager;

import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.pixeldreamstudios.summonerlib.SummonerLib;
import net.pixeldreamstudios.summonerlib.api.ISummonable;
import net.pixeldreamstudios.summonerlib.data.SummonData;


public class SummonLifecycleManager {

    private static final int DEFAULT_WARNING_TICKS = 100;
    private static final int DEFAULT_FLASH_INTERVAL = 20;

    public static void tickSummon(SummonData data, ServerWorld world, long currentTick) {
        Entity entity = data.getEntity();
        if (entity == null) return;

        int age = data.getAge(currentTick);

        if (entity instanceof ISummonable summonable) {
            summonable.onSummonTick(age);

            if (data.shouldShowWarning(currentTick, DEFAULT_WARNING_TICKS)) {
                int remaining = data.getRemainingTicks(currentTick);
                summonable.onSummonExpiring(remaining);
            }
        }

        if (data.shouldShowWarning(currentTick, DEFAULT_WARNING_TICKS)) {
            if (!data.hasShownWarning) {
                spawnWarningParticles(world, entity);
                data.setWarningShown();
                SummonerLib.LOGGER.debug("Showing warning for summon: {}", data.entityUuid);
            }
        }

        if (data.shouldFlash(currentTick, DEFAULT_WARNING_TICKS, DEFAULT_FLASH_INTERVAL)) {
            spawnFlashParticles(world, entity);
        }
    }
    public static void onSummonExpire(SummonData data, ServerWorld world) {
        Entity entity = data.getEntity();
        if (entity == null) return;

        SummonerLib.LOGGER.debug("Summon expired: {} of type {}", data.entityUuid, data.summonType);

        if (entity instanceof ISummonable summonable) {
            summonable.onSummonExpired();
        }

        spawnDespawnParticles(world, entity);
    }

    private static void spawnWarningParticles(ServerWorld world, Entity entity) {
        for (int i = 0; i < 8; i++) {
            double angle = (Math.PI * 2 * i) / 8;
            double offsetX = Math.cos(angle) * 0.5;
            double offsetZ = Math.sin(angle) * 0.5;

            world.spawnParticles(
                    ParticleTypes.FLAME,
                    entity.getX() + offsetX,
                    entity.getY() + 0.5,
                    entity.getZ() + offsetZ,
                    1,
                    0, 0.1, 0,
                    0.01
            );
        }
    }

    private static void spawnFlashParticles(ServerWorld world, Entity entity) {
        world.spawnParticles(
                ParticleTypes.END_ROD,
                entity.getX(),
                entity.getY() + 0.5,
                entity.getZ(),
                3,
                0.3, 0.5, 0.3,
                0.02
        );
    }

    private static void spawnDespawnParticles(ServerWorld world, Entity entity) {
        world.spawnParticles(
                ParticleTypes.POOF,
                entity.getX(),
                entity.getY() + 1.0,
                entity.getZ(),
                30,
                0.5, 0.8, 0.5,
                0.1
        );

        for (int i = 0; i < 15; i++) {
            double angle = (Math.PI * 2 * i) / 15;
            double offsetX = Math.cos(angle) * 0.3;
            double offsetZ = Math.sin(angle) * 0.3;

            world.spawnParticles(
                    ParticleTypes.SOUL,
                    entity.getX() + offsetX,
                    entity.getY() + 0.5,
                    entity.getZ() + offsetZ,
                    1,
                    0, 0.5, 0,
                    0.05
            );
        }

        world.spawnParticles(
                ParticleTypes.ENCHANT,
                entity.getX(),
                entity.getY() + 1.0,
                entity.getZ(),
                20,
                0.5, 0.8, 0.5,
                0.5
        );
    }

    public static void spawnSummonParticles(ServerWorld world, Entity entity) {
        world.spawnParticles(
                ParticleTypes.FIREWORK,
                entity.getX(),
                entity.getY() + 1.0,
                entity.getZ(),
                20,
                0.5, 0.8, 0.5,
                0.2
        );
    }
}