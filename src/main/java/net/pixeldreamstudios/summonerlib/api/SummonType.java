package net.pixeldreamstudios.summonerlib.api;

import net.minecraft.util.Identifier;

public record SummonType(
        Identifier id,
        String name,
        int defaultLifetimeTicks,
        boolean defaultPersistent,
        boolean defaultAllowInteraction,
        int defaultSlotCost,
        String groupId,
        int maxCount
) {

    public static Builder builder(Identifier id) {
        return new Builder(id);
    }

    public static class Builder {
        private final Identifier id;
        private String name;
        private int defaultLifetimeTicks = 600;
        private boolean defaultPersistent = false;
        private boolean defaultAllowInteraction = false;
        private int defaultSlotCost = 1;
        private String groupId = "default";
        private int maxCount = -1;

        private Builder(Identifier id) {
            this.id = id;
            this.name = id.getPath();
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder lifetime(int ticks) {
            this.defaultLifetimeTicks = ticks;
            return this;
        }

        public Builder persistent(boolean persistent) {
            this.defaultPersistent = persistent;
            return this;
        }

        public Builder allowInteraction(boolean allow) {
            this.defaultAllowInteraction = allow;
            return this;
        }

        public Builder slotCost(int cost) {
            this.defaultSlotCost = Math.max(1, cost);
            return this;
        }

        public Builder group(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder maxCount(int maxCount) {
            this.maxCount = maxCount;
            return this;
        }

        public SummonType build() {
            return new SummonType(id, name, defaultLifetimeTicks, defaultPersistent,
                    defaultAllowInteraction, defaultSlotCost, groupId, maxCount);
        }
    }
}