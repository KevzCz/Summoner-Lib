package net.pixeldreamstudios.summonerlib.manager;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.pixeldreamstudios.summonerlib.SummonerLib;
import net.pixeldreamstudios.summonerlib.api.ISummonable;
import net.pixeldreamstudios.summonerlib.attribute.SummonerAttributes;
import net.pixeldreamstudios.summonerlib.data.PlayerSummonData;
import net.pixeldreamstudios.summonerlib.registry.SummonerRegistry;
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
        // Use default values for slotCost and groupId
        int slotCost = 1;
        String groupId = "default";

        // Try to get from ISummonable interface
        if (entity instanceof ISummonable summonable) {
            slotCost = summonable.getSlotCost();
        }

        // Try to get from registered SummonType
        var registeredType = SummonerRegistry.getSummonType(summonType);
        if (registeredType != null) {
            slotCost = registeredType.defaultSlotCost();
            groupId = registeredType.groupId();
        }

        registerSummon(owner, entity, world, lifetimeTicks, allowInteraction, summonType, persistent, slotCost, groupId);
    }

    public static void registerSummon(
            PlayerEntity owner,
            Entity entity,
            ServerWorld world,
            int lifetimeTicks,
            boolean allowInteraction,
            String summonType,
            boolean persistent,
            int slotCost,
            String groupId
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
                persistent,
                slotCost,
                groupId
        );

        SummonerLib.LOGGER.debug("Registered summon: {} of type {} for player {} (slots: {}, group: {})",
                entityUuid, summonType, owner.getName().getString(), slotCost, groupId);
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

    public static int getSummonSlots(PlayerEntity player, String summonType) {
        return SummonTracker.getPlayerSummonSlotsByType(player.getUuid(), summonType);
    }

    public static int getTotalSummonSlots(PlayerEntity player) {
        return SummonTracker.getTotalPlayerSummonSlots(player.getUuid());
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

    public static void removeAllSummonsInGroup(PlayerEntity player, String groupId, ServerWorld world) {
        var summonUuids = SummonTracker.getPlayerSummonsByGroup(player.getUuid(), groupId);
        for (UUID uuid : summonUuids) {
            var data = SummonTracker.getSummonData(uuid);
            if (data != null) {
                Entity entity = data.getEntity();
                if (entity != null) {
                    entity.discard();
                }
                unregisterSummon(player, uuid, data.summonType);
            }
        }
    }

    public static void removeOldestSummonsToFreeSlots(PlayerEntity player, String summonType, int slotsNeeded, ServerWorld world) {
        int freedSlots = 0;

        while (freedSlots < slotsNeeded) {
            UUID oldestUuid = SummonTracker.getOldestSummonByType(player.getUuid(), summonType);

            if (oldestUuid == null) break;

            var oldData = SummonTracker.getSummonData(oldestUuid);

            if (oldData != null) {
                freedSlots += oldData.slotCost;

                Entity oldEntity = oldData.getEntity();
                if (oldEntity != null) {
                    oldEntity.discard();
                }

                unregisterSummon(player, oldestUuid, summonType);
            } else {
                break;
            }
        }

        SummonerLib.LOGGER.debug("Freed {} slots for player {} (needed: {})",
                freedSlots, player.getName().getString(), slotsNeeded);
    }

    public static boolean canSummon(PlayerEntity player, String summonType, int slotCost) {
        int maxSlots = getMaxSummons(player);
        int currentSlots = getSummonSlots(player, summonType);
        return currentSlots + slotCost <= maxSlots;
    }

    public static int getAvailableSlots(PlayerEntity player, String summonType) {
        int maxSlots = getMaxSummons(player);
        int currentSlots = getSummonSlots(player, summonType);
        return Math.max(0, maxSlots - currentSlots);
    }
    public static void enforceLimits(PlayerEntity player, ServerWorld world) {
        net.pixeldreamstudios.summonerlib.util.SummonLimitEnforcer.enforceGlobalLimit(player, world);
    }

    public static boolean wouldExceedLimit(PlayerEntity player, String summonType, int slotCost) {
        int maxSlots = getMaxSummons(player);
        int currentSlots = getTotalSummonSlots(player);
        return currentSlots + slotCost > maxSlots;
    }

}