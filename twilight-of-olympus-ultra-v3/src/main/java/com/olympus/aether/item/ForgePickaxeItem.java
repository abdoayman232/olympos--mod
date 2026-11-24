package com.olympus.aether.item;

import com.olympus.aether.registry.ModToolMaterials;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

public class ForgePickaxeItem extends PickaxeItem implements DivineRelic {
    public ForgePickaxeItem(Settings settings) {
        super(ModToolMaterials.OLYMPIAN, settings.attributeModifiers(
            PickaxeItem.createAttributeModifiers(ModToolMaterials.OLYMPIAN, 5, -2.5f)
        ));
    }

    @Override public int getOdysseyStage() { return 3; }
    @Override public String getOlympianGod() { return "هيفايستوس (Hephaestus)"; }

    public static boolean isAreaMode(ItemStack stack) {
        NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        return nbt.getBoolean("area_3x3");
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient && entity instanceof PlayerEntity p) DivineRelic.bindOwner(stack, p);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient) {
            NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
            boolean next = !nbt.getBoolean("area_3x3");
            nbt.putBoolean("area_3x3", next);
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
            user.sendMessage(Text.literal(next ? "⛏ وضع الحفر الجماعي 3×3 مُفعّل" : "⛏ وضع الحفر الفردي الدقيق مُفعّل").formatted(Formatting.GOLD), true);
            world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.4F, next ? 1.5F : 1.0F);
        }
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!world.isClient && world instanceof ServerWorld sw && miner instanceof PlayerEntity player && isAreaMode(stack) && !player.isSneaking()) {
            Direction facing = player.getHorizontalFacing();
            boolean vertical = Math.abs(player.getPitch()) > 55.0F;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    BlockPos target = vertical ? pos.add(dx, 0, dy)
                        : (facing == Direction.NORTH || facing == Direction.SOUTH) ? pos.add(dx, dy, 0) : pos.add(0, dy, dx);
                    BlockState bs = sw.getBlockState(target);
                    if (!bs.isAir() && bs.getHardness(sw, target) >= 0 && bs.getHardness(sw, target) <= 55.0F) {
                        Block.dropStacks(bs, sw, target, sw.getBlockEntity(target), player, stack);
                        sw.removeBlock(target, false);
                    }
                }
            }
        }
        return super.postMine(stack, world, state, pos, miner);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("✦ معول حداد الآلهة هيفايستوس — إرث أولمبي وحيد").formatted(Formatting.GOLD, Formatting.BOLD));
        tooltip.add(Text.literal("زر الفأرة الأيمن: التبديل بين التعدين الفردي و 3×3").formatted(Formatting.YELLOW));
        tooltip.add(Text.literal("⚡ مرتبط بالروح أبدياً (لا يُكسر ولا يضيع)").formatted(Formatting.LIGHT_PURPLE));
    }
}
