package com.olympus.aether.system;

import com.olympus.aether.lore.OlympusLoreLibrary;
import com.olympus.aether.network.AetherSyncPayload;
import com.olympus.aether.registry.ModItems;
import com.olympus.aether.world.OlympusWorldState;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

public class AetherCoreSystem {

    public static final String[] RUNE_NAMES = {
        "خطوة الإله (God Step — Spatium)",
        "نصل الإيثر والتجدد (Aether Blade & Vivum)",
        "حرف الدمار (Destruction Rune — Vivum)",
        "مرثية أروا (Aroa's Requiem — Aevum)",
        "سكون الفراغ (Static Void — Aevum)",
        "استدعاء ريجيس (Regis Aether Companion)"
    };

    public record BrokenRecord(RegistryKey<World> dim, BlockPos pos, BlockState state) {}
    private static final Deque<BrokenRecord> RECENTLY_BROKEN = new ArrayDeque<>();
    private static final Map<UUID, Integer> FROZEN_MOBS = new HashMap<>();
    private static final Random RNG = new Random();

    public static void syncToClient(ServerPlayerEntity player, OlympusWorldState ws) {
        OlympusWorldState.AetherData d = ws.getOrCreateAether(player.getUuid());
        ServerPlayNetworking.send(player, new AetherSyncPayload(
            d.unlocked, d.coreLayer, d.aether, d.maxAether, d.activeRune, d.destructionActive, ws.currentStage
        ));
    }

    public static void recordBrokenBlock(World world, BlockPos pos, BlockState state) {
        if (world.isClient || state.isAir()) return;
        RECENTLY_BROKEN.addFirst(new BrokenRecord(world.getRegistryKey(), pos.toImmutable(), state));
        while (RECENTLY_BROKEN.size() > 160) RECENTLY_BROKEN.removeLast();
    }

    public static void awakenAetherCore(ServerPlayerEntity player, OlympusWorldState ws) {
        OlympusWorldState.AetherData data = ws.getOrCreateAether(player.getUuid());
        if (data.unlocked) { syncToClient(player, ws); return; }

        data.unlocked = true;
        data.coreLayer = 1;
        data.maxAether = 120;
        data.aether = 120;
        ws.markDirty();
        syncToClient(player, ws);

        ServerWorld sw = player.getServerWorld();
        sw.spawnParticles(ParticleTypes.DRAGON_BREATH, player.getX(), player.getY() + 1.0, player.getZ(), 120, 1.5, 1.5, 1.5, 0.12);
        sw.spawnParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.2, player.getZ(), 60, 1.2, 1.2, 1.2, 0.1);
        sw.playSound(null, player.getBlockPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1.2F, 0.7F);
        sw.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_END_PORTAL_SPAWN, SoundCategory.PLAYERS, 1.0F, 1.1F);

        player.sendMessage(Text.literal("§5§l════════════════════════════════════════"), false);
        player.sendMessage(Text.literal("§d§l✦ نواة الإيثر: مُستيقظة (البداية بعد النهاية)! ✦"), false);
        player.sendMessage(Text.literal("§fتحطّمت حدود المانا داخلك يا ستيف، وتدفّق الإيثر البنفسجي ليشكّل نواتك الأولى على خطى آرثر ليون!"), false);
        player.sendMessage(Text.literal("§5حصلت على «بؤرة الإيثر» لإتقان الأحرف الإلهية (Godrunes) واستدعاء ريجيس."), false);
        player.sendMessage(Text.literal("§5§l════════════════════════════════════════"), false);

        player.getInventory().offerOrDrop(new ItemStack(ModItems.AETHER_FOCUS));
        player.getInventory().offerOrDrop(new ItemStack(ModItems.AETHER_KEYSTONE));
    }

    public static void upgradeCoreLayer(ServerPlayerEntity player, OlympusWorldState ws) {
        OlympusWorldState.AetherData data = ws.getOrCreateAether(player.getUuid());
        if (!data.unlocked) { awakenAetherCore(player, ws); return; }
        if (data.coreLayer >= 3) {
            player.sendMessage(Text.literal("§5نواة الإيثر لديك بلغت ذروتها القصوى (الطبقة الثالثة)!"), false);
            return;
        }
        data.coreLayer++;
        data.maxAether = data.coreLayer == 2 ? 250 : 500;
        data.aether = data.maxAether;
        ws.markDirty();
        syncToClient(player, ws);

        ServerWorld sw = player.getServerWorld();
        sw.spawnParticles(ParticleTypes.DRAGON_BREATH, player.getX(), player.getY() + 1.0, player.getZ(), 100, 1.2, 1.2, 1.2, 0.1);
        sw.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.2F, 0.8F);
        player.sendMessage(Text.literal("§5§l✦ نواة الإيثر: مُستيقظة (الطبقة " + data.coreLayer + ")! السعة القصوى: " + data.maxAether), false);
    }

    public static void tickServer(MinecraftServer server) {
        OlympusWorldState ws = OlympusWorldState.get(server);

        Iterator<Map.Entry<UUID, Integer>> it = FROZEN_MOBS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> entry = it.next();
            int left = entry.getValue() - 1;
            if (left <= 0) {
                for (ServerWorld sw : server.getWorlds()) {
                    if (sw.getEntity(entry.getKey()) instanceof MobEntity mob) mob.setAiDisabled(false);
                }
                it.remove();
            } else {
                entry.setValue(left);
            }
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            OlympusWorldState.AetherData data = ws.getOrCreateAether(player.getUuid());
            if (server.getTicks() % 10 == 0) syncToClient(player, ws);
            if (!data.unlocked) continue;

            if (server.getTicks() % 20 == 0 && data.aether < data.maxAether && !data.destructionActive) {
                data.aether = Math.min(data.maxAether, data.aether + (data.coreLayer * 4));
            }

            if (data.destructionActive) {
                data.destructionTicks++;
                if (server.getTicks() % 10 == 0) {
                    data.aether -= 5;
                    ServerWorld sw = player.getServerWorld();
                    sw.spawnParticles(ParticleTypes.DRAGON_BREATH, player.getX(), player.getY() + 1.0, player.getZ(), 14, 0.7, 0.8, 0.7, 0.04);
                    Box area = player.getBoundingBox().expand(4.5, 2.5, 4.5);
                    for (LivingEntity e : sw.getEntitiesByClass(LivingEntity.class, area, ent -> ent != player && ent.isAlive() && !(ent instanceof WolfEntity))) {
                        e.damage(sw.getDamageSources().magic(), 9.0F * data.coreLayer);
                        e.setOnFireFor(6.0F);
                    }
                }
                if (data.destructionTicks > 160) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0, true, false));
                    if (data.destructionTicks % 60 == 0) {
                        player.sendMessage(Text.literal("§5§o[همس الدمار]: أطفئ حرف الدمار قبل أن يبتلع وعيك!"), true);
                    }
                }
                if (data.aether <= 0) {
                    data.aether = 0;
                    data.destructionActive = false;
                    data.destructionTicks = 0;
                    player.sendMessage(Text.literal("§dانطفأ حرف الدمار لنفاد الإيثر.").formatted(Formatting.LIGHT_PURPLE), true);
                }
            }
        }
    }

    public static void cycleRune(ServerPlayerEntity player, OlympusWorldState ws) {
        OlympusWorldState.AetherData data = ws.getOrCreateAether(player.getUuid());
        if (!data.unlocked) {
            player.sendMessage(Text.literal("§cلم تستيقظ نواة الإيثر بعد! أكمل الأوديسة الـ 12 أولاً.").formatted(Formatting.RED), true);
            return;
        }
        data.activeRune = (data.activeRune + 1) % RUNE_NAMES.length;
        ws.markDirty();
        syncToClient(player, ws);
        player.sendMessage(Text.literal("§5✦ الحرف الإلهي النشط: §d" + RUNE_NAMES[data.activeRune]), true);
    }

    public static void castActiveRune(ServerPlayerEntity player, OlympusWorldState ws) {
        OlympusWorldState.AetherData data = ws.getOrCreateAether(player.getUuid());
        if (!data.unlocked) {
            player.sendMessage(Text.literal("§cنواة الإيثر مغلقة! اجمع الإثني عشر خالداً لكسر حاجز المانا.").formatted(Formatting.RED), true);
            return;
        }

        ServerWorld sw = player.getServerWorld();
        switch (data.activeRune) {
            case 0 -> {
                if (data.aether < 15) { notEnough(player, data); return; }
                data.aether -= 15;
                Vec3d start = player.getEyePos();
                Vec3d dir = player.getRotationVector().normalize();
                Vec3d dest = start;
                int range = 10 + (data.coreLayer * 6);
                for (int i = 1; i <= range; i++) {
                    Vec3d next = start.add(dir.multiply(i));
                    sw.spawnParticles(ParticleTypes.DRAGON_BREATH, next.x, next.y, next.z, 3, 0.1, 0.1, 0.1, 0.01);
                    BlockPos bp = BlockPos.ofFloored(next);
                    if (!sw.getBlockState(bp).isAir() && !sw.getBlockState(bp.up()).isAir()) break;
                    dest = next;
                }
                player.teleport(sw, dest.x, dest.y, dest.z, player.getYaw(), player.getPitch());
                sw.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.6F);
                gainInsight(player, data, ws, 10);
            }
            case 1 -> {
                if (data.aether < 20) { notEnough(player, data); return; }
                data.aether -= 20;
                player.heal(12.0F * data.coreLayer);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 160, 2));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 200, 2 + data.coreLayer));
                Vec3d start = player.getEyePos();
                Vec3d dir = player.getRotationVector().normalize();
                for (int i = 1; i <= 14; i++) {
                    Vec3d p = start.add(dir.multiply(i * 0.8));
                    sw.spawnParticles(ParticleTypes.DRAGON_BREATH, p.x, p.y, p.z, 8, 0.3, 0.3, 0.3, 0.03);
                    for (LivingEntity e : sw.getEntitiesByClass(LivingEntity.class, new Box(p.subtract(1.5, 1.5, 1.5), p.add(1.5, 1.5, 1.5)), ent -> ent != player && ent.isAlive())) {
                        e.damage(sw.getDamageSources().magic(), 20.0F * data.coreLayer);
                    }
                }
                sw.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 0.6F, 1.8F);
                gainInsight(player, data, ws, 12);
            }
            case 2 -> {
                data.destructionActive = !data.destructionActive;
                data.destructionTicks = 0;
                player.sendMessage(Text.literal(data.destructionActive
                    ? "§5🔥 اشتعل حرف الدمار البنفسجي! احذر من فقدان السيطرة!"
                    : "§dتم كبح حرف الدمار بنجاح."), true);
                sw.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 0.5F, 1.6F);
            }
            case 3 -> {
                if (data.aether < 30) { notEnough(player, data); return; }
                int restored = 0;
                Iterator<BrokenRecord> iter = RECENTLY_BROKEN.iterator();
                while (iter.hasNext() && restored < 48) {
                    BrokenRecord rec = iter.next();
                    if (rec.dim().equals(sw.getRegistryKey()) && rec.pos().isWithinDistance(player.getBlockPos(), 14.0)) {
                        if (sw.getBlockState(rec.pos()).isAir()) {
                            sw.setBlockState(rec.pos(), rec.state());
                            sw.spawnParticles(ParticleTypes.PORTAL, rec.pos().getX() + 0.5, rec.pos().getY() + 0.5, rec.pos().getZ() + 0.5, 6, 0.25, 0.25, 0.25, 0.05);
                            restored++;
                            iter.remove();
                        }
                    }
                }
                if (restored > 0) {
                    data.aether -= 30;
                    sw.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 1.2F, 1.2F);
                    player.sendMessage(Text.literal("§d⏳ مرثية أروا: تم إرجاع الزمن وترميم " + restored + " بلوكة!"), true);
                    gainInsight(player, data, ws, 15);
                } else {
                    player.sendMessage(Text.literal("§7لا توجد بلوكات مدمّرة مؤخراً في محيطك لإعادتها بالزمن."), true);
                }
            }
            case 4 -> {
                if (data.coreLayer < 2) {
                    player.sendMessage(Text.literal("§cسكون الفراغ يتطلب ارتقاء نواة الإيثر للطبقة الثانية!"), true);
                    return;
                }
                if (data.aether < 45) { notEnough(player, data); return; }
                data.aether -= 45;
                int freezeDuration = 100 + (data.coreLayer * 40);
                Box zone = player.getBoundingBox().expand(18.0, 10.0, 18.0);
                int count = 0;
                for (MobEntity mob : sw.getEntitiesByClass(MobEntity.class, zone, m -> m.isAlive() && !(m instanceof WolfEntity))) {
                    mob.setAiDisabled(true);
                    mob.setVelocity(Vec3d.ZERO);
                    FROZEN_MOBS.put(mob.getUuid(), freezeDuration);
                    sw.spawnParticles(ParticleTypes.END_ROD, mob.getX(), mob.getY() + 1.0, mob.getZ(), 10, 0.4, 0.6, 0.4, 0.01);
                    count++;
                }
                sw.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 1.2F, 0.5F);
                player.sendMessage(Text.literal("§5⏸ سكون الفراغ (Static Void): توقّف الزمن لـ " + count + " كائن حولك!"), true);
                gainInsight(player, data, ws, 20);
            }
            case 5 -> {
                if (data.aether < 35) { notEnough(player, data); return; }
                data.aether -= 35;
                WolfEntity regis = EntityType.WOLF.create(sw);
                if (regis != null) {
                    regis.refreshPositionAndAngles(player.getX() + 1.2, player.getY(), player.getZ() + 1.2, player.getYaw(), 0);
                    regis.setOwner(player);
                    regis.setCustomName(Text.literal("§5§lRegis — ريجيس الإيثري"));
                    regis.setCustomNameVisible(true);
                    regis.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 12000, 3));
                    regis.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 12000, 3));
                    regis.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 12000, 0));
                    regis.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 12000, 0));
                    sw.spawnEntity(regis);
                    sw.spawnParticles(ParticleTypes.DRAGON_BREATH, regis.getX(), regis.getY() + 0.5, regis.getZ(), 35, 0.5, 0.5, 0.5, 0.08);
                    String quote = OlympusLoreLibrary.REGIS_QUOTES.get(RNG.nextInt(OlympusLoreLibrary.REGIS_QUOTES.size()));
                    player.sendMessage(Text.literal(quote), false);
                }
            }
        }
        syncToClient(player, ws);
    }

    private static void notEnough(ServerPlayerEntity player, OlympusWorldState.AetherData data) {
        player.sendMessage(Text.literal("§cإيثر غير كافٍ! [" + data.aether + "/" + data.maxAether + "]"), true);
    }

    private static void gainInsight(ServerPlayerEntity player, OlympusWorldState.AetherData data, OlympusWorldState ws, int amount) {
        data.insightXp += amount;
        if (data.coreLayer == 1 && data.insightXp >= 150) upgradeCoreLayer(player, ws);
        else if (data.coreLayer == 2 && data.insightXp >= 400) upgradeCoreLayer(player, ws);
        ws.markDirty();
    }
}
