package net.pixeldreamstudios.summonerlib.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.pixeldreamstudios.summonerlib.data.SummonData;
import net.pixeldreamstudios.summonerlib.tracker.SummonTracker;

import java.util.List;
import java.util.UUID;

public class SummonFormationUtil {

    /**
     * Arrange summons in a circle around the player
     */
    public static void arrangeInCircle(PlayerEntity player, String summonType, double radius) {
        List<UUID> summons = SummonTracker.getPlayerSummonsByType(player.getUuid(), summonType);
        int count = summons.size();
        if (count == 0) return;

        Vec3d center = player.getPos();

        for (int i = 0; i < count; i++) {
            SummonData data = SummonTracker.getSummonData(summons.get(i));
            if (data != null && data.getEntity() != null) {
                double angle = (2 * Math.PI * i) / count;
                double x = center.x + radius * Math.cos(angle);
                double z = center.z + radius * Math.sin(angle);

                data.getEntity().setPosition(x, center.y, z);
                data.getEntity().setYaw((float) Math.toDegrees(angle + Math.PI / 2));
            }
        }
    }

    /**
     * Arrange summons in a line formation
     */
    public static void arrangeInLine(PlayerEntity player, String summonType, double spacing) {
        List<UUID> summons = SummonTracker.getPlayerSummonsByType(player.getUuid(), summonType);
        int count = summons.size();
        if (count == 0) return;

        Vec3d playerPos = player.getPos();
        Vec3d lookDir = player.getRotationVec(1.0f).normalize();
        Vec3d rightDir = new Vec3d(-lookDir.z, 0, lookDir.x).normalize();

        double totalWidth = (count - 1) * spacing;
        double startOffset = -totalWidth / 2;

        for (int i = 0; i < count; i++) {
            SummonData data = SummonTracker.getSummonData(summons.get(i));
            if (data != null && data.getEntity() != null) {
                double offset = startOffset + (i * spacing);
                Vec3d pos = playerPos.add(rightDir.multiply(offset));

                data.getEntity().setPosition(pos.x, pos.y, pos.z);
                data.getEntity().setYaw(player.getYaw());
            }
        }
    }

    /**
     * Teleport all summons to player
     */
    public static void teleportToPlayer(PlayerEntity player, String summonType) {
        List<UUID> summons = SummonTracker.getPlayerSummonsByType(player.getUuid(), summonType);
        Vec3d playerPos = player.getPos();

        for (UUID uuid : summons) {
            SummonData data = SummonTracker.getSummonData(uuid);
            if (data != null && data.getEntity() != null) {
                data.getEntity().setPosition(playerPos.x, playerPos.y, playerPos.z);
            }
        }
    }
}