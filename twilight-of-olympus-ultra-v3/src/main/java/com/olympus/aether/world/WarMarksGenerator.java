package com.olympus.aether.world;

import com.olympus.aether.registry.ModBlocks;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.Random;

public class WarMarksGenerator {

    public static void trySpawnWarMarkNear(ServerPlayerEntity player, OlympusWorldState ws) {
        ServerWorld sw = player.getServerWorld();
        if (!sw.getRegistryKey().equals(World.OVERWORLD)) return;

        int regX = Math.floorDiv(player.getBlockX(), 180);
        int regZ = Math.floorDiv(player.getBlockZ(), 180);
        long regionKey = (((long) regX) << 32) ^ (regZ & 0xFFFFFFFFL);
        if (!ws.warMarkRegions.add(regionKey)) return;
        ws.markDirty();

        Random rng = new Random(sw.getSeed() ^ regionKey);
        int ox = player.getBlockX() + (rng.nextBoolean() ? 1 : -1) * (28 + rng.nextInt(26));
        int oz = player.getBlockZ() + (rng.nextBoolean() ? 1 : -1) * (28 + rng.nextInt(26));
        int oy = sw.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ox, oz);
        if (oy < 60 || oy > 180) return;

        spawnWarMark(sw, new BlockPos(ox, oy, oz), rng.nextInt(3));
    }

    public static void spawnWarMark(ServerWorld sw, BlockPos origin, int type) {
        switch (type) {
            case 0 -> buildFulminatedCrater(sw, origin);
            case 1 -> buildFieldOfThePetrified(sw, origin);
            default -> buildFallenOlympusPillar(sw, origin);
        }
    }

    // 1. Fulminated Crater of Zeus (charred blackstone + gold veins + War Stele)
    private static void buildFulminatedCrater(ServerWorld sw, BlockPos c) {
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist <= 4.2) {
                    int depth = dist < 1.8 ? 2 : 1;
                    for (int dy = 0; dy <= 2; dy++) sw.setBlockState(c.add(dx, dy, dz), Blocks.AIR.getDefaultState());
                    for (int dy = 1; dy <= depth; dy++) sw.setBlockState(c.add(dx, -dy, dz), Blocks.AIR.getDefaultState());
                    BlockPos floor = c.add(dx, -(depth + 1), dz);
                    sw.setBlockState(floor, dist < 1.5 ? Blocks.GILDED_BLACKSTONE.getDefaultState() : Blocks.BLACKSTONE.getDefaultState());
                }
            }
        }
        sw.setBlockState(c.add(0, -2, 0), ModBlocks.WAR_STELE.getDefaultState());
        sw.setBlockState(c.add(1, -2, 0), Blocks.SOUL_CAMPFIRE.getDefaultState());
    }

    // 2. Field of the Petrified (Athena's Medusa Shield victims frozen in stone)
    private static void buildFieldOfThePetrified(ServerWorld sw, BlockPos c) {
        int[][] offsets = {{-2, -1}, {2, -2}, {0, 2}};
        for (int[] o : offsets) {
            BlockPos base = c.add(o[0], 0, o[1]);
            sw.setBlockState(base, Blocks.POLISHED_ANDESITE.getDefaultState());
            sw.setBlockState(base.up(1), Blocks.CHISELED_STONE_BRICKS.getDefaultState());
            sw.setBlockState(base.up(2), Blocks.CARVED_PUMPKIN.getDefaultState());
        }
        sw.setBlockState(c, ModBlocks.WAR_STELE.getDefaultState());
    }

    // 3. Fallen Olympus Pillar (toppled white quartz & gold column from the sky)
    private static void buildFallenOlympusPillar(ServerWorld sw, BlockPos c) {
        for (int i = 0; i < 6; i++) {
            sw.setBlockState(c.add(i, 0, 0), Blocks.QUARTZ_PILLAR.getDefaultState());
            sw.setBlockState(c.add(i, 1, 0), i == 0 || i == 5 ? Blocks.GOLD_BLOCK.getDefaultState() : Blocks.QUARTZ_SLAB.getDefaultState());
        }
        sw.setBlockState(c.add(2, 0, 2), ModBlocks.WAR_STELE.getDefaultState());
    }
}
