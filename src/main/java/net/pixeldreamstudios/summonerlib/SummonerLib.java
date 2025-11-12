package net.pixeldreamstudios.summonerlib;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.pixeldreamstudios.summonerlib.attribute.SummonerAttributes;
import net.pixeldreamstudios.summonerlib.compat.RPGSystemsCritCompat;
import net.pixeldreamstudios.summonerlib.event.PlayerEventHandler;
import net.pixeldreamstudios.summonerlib.network.payload.SummonRemovePayload;
import net.pixeldreamstudios.summonerlib.network.payload.SummonSyncPayload;
import net.pixeldreamstudios.summonerlib.network.payload.UnsummonAllPayload;
import net.pixeldreamstudios.summonerlib.registry.SummonerRegistry;
import net.pixeldreamstudios.summonerlib.tracker.SummonTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SummonerLib implements ModInitializer {
	public static final String MOD_ID = "summonerlib";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		SummonerAttributes.register();
		SummonerRegistry.register();

		PayloadTypeRegistry.playC2S().register(UnsummonAllPayload.ID, UnsummonAllPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(SummonSyncPayload.ID, SummonSyncPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(SummonRemovePayload.ID, SummonRemovePayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(UnsummonAllPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				var player = context.player();
				if (player != null && player.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
					SummonTracker.removeAllSummonsForPlayer(player.getUuid(), serverWorld);
				}
			});
		});
		if (FabricLoader.getInstance().isModLoaded("rpg-systems"))
		{
			RPGSystemsCritCompat.init();
		}
		ServerTickEvents.END_WORLD_TICK.register(SummonTracker::tick);
		PlayerEventHandler.register();
		LOGGER.info("Summoner Lib initialized!");
	}
}