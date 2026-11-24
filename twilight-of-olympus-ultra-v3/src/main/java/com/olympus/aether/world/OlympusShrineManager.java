package com.olympus.aether.world;

import com.olympus.aether.block.OlympianSealBlock;
import com.olympus.aether.lore.OlympusLoreLibrary;
import com.olympus.aether.registry.ModBlocks;
import com.olympus.aether.registry.ModItems;
import net.minecraft.block.Blocks;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class OlympusShrineManager {

    public static void generateUniqueShrine(ServerWorld world, BlockPos origin, int stage, OlympusWorldState ws) {
        if (ws.generatedShrines.contains(stage)) return;
        ws.generatedShrines.add(stage);
        ws.markDirty();
        buildShrineAt(world, origin, stage);
    }

    public static void buildShrineAt(ServerWorld world, BlockPos origin, int stage) {
        for (int x = -5; x <= 5; x++) {
            for (int y = 0; y <= 7; y++) {
                for (int z = -5; z <= 5; z++) {
                    BlockPos p = origin.add(x, y, z);
                    boolean border = Math.abs(x) == 5 || Math.abs(z) == 5 || y == 0 || y == 7;
                    if (!border) {
                        world.setBlockState(p, Blocks.AIR.getDefaultState());
                    } else if (y == 0) {
                        world.setBlockState(p, (Math.abs(x) + Math.abs(z)) % 2 == 0
                            ? Blocks.QUARTZ_BRICKS.getDefaultState()
                            : Blocks.GILDED_BLACKSTONE.getDefaultState());
                    } else if (y == 7 && Math.abs(x) <= 1 && Math.abs(z) <= 1) {
                        world.setBlockState(p, Blocks.AIR.getDefaultState());
                    } else if (Math.abs(x) == 5 && Math.abs(z) == 5) {
                        world.setBlockState(p, y == 1 || y == 6 ? Blocks.GOLD_BLOCK.getDefaultState() : Blocks.QUARTZ_PILLAR.getDefaultState());
                    } else {
                        world.setBlockState(p, stage == 8 || stage == 9
                            ? Blocks.POLISHED_BLACKSTONE_BRICKS.getDefaultState()
                            : stage == 7 ? Blocks.DEEPSLATE_TILES.getDefaultState()
                            : Blocks.QUARTZ_PILLAR.getDefaultState());
                    }
                }
            }
        }

        world.setBlockState(origin.add(-3, 1, -2), Blocks.CAMPFIRE.getDefaultState());
        world.setBlockState(origin.add(3, 1, -2), Blocks.CAMPFIRE.getDefaultState());

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 1; dy <= 3; dy++) {
                world.setBlockState(origin.add(dx, dy, -5),
                    ModBlocks.OLYMPIAN_SEAL.getDefaultState().with(OlympianSealBlock.STAGE, stage));
            }
        }

        world.setBlockState(origin.add(0, 1, 0), Blocks.GOLD_BLOCK.getDefaultState());
        BlockPos chestPos = origin.add(0, 2, 0);
        world.setBlockState(chestPos, Blocks.CHEST.getDefaultState());

        if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setStack(13, new ItemStack(ModItems.getRelicByStage(stage)));
            chest.setStack(4,  new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
            chest.setStack(12, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
            chest.setStack(14, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
            chest.setStack(22, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
            int[] goldenFrame = {0, 3, 5, 8, 11, 15, 18, 21, 23, 26};
            for (int slot : goldenFrame) {
                chest.setStack(slot, new ItemStack(Items.GOLDEN_APPLE));
            }
        }

        BlockPos lecternPos = origin.add(2, 1, 0);
        world.setBlockState(lecternPos, Blocks.LECTERN.getDefaultState().with(LecternBlock.HAS_BOOK, true));
        if (world.getBlockEntity(lecternPos) instanceof LecternBlockEntity lectern) {
            lectern.setBook(OlympusLoreLibrary.createScrollBook(stage));
        }

        world.setBlockState(origin.add(-2, 1, 0), ModBlocks.WAR_STELE.getDefaultState());
    }
}
