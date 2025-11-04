package net.pixeldreamstudios.summonerlib;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.pixeldreamstudios.summonerlib.attribute.SummonerAttributes;
import net.pixeldreamstudios.summonerlib.event.PlayerEventHandler;
import net.pixeldreamstudios.summonerlib.event.SummonEventHandler;
import net.pixeldreamstudios.summonerlib.network.payload.SummonRemovePayload;
import net.pixeldreamstudios.summonerlib.network.payload.SummonSyncPayload;
import net.pixeldreamstudios.summonerlib.registry.SummonerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SummonerLib implements ModInitializer {
	public static final String MOD_ID = "summonerlib";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Summoner Lib...");

		SummonerAttributes.register();
		SummonerRegistry.register();

		PayloadTypeRegistry.playS2C().register(SummonSyncPayload.ID, SummonSyncPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(SummonRemovePayload.ID, SummonRemovePayload.CODEC);

		SummonEventHandler.register();
		PlayerEventHandler.register();

		LOGGER.info("Summoner Lib initialized!");
	}
}