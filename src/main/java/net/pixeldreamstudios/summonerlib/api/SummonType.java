package net.pixeldreamstudios.summonerlib.api;

import net.minecraft.util.Identifier;

public record SummonType(
        Identifier id,
        String name,
        int defaultLifetimeTicks,
        boolean defaultPersistent,
        boolean defaultAllowInteraction
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

        public SummonType build() {
            return new SummonType(id, name, defaultLifetimeTicks, defaultPersistent, defaultAllowInteraction);
        }
    }
}