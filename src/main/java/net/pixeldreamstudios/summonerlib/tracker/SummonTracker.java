package net.pixeldreamstudios.summonerlib.tracker;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.pixeldreamstudios.summonerlib.SummonerLib;
import net.pixeldreamstudios.summonerlib.data.SummonData;
import net.pixeldreamstudios.summonerlib.manager.SummonLifecycleManager;
import net.pixeldreamstudios.summonerlib.network.payload.SummonRemovePayload;
import net.pixeldreamstudios.summonerlib.network.payload.SummonSyncPayload;

import java.util.*;

public class SummonTracker {

    private static final Map<UUID, List<SummonData>> PLAYER_SUMMONS = new HashMap<>();
    private static final Map<UUID, SummonData> ENTITY_LOOKUP = new HashMap<>();

    public static void registerSummon(
            PlayerEntity owner,
            Entity entity,
            long spawnTick,
            int lifetimeTicks,
            boolean allowInteraction,
            String summonType,
            boolean persistent
    ) {
        UUID ownerUuid = owner.getUuid();
        UUID entityUuid = entity.getUuid();

        List<SummonData> playerSummons = PLAYER_SUMMONS.computeIfAbsent(ownerUuid, k -> new ArrayList<>());
        int summonIndex = (int) playerSummons.stream().filter(s -> s.summonType.equals(summonType)).count();

        SummonData data = new SummonData(
                entityUuid,
                ownerUuid,
                spawnTick,
                lifetimeTicks,
                allowInteraction,
                summonType,
                entity,
                summonIndex,
                persistent
        );

        playerSummons.add(data);
        ENTITY_LOOKUP.put(entityUuid, data);

        if (entity.getWorld() instanceof ServerWorld serverWorld) {
            syncToClients(serverWorld, entityUuid, summonType, true);
        }

    }

    public static void unregisterSummon(UUID entityUuid) {
        SummonData data = ENTITY_LOOKUP.remove(entityUuid);
        if (data != null) {
            List<SummonData> playerSummons = PLAYER_SUMMONS.get(data.ownerUuid);
            if (playerSummons != null) {
                playerSummons.removeIf(s -> s.entityUuid.equals(entityUuid));
                if (playerSummons.isEmpty()) {
                    PLAYER_SUMMONS.remove(data.ownerUuid);
                } else {
                    reindexSummons(playerSummons, data.summonType);
                }
            }
        }
    }

    private static void reindexSummons(List<SummonData> summons, String summonType) {
        List<SummonData> typedSummons = summons.stream()
                .filter(s -> s.summonType.equals(summonType))
                .sorted(Comparator.comparingLong(s -> s.spawnTick))
                .toList();

        for (int i = 0; i < typedSummons.size(); i++) {
            SummonData oldData = typedSummons.get(i);
            SummonData newData = new SummonData(
                    oldData.entityUuid,
                    oldData.ownerUuid,
                    oldData.spawnTick,
                    oldData.lifetimeTicks,
                    oldData.allowInteraction,
                    oldData.summonType,
                    oldData.entityRef,
                    i,
                    oldData.persistent
            );
            summons.remove(oldData);
            summons.add(newData);
            ENTITY_LOOKUP.put(oldData.entityUuid, newData);
        }
    }

    public static void unregisterSummon(ServerWorld world, UUID entityUuid) {
        unregisterSummon(entityUuid);
        syncRemoveToClients(world, entityUuid);
    }

    private static void syncToClients(ServerWorld world, UUID entityUuid, String summonType, boolean isRegistering) {
        SummonSyncPayload payload = new SummonSyncPayload(entityUuid, summonType, isRegistering);
        for (ServerPlayerEntity player : world.getPlayers()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    private static void syncRemoveToClients(ServerWorld world, UUID entityUuid) {
        SummonRemovePayload payload = new SummonRemovePayload(entityUuid);
        for (ServerPlayerEntity player : world.getPlayers()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static boolean isSpellSummon(UUID entityUuid) {
        return ENTITY_LOOKUP.containsKey(entityUuid);
    }

    public static SummonData getSummonData(UUID entityUuid) {
        return ENTITY_LOOKUP.get(entityUuid);
    }

    public static boolean canInteract(UUID entityUuid) {
        SummonData data = ENTITY_LOOKUP.get(entityUuid);
        return data != null && data.allowInteraction;
    }

    public static int getPlayerSummonCount(UUID playerUuid) {
        List<SummonData> summons = PLAYER_SUMMONS.get(playerUuid);
        return summons != null ? summons.size() : 0;
    }

    public static int getPlayerSummonCountByType(UUID playerUuid, String summonType) {
        List<SummonData> summons = PLAYER_SUMMONS.get(playerUuid);
        if (summons == null) return 0;
        return (int) summons.stream().filter(s -> s.summonType.equals(summonType)).count();
    }

    public static List<UUID> getPlayerSummons(UUID playerUuid) {
        List<SummonData> summons = PLAYER_SUMMONS.get(playerUuid);
        if (summons == null) return Collections.emptyList();
        return summons.stream().map(s -> s.entityUuid).toList();
    }

    public static List<UUID> getPlayerSummonsByType(UUID playerUuid, String summonType) {
        List<SummonData> summons = PLAYER_SUMMONS.get(playerUuid);
        if (summons == null) return Collections.emptyList();
        return summons.stream()
                .filter(s -> s.summonType.equals(summonType))
                .map(s -> s.entityUuid)
                .toList();
    }

    public static UUID getOldestSummonByType(UUID playerUuid, String summonType) {
        List<SummonData> summons = PLAYER_SUMMONS.get(playerUuid);
        if (summons == null) return null;

        return summons.stream()
                .filter(s -> s.summonType.equals(summonType))
                .min(Comparator.comparingLong(s -> s.spawnTick))
                .map(s -> s.entityUuid)
                .orElse(null);
    }

    public static void tick(ServerWorld world) {
        long currentTick = world.getTime();
        List<UUID> playersToRemove = new ArrayList<>();

        PLAYER_SUMMONS.forEach((ownerUuid, summons) -> {
            PlayerEntity owner = null;
            for (ServerWorld serverWorld : world.getServer().getWorlds()) {
                owner = serverWorld.getPlayerByUuid(ownerUuid);
                if (owner != null) break;
            }

            PlayerEntity finalOwner = owner;

            summons.removeIf(data -> {
                Entity entity = data.getEntity();

                if (entity == null || entity.isRemoved()) {
                    ENTITY_LOOKUP.remove(data.entityUuid);
                    if (entity != null && entity.getWorld() instanceof ServerWorld sw) {
                        syncRemoveToClients(sw, data.entityUuid);
                    } else {
                        syncRemoveToClients(world, data.entityUuid);
                    }
                    return true;
                }

                if (!entity.isAlive()) {
                    ENTITY_LOOKUP.remove(data.entityUuid);
                    if (entity.getWorld() instanceof ServerWorld sw) {
                        syncRemoveToClients(sw, data.entityUuid);
                    }
                    return true;
                }

                if (finalOwner == null) {
                    if (entity.getWorld() instanceof ServerWorld sw) {
                        SummonLifecycleManager.onSummonExpire(data, sw);
                    }
                    entity.discard();
                    ENTITY_LOOKUP.remove(data.entityUuid);
                    if (entity.getWorld() instanceof ServerWorld sw) {
                        syncRemoveToClients(sw, data.entityUuid);
                    }
                    return true;
                }

                if (!finalOwner.isAlive()) {
                    if (entity.getWorld() instanceof ServerWorld sw) {
                        SummonLifecycleManager.onSummonExpire(data, sw);
                    }
                    entity.discard();
                    ENTITY_LOOKUP.remove(data.entityUuid);
                    if (entity.getWorld() instanceof ServerWorld sw) {
                        syncRemoveToClients(sw, data.entityUuid);
                    }
                    return true;
                }

                if (entity.getWorld() instanceof ServerWorld sw) {
                    SummonLifecycleManager.tickSummon(data, sw, currentTick);
                }

                if (data.isExpired(currentTick)) {
                    if (entity.getWorld() instanceof ServerWorld sw) {
                        SummonLifecycleManager.onSummonExpire(data, sw);
                    }
                    entity.discard();
                    ENTITY_LOOKUP.remove(data.entityUuid);
                    if (entity.getWorld() instanceof ServerWorld sw) {
                        syncRemoveToClients(sw, data.entityUuid);
                    }
                    return true;
                }

                return false;
            });

            if (summons.isEmpty()) {
                playersToRemove.add(ownerUuid);
            }
        });

        playersToRemove.forEach(PLAYER_SUMMONS::remove);
    }

    public static void cleanupPlayer(UUID playerUuid) {
        List<SummonData> summons = PLAYER_SUMMONS.remove(playerUuid);
        if (summons != null) {
            for (SummonData data : summons) {
                Entity entity = data.getEntity();
                if (entity != null && !entity.isRemoved() && entity.getWorld() instanceof ServerWorld sw) {
                    SummonLifecycleManager.onSummonExpire(data, sw);
                    entity.discard();
                    syncRemoveToClients(sw, data.entityUuid);
                }
                ENTITY_LOOKUP.remove(data.entityUuid);
            }
        }
    }
}