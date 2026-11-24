package com.olympus.aether.item;

import com.olympus.aether.registry.ModToolMaterials;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

public class HearthGreataxeItem extends AxeItem implements DivineRelic {
    public HearthGreataxeItem(Settings settings) {
        super(ModToolMaterials.OLYMPIAN, settings.attributeModifiers(
            AxeItem.createAttributeModifiers(ModToolMaterials.OLYMPIAN, 15.0f, -2.6f)
        ));
    }

    @Override public int getOdysseyStage() { return 4; }
    @Override public String getOlympianGod() { return "هيستيا وديونيسوس (Hestia & Dionysus)"; }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient && entity instanceof PlayerEntity p) DivineRelic.bindOwner(stack, p);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 80, 1));
        return super.postHit(stack, target, attacker);
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!world.isClient && world instanceof ServerWorld sw && miner instanceof PlayerEntity player && state.isIn(BlockTags.LOGS) && !player.isSneaking()) {
            Queue<BlockPos> queue = new ArrayDeque<>();
            Set<BlockPos> visited = new HashSet<>();
            queue.add(pos);
            visited.add(pos);
            int broken = 0;
            while (!queue.isEmpty() && broken < 96) {
                BlockPos current = queue.poll();
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = 0; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            BlockPos next = current.add(dx, dy, dz);
                            if (visited.add(next) && sw.getBlockState(next).isIn(BlockTags.LOGS)) {
                                Block.dropStacks(sw.getBlockState(next), sw, next, null, player, stack);
                                sw.removeBlock(next, false);
                                queue.add(next);
                                broken++;
                            }
                        }
                    }
                }
            }
        }
        return super.postMine(stack, world, state, pos, miner);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("✦ فأس الموقد الذهبي لهيستيا — إرث أولمبي وحيد").formatted(Formatting.GOLD, Formatting.BOLD));
        tooltip.add(Text.literal("يقطع الشجرة بالكامل بضربة واحدة + تجدد صحي في القتال").formatted(Formatting.GREEN));
        tooltip.add(Text.literal("⚡ مرتبط بالروح أبدياً (لا يُكسر ولا يضيع)").formatted(Formatting.LIGHT_PURPLE));
    }
}
