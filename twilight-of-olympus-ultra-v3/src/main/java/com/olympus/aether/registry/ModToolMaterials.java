package com.olympus.aether.registry;

import net.minecraft.block.Block;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

public enum ModToolMaterials implements ToolMaterial {
    OLYMPIAN;

    @Override public int getDurability() { return 99999; }
    @Override public float getMiningSpeedMultiplier() { return 24.0F; }
    @Override public float getAttackDamage() { return 10.0F; }
    @Override public TagKey<Block> getInverseTag() { return BlockTags.INCORRECT_FOR_NETHERITE_TOOL; }
    @Override public int getEnchantability() { return 35; }
    @Override public Ingredient getRepairIngredient() { return Ingredient.ofItems(Items.ENCHANTED_GOLDEN_APPLE); }
}
