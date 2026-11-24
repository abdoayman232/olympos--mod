package com.olympus.aether.block;

import com.olympus.aether.lore.OlympusLoreLibrary;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class WarSteleBlock extends Block {
    public WarSteleBlock(Settings settings) { super(settings); }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(3) == 0) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 1.1;
            double y = pos.getY() + 0.3 + random.nextDouble() * 0.9;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 1.1;
            world.addParticle(random.nextBoolean() ? ParticleTypes.CRIMSON_SPORE : ParticleTypes.ENCHANT, x, y, z, 0.0, 0.04, 0.0);
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient && world instanceof ServerWorld sw) {
            int idx = Math.abs((pos.getX() * 31 + pos.getZ() * 17)) % OlympusLoreLibrary.WAR_STELE_VISIONS.size();
            String vision = OlympusLoreLibrary.WAR_STELE_VISIONS.get(idx);
            player.sendMessage(Text.literal("§8══════════════════════════════════"), false);
            player.sendMessage(Text.literal(vision), false);
            player.sendMessage(Text.literal("§8══════════════════════════════════"), false);
            sw.spawnParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 35, 0.6, 0.8, 0.6, 0.15);
            sw.spawnParticles(ParticleTypes.DRAGON_BREATH, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 12, 0.4, 0.5, 0.4, 0.04);
            sw.playSound(null, pos, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 0.8F, 1.3F);
        }
        return ActionResult.SUCCESS;
    }
}
