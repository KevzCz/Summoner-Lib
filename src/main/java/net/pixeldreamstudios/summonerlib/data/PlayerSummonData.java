package net.pixeldreamstudios.summonerlib.data;


import net.minecraft.entity.LivingEntity;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlayerSummonData {

    private static final HashMap<UUID, HashMap<String, List<UUID>>> PLAYER_SUMMONS = new HashMap<>();

    public static void addSummon(LivingEntity player, String summonType, UUID summonUuid) {
        if (!(player instanceof ServerPlayerEntity)) return;

        UUID playerUuid = player.getUuid();

        PLAYER_SUMMONS.computeIfAbsent(playerUuid, k -> new HashMap<>())
                .computeIfAbsent(summonType, k -> new ArrayList<>())
                .add(summonUuid);
    }

    public static void removeSummon(LivingEntity player, String summonType, UUID summonUuid) {
        if (!(player instanceof ServerPlayerEntity)) return;

        UUID playerUuid = player.getUuid();
        var summonTypes = PLAYER_SUMMONS.get(playerUuid);

        if (summonTypes != null) {
            var summons = summonTypes.get(summonType);
            if (summons != null) {
                summons.remove(summonUuid);
                if (summons.isEmpty()) {
                    summonTypes.remove(summonType);
                }
            }
            if (summonTypes.isEmpty()) {
                PLAYER_SUMMONS.remove(playerUuid);
            }
        }
    }

    public static List<UUID> getSummons(LivingEntity player, String summonType) {
        if (!(player instanceof ServerPlayerEntity)) return List.of();

        UUID playerUuid = player.getUuid();
        var summonTypes = PLAYER_SUMMONS.get(playerUuid);

        if (summonTypes == null) return List.of();

        var summons = summonTypes.get(summonType);
        return summons != null ? new ArrayList<>(summons) : List.of();
    }

    public static boolean hasSummon(LivingEntity player, String summonType, UUID summonUuid) {
        return getSummons(player, summonType).contains(summonUuid);
    }

    public static void clearAllSummons(LivingEntity player, String summonType) {
        if (!(player instanceof ServerPlayerEntity)) return;

        UUID playerUuid = player.getUuid();
        var summonTypes = PLAYER_SUMMONS.get(playerUuid);

        if (summonTypes != null) {
            summonTypes.remove(summonType);
            if (summonTypes.isEmpty()) {
                PLAYER_SUMMONS.remove(playerUuid);
            }
        }
    }

    public static void clearAllSummons(LivingEntity player) {
        if (!(player instanceof ServerPlayerEntity)) return;

        UUID playerUuid = player.getUuid();
        PLAYER_SUMMONS.remove(playerUuid);
    }
}