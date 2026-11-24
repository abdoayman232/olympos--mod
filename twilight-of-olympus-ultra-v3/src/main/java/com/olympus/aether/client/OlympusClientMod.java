package com.olympus.aether.client;

import com.olympus.aether.OlympusMod;
import com.olympus.aether.network.AetherSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.render.entity.PlayerEntityRenderer;

public class OlympusClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            if (entityRenderer instanceof PlayerEntityRenderer playerRenderer) {
                registrationHelper.register(new OlympianWingsFeatureRenderer(playerRenderer));
            }
        });

        HudRenderCallback.EVENT.register(OlympusHudRenderer::render);

        ClientPlayNetworking.registerGlobalReceiver(AetherSyncPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClientAetherState.unlocked = payload.unlocked();
                ClientAetherState.coreLayer = payload.coreLayer();
                ClientAetherState.aether = payload.aether();
                ClientAetherState.maxAether = payload.maxAether();
                ClientAetherState.activeRune = payload.activeRune();
                ClientAetherState.destructionActive = payload.destructionActive();
                ClientAetherState.currentStage = payload.currentStage();
            });
        });

        OlympusMod.LOGGER.info("Twilight of Olympus Ultra v3 Client initialized!");
    }
}
