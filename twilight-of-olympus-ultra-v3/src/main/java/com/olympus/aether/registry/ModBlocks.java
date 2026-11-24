package com.olympus.aether.registry;

import com.olympus.aether.OlympusMod;
import com.olympus.aether.block.OlympianSealBlock;
import com.olympus.aether.block.WarSteleBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {
    public static final Block WAR_STELE = registerBlock("war_stele",
        new WarSteleBlock(AbstractBlock.Settings.create()
            .strength(5.0F, 1200.0F)
            .luminance(s -> 9)
            .sounds(BlockSoundGroup.DEEPSLATE_TILES)));

    public static final Block OLYMPIAN_SEAL = registerBlock("olympian_seal",
        new OlympianSealBlock(AbstractBlock.Settings.create()
            .strength(-1.0F, 3600000.0F)
            .luminance(s -> 15)
            .nonOpaque()
            .sounds(BlockSoundGroup.AMETHYST_BLOCK)));

    private static Block registerBlock(String name, Block block) {
        Identifier id = Identifier.of(OlympusMod.MOD_ID, name);
        Registry.register(Registries.BLOCK, id, block);
        Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
        return block;
    }

    public static void register() {
        OlympusMod.LOGGER.info("Registered Olympus War Stele & Sequential Seal blocks.");
    }
}
