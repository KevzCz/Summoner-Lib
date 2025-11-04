package net.pixeldreamstudios.summonerlib.manager;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.pixeldreamstudios.summonerlib.SummonerLib;
import net.pixeldreamstudios.summonerlib.attribute.SummonerAttributes;
import net.pixeldreamstudios.summonerlib.data.PlayerSummonData;
import net.pixeldreamstudios.summonerlib.tracker.SummonTracker;

import java.util.UUID;


public class SummonManager {

    public static void registerSummon(
            PlayerEntity owner,
            Entity entity,
            ServerWorld world,
            int lifetimeTicks,
            boolean allowInteraction,
            String summonType,
            boolean persistent
    ) {
        UUID entityUuid = entity.getUuid();

        // Add to player NBT data
        PlayerSummonData.addSummon(owner, summonType, entityUuid);

        // Register with tracker
        SummonTracker.registerSummon(
                owner,
                entity,
                world.getTime(),
                lifetimeTicks,
                allowInteraction,
                summonType,
                persistent
        );

        SummonerLib.LOGGER.debug("Registered summon: {} of type {} for player {}",
                entityUuid, summonType, owner.getName().getString());
    }

    public static void unregisterSummon(PlayerEntity owner, UUID entityUuid, String summonType) {
        PlayerSummonData.removeSummon(owner, summonType, entityUuid);

        if (owner.getWorld() instanceof ServerWorld serverWorld) {
            SummonTracker.unregisterSummon(serverWorld, entityUuid);
        } else {
            SummonTracker.unregisterSummon(entityUuid);
        }

        SummonerLib.LOGGER.debug("Unregistered summon: {} of type {}", entityUuid, summonType);
    }

    public static boolean isOwnedBy(PlayerEntity owner, String summonType, UUID summonUuid) {
        return PlayerSummonData.hasSummon(owner, summonType, summonUuid);
    }

    public static int getMaxSummons(PlayerEntity player) {
        return (int) player.getAttributeValue(SummonerAttributes.MAX_SUMMONS);
    }

    public static int getSummonCount(PlayerEntity player, String summonType) {
        return SummonTracker.getPlayerSummonCountByType(player.getUuid(), summonType);
    }

    public static void removeOldestSummon(PlayerEntity player, String summonType, ServerWorld world) {
        UUID oldestUuid = SummonTracker.getOldestSummonByType(player.getUuid(), summonType);
        if (oldestUuid != null) {
            var oldData = SummonTracker.getSummonData(oldestUuid);
            if (oldData != null) {
                Entity oldEntity = oldData.getEntity();
                if (oldEntity != null) {
                    oldEntity.discard();
                }
            }
            unregisterSummon(player, oldestUuid, summonType);
        }
    }

    public static void removeAllSummons(PlayerEntity player, String summonType, ServerWorld world) {
        var summonUuids = SummonTracker.getPlayerSummonsByType(player.getUuid(), summonType);
        for (UUID uuid : summonUuids) {
            var data = SummonTracker.getSummonData(uuid);
            if (data != null) {
                Entity entity = data.getEntity();
                if (entity != null) {
                    entity.discard();
                }
            }
            unregisterSummon(player, uuid, summonType);
        }
    }
}