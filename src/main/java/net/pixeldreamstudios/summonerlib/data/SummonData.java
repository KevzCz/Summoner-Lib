package net.pixeldreamstudios.summonerlib.data;

import net.minecraft.entity.Entity;

import java.util.UUID;

public class SummonData {
    public final UUID entityUuid;
    public final UUID ownerUuid;
    public final long spawnTick;
    public final int lifetimeTicks;
    public final boolean allowInteraction;
    public final String summonType;
    public final Entity entityRef;
    public boolean hasShownWarning = false;
    public final int summonIndex;
    public final boolean persistent;
    public final int slotCost;
    public final String groupId;

    public SummonData(
            UUID entityUuid,
            UUID ownerUuid,
            long spawnTick,
            int lifetimeTicks,
            boolean allowInteraction,
            String summonType,
            Entity entityRef,
            int summonIndex,
            boolean persistent,
            int slotCost,
            String groupId
    ) {
        this.entityUuid = entityUuid;
        this.ownerUuid = ownerUuid;
        this.spawnTick = spawnTick;
        this.lifetimeTicks = lifetimeTicks;
        this.allowInteraction = allowInteraction;
        this.summonType = summonType;
        this.entityRef = entityRef;
        this.summonIndex = summonIndex;
        this.persistent = persistent;
        this.slotCost = Math.max(0, slotCost);
        this.groupId = groupId;
    }

    public Entity getEntity() {
        return entityRef;
    }

    public boolean isExpired(long currentTick) {
        if (lifetimeTicks <= 0) return false;
        return (currentTick - spawnTick) >= lifetimeTicks;
    }

    public int getRemainingTicks(long currentTick) {
        if (lifetimeTicks <= 0) return Integer.MAX_VALUE;
        return (int) (lifetimeTicks - (currentTick - spawnTick));
    }

    public boolean shouldShowWarning(long currentTick, int warningThreshold) {
        return getRemainingTicks(currentTick) <= warningThreshold && !hasShownWarning;
    }

    public void setWarningShown() {
        this.hasShownWarning = true;
    }

    public boolean shouldFlash(long currentTick, int warningThreshold, int flashInterval) {
        int remainingTicks = getRemainingTicks(currentTick);
        return remainingTicks <= warningThreshold && (remainingTicks % flashInterval) < flashInterval / 2;
    }

    public int getAge(long currentTick) {
        return (int) (currentTick - spawnTick);
    }

    public float getLifetimeProgress(long currentTick) {
        if (lifetimeTicks <= 0) return 0f;
        return (float) getAge(currentTick) / lifetimeTicks;
    }
}