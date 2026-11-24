package com.olympus.aether.block;

import com.olympus.aether.lore.OlympusLoreLibrary;
import com.olympus.aether.world.OlympusWorldState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class OlympianSealBlock extends Block {
    public static final IntProperty STAGE = IntProperty.of("stage", 1, 12);

    public OlympianSealBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(STAGE, 1));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        for (int i = 0; i < 2; i++) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 1.25;
            double y = pos.getY() + 0.6 + random.nextDouble() * 0.9;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 1.25;
            world.addParticle(i == 0 ? ParticleTypes.DRAGON_BREATH : ParticleTypes.WAX_ON, x, y, z, 0.0, 0.05, 0.0);
        }
        if (random.nextInt(4) == 0) {
            world.addParticle(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.35, pos.getZ() + 0.5,
                (random.nextDouble() - 0.5) * 0.6, 0.35, (random.nextDouble() - 0.5) * 0.6);
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient && world instanceof ServerWorld sw) {
            int requiredStage = state.get(STAGE);
            OlympusWorldState ws = OlympusWorldState.get(sw.getServer());
            if (ws.canUnlockStage(requiredStage)) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 2; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            BlockPos p = pos.add(dx, dy, dz);
                            if (sw.getBlockState(p).getBlock() instanceof OlympianSealBlock) {
                                sw.removeBlock(p, false);
                                sw.spawnParticles(ParticleTypes.DRAGON_BREATH, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 24, 0.5, 0.6, 0.5, 0.08);
                                sw.spawnParticles(ParticleTypes.WAX_ON, p.getX() + 0.5, p.getY() + 0.6, p.getZ() + 0.5, 16, 0.5, 0.5, 0.5, 0.06);
                                sw.spawnParticles(ParticleTypes.END_ROD, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 12, 0.4, 0.4, 0.4, 0.08);
                            }
                        }
                    }
                }
                sw.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.BLOCKS, 1.1F, 1.0F);
                player.sendMessage(Text.literal("⚡ انكسر ختم الأولمب للمحطة " + requiredStage + "! ادخل المذبح واستعد الإرث الإلهي.").formatted(Formatting.GOLD, Formatting.BOLD), false);
            } else {
                OlympusLoreLibrary.StageMeta prev = OlympusLoreLibrary.getStage(ws.currentStage);
                sw.playSound(null, pos, SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundCategory.BLOCKS, 1.0F, 0.6F);
                player.sendMessage(Text.literal("🔒 ختم الأولمب موصد! يجب أولاً إتمام المحطة " + ws.currentStage + ": إرث " + prev.godNameAr() + " (" + prev.structureHintAr() + ").").formatted(Formatting.RED), false);
            }
        }
        return ActionResult.SUCCESS;
    }
}
