package com.olympus.aether.registry;

import com.olympus.aether.OlympusMod;
import com.olympus.aether.item.*;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ModItems {

    private static Item.Settings divineSettings() {
        return new Item.Settings()
            .maxCount(1)
            .fireproof()
            .rarity(Rarity.EPIC)
            .component(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(true))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    public static final Item TALARIA_OF_HERMES = register("talaria_of_hermes",
        new OlympianArmorItem(ModArmorMaterials.OLYMPIAN, ArmorItem.Type.BOOTS, divineSettings(),
            1, "هرمز (Hermes)", "سرعة فائقة (Speed III) + إلغاء ضرر السقوط نهائياً + تخطي البلوكات تلقائياً"));

    public static final Item HELM_OF_POSEIDON = register("helm_of_poseidon",
        new OlympianArmorItem(ModArmorMaterials.OLYMPIAN, ArmorItem.Type.HELMET, divineSettings(),
            6, "بوسيدون (Poseidon)", "تنفس أبدي تحت الماء + تعدين فائق (Haste III) + رؤية ليلية نقية + حماية قصوى من الانفجارات"));

    public static final Item GREAVES_OF_ARES = register("greaves_of_ares",
        new OlympianArmorItem(ModArmorMaterials.OLYMPIAN, ArmorItem.Type.LEGGINGS, divineSettings(),
            8, "آريس (Ares)", "قوة هجومية دائمة (Strength III) + ثبات كامل ضد الارتداد 100% + حماية فائقة"));

    public static final Item AEGIS_CHESTPLATE_OF_ZEUS = register("aegis_chestplate_of_zeus",
        new OlympianChestplateItem(ModArmorMaterials.OLYMPIAN, divineSettings()));

    public static final Item MOONBOW_OF_ARTEMIS = register("moonbow_of_artemis", new MoonbowOfArtemisItem(divineSettings()));
    public static final Item FORGE_PICKAXE_OF_HEPHAESTUS = register("forge_pickaxe_of_hephaestus", new ForgePickaxeItem(divineSettings()));
    public static final Item HEARTH_GREATAXE_OF_HESTIA = register("hearth_greataxe_of_hestia", new HearthGreataxeItem(divineSettings()));
    public static final Item SUNBLADE_OF_APOLLO = register("sunblade_of_apollo", new SunbladeOfApolloItem(divineSettings()));
    public static final Item AEGIS_SHIELD_OF_ATHENA = register("aegis_shield_of_athena", new AegisShieldOfAthenaItem(divineSettings()));
    public static final Item TREMOR_MACE_OF_HADES = register("tremor_mace_of_hades", new TremorMaceOfHadesItem(divineSettings()));
    public static final Item THUNDERBOLT_SPEAR_OF_ZEUS = register("thunderbolt_spear_of_zeus", new ThunderboltSpearItem(divineSettings()));
    public static final Item TERRA_SHOVEL_OF_DEMETER = register("terra_shovel_of_demeter", new TerraShovelItem(divineSettings()));

    public static final Item ODYSSEY_COMPASS = register("odyssey_compass", new OdysseyCompassItem(divineSettings()));
    public static final Item OLYMPIAN_CODEX = register("olympian_codex", new OlympianCodexItem(divineSettings()));
    public static final Item AETHER_FOCUS = register("aether_focus", new AetherFocusItem(divineSettings()));
    public static final Item AETHER_KEYSTONE = register("aether_keystone", new AetherKeystoneItem(new Item.Settings().maxCount(3).fireproof().rarity(Rarity.EPIC)));

    public static final ItemGroup OLYMPUS_GROUP = Registry.register(
        Registries.ITEM_GROUP,
        Identifier.of(OlympusMod.MOD_ID, "olympus_group"),
        FabricItemGroup.builder()
            .icon(() -> new ItemStack(AEGIS_CHESTPLATE_OF_ZEUS))
            .displayName(Text.translatable("itemGroup.olympus_aether.olympus_group"))
            .entries((displayContext, entries) -> {
                entries.add(ODYSSEY_COMPASS);
                entries.add(OLYMPIAN_CODEX);
                entries.add(HELM_OF_POSEIDON);
                entries.add(AEGIS_CHESTPLATE_OF_ZEUS);
                entries.add(GREAVES_OF_ARES);
                entries.add(TALARIA_OF_HERMES);
                entries.add(SUNBLADE_OF_APOLLO);
                entries.add(THUNDERBOLT_SPEAR_OF_ZEUS);
                entries.add(TREMOR_MACE_OF_HADES);
                entries.add(AEGIS_SHIELD_OF_ATHENA);
                entries.add(MOONBOW_OF_ARTEMIS);
                entries.add(FORGE_PICKAXE_OF_HEPHAESTUS);
                entries.add(HEARTH_GREATAXE_OF_HESTIA);
                entries.add(TERRA_SHOVEL_OF_DEMETER);
                entries.add(AETHER_FOCUS);
                entries.add(AETHER_KEYSTONE);
                entries.add(ModBlocks.WAR_STELE);
                entries.add(ModBlocks.OLYMPIAN_SEAL);
            })
            .build()
    );

    public static Item getRelicByStage(int stage) {
        return switch (stage) {
            case 1 -> TALARIA_OF_HERMES;
            case 2 -> MOONBOW_OF_ARTEMIS;
            case 3 -> FORGE_PICKAXE_OF_HEPHAESTUS;
            case 4 -> HEARTH_GREATAXE_OF_HESTIA;
            case 5 -> SUNBLADE_OF_APOLLO;
            case 6 -> HELM_OF_POSEIDON;
            case 7 -> AEGIS_SHIELD_OF_ATHENA;
            case 8 -> GREAVES_OF_ARES;
            case 9 -> TREMOR_MACE_OF_HADES;
            case 10 -> THUNDERBOLT_SPEAR_OF_ZEUS;
            case 11 -> AEGIS_CHESTPLATE_OF_ZEUS;
            default -> TERRA_SHOVEL_OF_DEMETER;
        };
    }

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(OlympusMod.MOD_ID, name), item);
    }

    public static void register() {
        OlympusMod.LOGGER.info("Registered all 12 Olympian Relics + Odyssey & Aether items.");
    }
}
