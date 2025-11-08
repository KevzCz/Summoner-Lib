package net.pixeldreamstudios.summonerlib.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.pixeldreamstudios.summonerlib.attribute.SummonerAttributes;
import net.spell_power.api.SpellPower;
import net.spell_power.api.SpellSchool;

public class SummonAttributeApplicator {

    private static final int BASE_LIFETIME_TICKS = 600;
    private static final double MAX_DURATION_MULTIPLIER = 2.0;

    public static class AttributeConfig {
        private final PlayerEntity summoner;
        private final LivingEntity summon;
        private final float spellCoefficient;
        private final SpellSchool school;

        public AttributeConfig(PlayerEntity summoner, LivingEntity summon, float spellCoefficient, SpellSchool school) {
            this.summoner = summoner;
            this.summon = summon;
            this.spellCoefficient = spellCoefficient;
            this.school = school;
        }
    }

    public static void applyAllAttributes(AttributeConfig config) {
        applyDamage(config);
        applyHealth(config);
        applyArmor(config);
    }

    public static void applyDamage(AttributeConfig config) {
        if (config.summon.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE) == null) {
            return;
        }

        double multiplier = calculateMultiplier(config.summoner, SummonerAttributes.SUMMON_DAMAGE, config.spellCoefficient, config.school);
        double baseDamage = config.summon.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        config.summon.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(baseDamage * multiplier);
    }

    public static void applyHealth(AttributeConfig config) {
        if (config.summon.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH) == null) {
            return;
        }

        double multiplier = calculateMultiplier(config.summoner, SummonerAttributes.SUMMON_HEALTH, config.spellCoefficient, config.school);
        double baseHealth = config.summon.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH);
        config.summon.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(baseHealth * multiplier);
        config.summon.setHealth(config.summon.getMaxHealth());
    }

    public static void applyArmor(AttributeConfig config) {
        if (config.summon.getAttributeInstance(EntityAttributes.GENERIC_ARMOR) == null) {
            return;
        }

        double armorBonus = config.summoner.getAttributeValue(SummonerAttributes.SUMMON_ARMOR);
        double baseArmor = config.summon.getAttributeValue(EntityAttributes.GENERIC_ARMOR);
        config.summon.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(baseArmor + armorBonus);
    }

    public static int calculateLifetime(PlayerEntity summoner, float spellCoefficient, SpellSchool school) {
        return calculateLifetime(summoner, spellCoefficient, school, BASE_LIFETIME_TICKS);
    }

    public static int calculateLifetime(PlayerEntity summoner, float spellCoefficient, SpellSchool school, int baseLifetime) {
        SpellPower.Result powerResult = SpellPower.getSpellPower(school, summoner);
        double spellPower = powerResult.baseValue();
        double spellMultiplier = 1.0 + (spellPower * spellCoefficient);

        double durationMultiplier = summoner.getAttributeValue(SummonerAttributes.SUMMON_DURATION);
        double totalMultiplier = Math.min(MAX_DURATION_MULTIPLIER, durationMultiplier * spellMultiplier);

        return (int) (baseLifetime * totalMultiplier);
    }

    public static double getCritChance(PlayerEntity summoner) {
        return summoner.getAttributeValue(SummonerAttributes.SUMMON_CRIT_CHANCE);
    }

    public static double getCritDamage(PlayerEntity summoner) {
        return summoner.getAttributeValue(SummonerAttributes.SUMMON_CRIT_DAMAGE);
    }

    private static double calculateMultiplier(PlayerEntity summoner, net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attribute, float spellCoefficient, SpellSchool school) {
        SpellPower.Result powerResult = SpellPower.getSpellPower(school, summoner);
        double spellPower = powerResult.baseValue();
        double spellMultiplier = 1.0 + (spellPower * spellCoefficient);

        return summoner.getAttributeValue(attribute) * spellMultiplier;
    }
}