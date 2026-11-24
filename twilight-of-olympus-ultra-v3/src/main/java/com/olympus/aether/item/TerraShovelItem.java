package com.olympus.aether.item;

import com.olympus.aether.registry.ModToolMaterials;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class TerraShovelItem extends ShovelItem implements DivineRelic {
    public TerraShovelItem(Settings settings) {
        super(ModToolMaterials.OLYMPIAN, settings.attributeModifiers(
            ShovelItem.createAttributeModifiers(ModToolMaterials.OLYMPIAN, 4.5f, -2.7f)
        ));
    }

    @Override public int getOdysseyStage() { return 12; }
    @Override public String getOlympianGod() { return "ديميتر (Demeter)"; }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient && entity instanceof PlayerEntity p) DivineRelic.bindOwner(stack, p);
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!world.isClient && world instanceof ServerWorld sw && miner instanceof PlayerEntity player && state.isIn(BlockTags.SHOVEL_MINEABLE) && !player.isSneaking()) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    BlockPos target = pos.add(dx, 0, dz);
                    BlockState bs = sw.getBlockState(target);
                    if (bs.isIn(BlockTags.SHOVEL_MINEABLE)) {
                        Block.dropStacks(bs, sw, target, null, player, stack);
                        sw.removeBlock(target, false);
                    }
                }
            }
        }
        return super.postMine(stack, world, state, pos, miner);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient && world instanceof ServerWorld sw) {
            BlockPos center = user.getBlockPos().down();
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos p = center.add(dx, 0, dz);
                    BlockState s = sw.getBlockState(p);
                    if (s.isOf(Blocks.DIRT) || s.isOf(Blocks.SAND) || s.isOf(Blocks.COARSE_DIRT) || s.isOf(Blocks.RED_SAND)) {
                        sw.setBlockState(p, Blocks.GRASS_BLOCK.getDefaultState());
                        sw.spawnParticles(ParticleTypes.HAPPY_VILLAGER, p.getX() + 0.5, p.getY() + 1.1, p.getZ() + 0.5, 2, 0.2, 0.1, 0.2, 0.02);
                    }
                }
            }
            sw.playSound(null, user.getBlockPos(), SoundEvents.ITEM_BONE_MEAL_USE, SoundCategory.PLAYERS, 1.0F, 1.1F);
            user.getItemCooldownManager().set(this, 25);
        }
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("✦ مجرفة الحصاد والأرض لديميتر — إرث أولمبي وحيد").formatted(Formatting.GOLD, Formatting.BOLD));
        tooltip.add(Text.literal("حفر 3×3 فوري + زر الفأرة الأيمن يُحيي الأرض القاحلة إلى مروج خضراء").formatted(Formatting.GREEN));
        tooltip.add(Text.literal("⚡ مرتبط بالروح أبدياً (لا يُكسر ولا يضيع)").formatted(Formatting.LIGHT_PURPLE));
    }
}
