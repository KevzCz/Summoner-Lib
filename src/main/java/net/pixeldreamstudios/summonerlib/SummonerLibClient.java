package net.pixeldreamstudios.summonerlib;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.pixeldreamstudios.summonerlib.network.payload.SummonRemovePayload;
import net.pixeldreamstudios.summonerlib.network.payload.SummonSyncPayload;
import net.pixeldreamstudios.summonerlib.network.payload.UnsummonAllPayload;
import net.pixeldreamstudios.summonerlib.tracker.ClientSummonTracker;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class SummonerLibClient implements ClientModInitializer {

    private static KeyBinding unsummonAllKey;

    @Override
    public void onInitializeClient() {
        unsummonAllKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.summonerlib.unsummon_all",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                "category.summonerlib.keybinds"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (unsummonAllKey.wasPressed()) {
                if (client.player != null) {
                    ClientPlayNetworking.send(new UnsummonAllPayload());
                }
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(SummonSyncPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (payload.isRegistering()) {
                    ClientSummonTracker.registerSummon(payload.entityUuid(), payload.summonType());
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SummonRemovePayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClientSummonTracker.unregisterSummon(payload.entityUuid());
            });
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ClientSummonTracker.clearAll();
        });

        SummonerLib.LOGGER.info("Summoner Lib client initialized!");
    }
}