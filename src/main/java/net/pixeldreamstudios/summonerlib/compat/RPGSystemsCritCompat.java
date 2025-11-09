package net.pixeldreamstudios.summonerlib.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.pixeldreamstudios.rpgsystems.util.DamageCritLinks;
import net.pixeldreamstudios.summonerlib.util.SummonCritUtil;

public final class RPGSystemsCritCompat {

    private RPGSystemsCritCompat() {}

    public static void init() {

    }

    public static void markSummonCrit(net.minecraft.entity.damage.DamageSource source) {
        if (!FabricLoader.getInstance().isModLoaded("rpg-systems")) return;

        if (SummonCritUtil.wasLastDamageCrit()) {
            DamageCritLinks.link(source, DamageCritLinks.Kind.MAGIC, null);
        }
    }
}