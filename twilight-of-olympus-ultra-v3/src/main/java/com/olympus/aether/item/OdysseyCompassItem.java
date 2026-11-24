package com.olympus.aether.item;

import com.olympus.aether.lore.OlympusLoreLibrary;
import com.olympus.aether.world.OlympusWorldState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class OdysseyCompassItem extends Item implements DivineRelic {
    public OdysseyCompassItem(Settings settings) { super(settings); }
    @Override public int getOdysseyStage() { return 0; }
    @Override public String getOlympianGod() { return "بروميثيوس (Prometheus)"; }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient && entity instanceof PlayerEntity p) DivineRelic.bindOwner(stack, p);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient && user instanceof ServerPlayerEntity sp && world instanceof ServerWorld sw) {
            OlympusWorldState ws = OlympusWorldState.get(sp.getServer());
            if (ws.currentStage > 12) {
                sp.sendMessage(Text.literal("§5✦ اكتملت أوديسة الإثني عشر! استخدم «بؤرة الإيثر» لإتقان الأحرف الإلهية (Godrunes)."), false);
                return TypedActionResult.success(stack, false);
            }
            OlympusLoreLibrary.StageMeta meta = OlympusLoreLibrary.getStage(ws.currentStage);
            BlockPos target = ws.shrinePositions.get(ws.currentStage);
            if (target == null) return TypedActionResult.pass(stack);

            boolean rightDim = (ws.currentStage <= 7 || ws.currentStage == 10 || ws.currentStage == 11) && sw.getRegistryKey().equals(World.OVERWORLD)
                || (ws.currentStage == 8 || ws.currentStage == 9) && sw.getRegistryKey().equals(World.NETHER)
                || (ws.currentStage == 12) && sw.getRegistryKey().equals(World.END);

            sp.sendMessage(Text.literal("§6═════ [بوصلة بروميثيوس — المحطة " + ws.currentStage + "/12] ═════"), false);
            sp.sendMessage(Text.literal("§eالهدف: §f" + meta.relicNameAr() + " §7(" + meta.godNameAr() + ")"), false);
            sp.sendMessage(Text.literal("§bالهيكل المرتبط: §f" + meta.structureHintAr() + " §7— " + meta.dimensionNameAr()), false);
            if (rightDim) {
                int dist = (int) Math.sqrt(sp.getBlockPos().getSquaredDistance(target));
                sp.sendMessage(Text.literal("§aالإحداثيات: §fX=" + target.getX() + " , Y=" + target.getY() + " , Z=" + target.getZ() + " §7(المسافة: " + dist + " بلوكة)"), false);
                Vec3d dir = Vec3d.ofCenter(target).subtract(sp.getEyePos()).normalize();
                for (int i = 1; i <= 12; i++) {
                    Vec3d pt = sp.getEyePos().add(dir.multiply(i * 0.8));
                    sw.spawnParticles(ParticleTypes.END_ROD, pt.x, pt.y, pt.z, 2, 0.05, 0.05, 0.05, 0.01);
                }
            } else {
                sp.sendMessage(Text.literal("§cانتقل أولاً إلى بُعد: " + meta.dimensionNameAr() + " لتوجيه المسار!"), false);
            }
            sp.getItemCooldownManager().set(this, 20);
        }
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("✦ بوصلة طريق الأوديسة المتسلسل").formatted(Formatting.GOLD, Formatting.BOLD));
        tooltip.add(Text.literal("زر الفأرة الأيمن: كشف إحداثيات واتجاه المحطة القصصية الحالية").formatted(Formatting.AQUA));
    }
}
