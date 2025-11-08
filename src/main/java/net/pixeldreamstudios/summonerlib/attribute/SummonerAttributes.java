package net.pixeldreamstudios.summonerlib.attribute;

import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.pixeldreamstudios.summonerlib.SummonerLib;

public class SummonerAttributes {

    public static final RegistryEntry<EntityAttribute> MAX_SUMMONS = register(
            "max_summons",
            new ClampedEntityAttribute(
                    "attribute.name.summonerlib.max_summons",
                    3.0,
                    0.0,
                    100.0
            ).setTracked(true)
    );

    public static final RegistryEntry<EntityAttribute> SUMMON_DAMAGE = register(
            "summon_damage",
            new ClampedEntityAttribute(
                    "attribute.name.summonerlib.summon_damage",
                    1.0,
                    0.0,
                    100.0
            ).setTracked(true)
    );

    public static final RegistryEntry<EntityAttribute> SUMMON_HEALTH = register(
            "summon_health",
            new ClampedEntityAttribute(
                    "attribute.name.summonerlib.summon_health",
                    1.0,
                    0.0,
                    100.0
            ).setTracked(true)
    );

    public static final RegistryEntry<EntityAttribute> SUMMON_DURATION = register(
            "summon_duration",
            new ClampedEntityAttribute(
                    "attribute.name.summonerlib.summon_duration",
                    1.0,
                    0.1,
                    10.0
            ).setTracked(true)
    );

    public static final RegistryEntry<EntityAttribute> SUMMON_CRIT_CHANCE = register(
            "summon_crit_chance",
            new ClampedEntityAttribute(
                    "attribute.name.summonerlib.summon_crit_chance",
                    0.0,
                    0.0,
                    1.0
            ).setTracked(true)
    );

    public static final RegistryEntry<EntityAttribute> SUMMON_CRIT_DAMAGE = register(
            "summon_crit_damage",
            new ClampedEntityAttribute(
                    "attribute.name.summonerlib.summon_crit_damage",
                    1.5,
                    1.0,
                    10.0
            ).setTracked(true)
    );

    public static final RegistryEntry<EntityAttribute> SUMMON_ARMOR = register(
            "summon_armor",
            new ClampedEntityAttribute(
                    "attribute.name.summonerlib.summon_armor",
                    0.0,
                    0.0,
                    30.0
            ).setTracked(true)
    );

    private static RegistryEntry<EntityAttribute> register(String name, EntityAttribute attribute) {
        return Registry.registerReference(
                Registries.ATTRIBUTE,
                Identifier.of(SummonerLib.MOD_ID, name),
                attribute
        );
    }

    public static void register() {
        SummonerLib.LOGGER.info("Registering Summoner Attributes...");
    }
}