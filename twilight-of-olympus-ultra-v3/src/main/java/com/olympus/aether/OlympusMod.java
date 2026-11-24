package com.olympus.aether;

import com.olympus.aether.command.OlympusCommands;
import com.olympus.aether.item.DivineRelic;
import com.olympus.aether.lore.OlympusLoreLibrary;
import com.olympus.aether.network.AetherSyncPayload;
import com.olympus.aether.registry.ModArmorMaterials;
import com.olympus.aether.registry.ModBlocks;
import com.olympus.aether.registry.ModItems;
import com.olympus.aether.system.AetherCoreSystem;
import com.olympus.aether.system.EternalSoulboundHandler;
import com.olympus.aether.world.OlympusShrineManager;
import com.olympus.aether.world.OlympusWorldState;
import com.olympus.aether.world.WarMarksGenerator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OlympusMod implements ModInitializer {
    public static final String MOD_ID = "olympus_aether";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(AetherSyncPayload.ID, AetherSyncPayload.CODEC);
        ModArmorMaterials.register();
        ModBlocks.register();
        ModItems.register();
        OlympusCommands.register();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            OlympusWorldState ws = OlympusWorldState.get(server);
            if (ws.initializedPlayers.add(player.getUuid())) {
                ws.markDirty();
                player.getInventory().offerOrDrop(new ItemStack(ModItems.ODYSSEY_COMPASS));
                player.getInventory().offerOrDrop(new ItemStack(ModItems.OLYMPIAN_CODEX));
                player.sendMessage(Text.literal("§6§l✦ استيقظت شرارة بروميثيوس فيك يا ستيف — أنت آخر إنسان نقي في العالم!"), false);
                player.sendMessage(Text.literal("§eاستخدم «بوصلة بروميثيوس» لبدء طريق الأوديسة من المحطة الأولى."), false);
            }
            AetherCoreSystem.syncToClient(player, ws);
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            EternalSoulboundHandler.restoreAfterRespawn(newPlayer);
            AetherCoreSystem.syncToClient(newPlayer, OlympusWorldState.get(newPlayer.getServer()));
        });

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, be) ->
            AetherCoreSystem.recordBrokenBlock(world, pos, state)
        );

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(entity instanceof ServerPlayerEntity player)) return true;

            if (source.isOf(DamageTypes.FALL) && player.getEquippedStack(EquipmentSlot.FEET).isOf(ModItems.TALARIA_OF_HERMES)) {
                return false;
            }
            if (player.isBlocking() && player.getActiveItem().isOf(ModItems.AEGIS_SHIELD_OF_ATHENA)) {
                if (source.getAttacker() instanceof LivingEntity attacker) {
                    attacker.damage(player.getServerWorld().getDamageSources().thorns(player), amount * 0.5F);
                    attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 6));
                    attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 80, 4));
                    player.getServerWorld().spawnParticles(ParticleTypes.END_ROD, attacker.getX(), attacker.getY() + 1, attacker.getZ(), 18, 0.4, 0.7, 0.4, 0.05);
                }
                player.getServerWorld().playSound(null, player.getBlockPos(), SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS, 1.0F, 1.4F);
                return false;
            }
            return true;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            AetherCoreSystem.tickServer(server);
            if (server.getTicks() % 20 != 0) return;

            OlympusWorldState ws = OlympusWorldState.get(server);
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (server.getTicks() % 100 == 0) {
                    WarMarksGenerator.trySpawnWarMarkNear(player, ws);
                }

                for (int i = 0; i < player.getInventory().size(); i++) {
                    ItemStack st = player.getInventory().getStack(i);
                    if (st.getItem() instanceof DivineRelic relic && relic.getOdysseyStage() >= 1 && relic.getOdysseyStage() <= 12) {
                        int s = relic.getOdysseyStage();
                        if (!ws.claimedRelics.contains(s)) {
                            ws.markRelicClaimed(s);
                            OlympusLoreLibrary.StageMeta meta = OlympusLoreLibrary.getStage(s);
                            player.sendMessage(Text.literal("§6§l✦ اكتملت المحطة " + s + "/12: استعدت " + meta.relicNameAr() + "!"), false);
                            if (s == 12) {
                                AetherCoreSystem.awakenAetherCore(player, ws);
                            }
                        }
                    }
                }

                if (ws.currentStage <= 12 && !ws.generatedShrines.contains(ws.currentStage)) {
                    BlockPos target = ws.shrinePositions.get(ws.currentStage);
                    ServerWorld sw = player.getServerWorld();
                    boolean rightDim = (ws.currentStage <= 7 || ws.currentStage == 10 || ws.currentStage == 11) && sw.getRegistryKey().equals(World.OVERWORLD)
                        || (ws.currentStage == 8 || ws.currentStage == 9) && sw.getRegistryKey().equals(World.NETHER)
                        || (ws.currentStage == 12) && sw.getRegistryKey().equals(World.END);

                    if (rightDim && target != null && player.getBlockPos().isWithinDistance(target, 96.0)) {
                        OlympusShrineManager.generateUniqueShrine(sw, target, ws.currentStage, ws);
                    }
                }
            }
        });

        LOGGER.info("Twilight of Olympus: Legacy of Aether ULTRA v3 initialized!");
    }
}
