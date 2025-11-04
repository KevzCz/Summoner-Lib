package net.pixeldreamstudios.summonerlib.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.pixeldreamstudios.summonerlib.tracker.SummonTracker;

public class SummonEventHandler {

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(SummonTracker::tick);
    }
}