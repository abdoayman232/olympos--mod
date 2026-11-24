package com.olympus.aether.registry;

import com.olympus.aether.OlympusMod;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.EnumMap;
import java.util.List;

public class ModArmorMaterials {
    public static final RegistryEntry<ArmorMaterial> OLYMPIAN = Registry.registerReference(
        Registries.ARMOR_MATERIAL,
        Identifier.of(OlympusMod.MOD_ID, "olympian"),
        new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 7);
                map.put(ArmorItem.Type.LEGGINGS, 10);
                map.put(ArmorItem.Type.CHESTPLATE, 14);
                map.put(ArmorItem.Type.HELMET, 7);
                map.put(ArmorItem.Type.BODY, 16);
            }),
            35,
            SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE,
            () -> Ingredient.ofItems(Items.ENCHANTED_GOLDEN_APPLE),
            List.of(new ArmorMaterial.Layer(Identifier.of(OlympusMod.MOD_ID, "olympian"))),
            8.0F,
            0.35F
        )
    );

    public static void register() {
        OlympusMod.LOGGER.info("Registered Olympian Armor Material.");
    }
}
