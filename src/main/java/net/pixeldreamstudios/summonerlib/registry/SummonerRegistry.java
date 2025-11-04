package net.pixeldreamstudios.summonerlib.registry;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.pixeldreamstudios.summonerlib.SummonerLib;
import net.pixeldreamstudios.summonerlib.api.SummonType;
import net.pixeldreamstudios.summonerlib.attribute.SummonerAttributes;

import java.util.HashMap;
import java.util.Map;

public class SummonerRegistry {

    private static final Map<String, SummonType> SUMMON_TYPES = new HashMap<>();

    public static void registerSummonType(SummonType summonType) {
        SUMMON_TYPES.put(summonType.id().toString(), summonType);
    }


    public static SummonType getSummonType(String id) {
        return SUMMON_TYPES.get(id);
    }
    public static boolean hasSummonType(String id) {
        return SUMMON_TYPES.containsKey(id);
    }

    public static Map<String, SummonType> getAllSummonTypes() {
        return Map.copyOf(SUMMON_TYPES);
    }

    public static void addMaxSummonsModifier(PlayerEntity player, String name, double value, EntityAttributeModifier.Operation operation) {
        var instance = player.getAttributeInstance(SummonerAttributes.MAX_SUMMONS);
        if (instance != null) {
            Identifier modifierId = Identifier.of(SummonerLib.MOD_ID, "max_summons_" + name.toLowerCase().replace(" ", "_"));
            instance.removeModifier(modifierId);
            instance.addTemporaryModifier(
                    new EntityAttributeModifier(modifierId, value, operation)
            );
        }
    }


    public static void addSummonDamageModifier(PlayerEntity player, String name, double value, EntityAttributeModifier.Operation operation) {
        var instance = player.getAttributeInstance(SummonerAttributes.SUMMON_DAMAGE);
        if (instance != null) {
            Identifier modifierId = Identifier.of(SummonerLib.MOD_ID, "summon_damage_" + name.toLowerCase().replace(" ", "_"));
            instance.removeModifier(modifierId);
            instance.addTemporaryModifier(
                    new EntityAttributeModifier(modifierId, value, operation)
            );
        }
    }

    public static void addSummonHealthModifier(PlayerEntity player, String name, double value, EntityAttributeModifier.Operation operation) {
        var instance = player.getAttributeInstance(SummonerAttributes.SUMMON_HEALTH);
        if (instance != null) {
            Identifier modifierId = Identifier.of(SummonerLib.MOD_ID, "summon_health_" + name.toLowerCase().replace(" ", "_"));
            instance.removeModifier(modifierId);
            instance.addTemporaryModifier(
                    new EntityAttributeModifier(modifierId, value, operation)
            );
        }
    }


    public static void addSummonDurationModifier(PlayerEntity player, String name, double value, EntityAttributeModifier.Operation operation) {
        var instance = player.getAttributeInstance(SummonerAttributes.SUMMON_DURATION);
        if (instance != null) {
            Identifier modifierId = Identifier.of(SummonerLib.MOD_ID, "summon_duration_" + name.toLowerCase().replace(" ", "_"));
            instance.removeModifier(modifierId);
            instance.addTemporaryModifier(
                    new EntityAttributeModifier(modifierId, value, operation)
            );
        }
    }

    public static void register() {
        SummonerLib.LOGGER.info("Summoner Registry initialized");
    }
}