package com.olympus.aether.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.olympus.aether.registry.ModItems;
import com.olympus.aether.system.AetherCoreSystem;
import com.olympus.aether.world.OlympusShrineManager;
import com.olympus.aether.world.OlympusWorldState;
import com.olympus.aether.world.WarMarksGenerator;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class OlympusCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("olympus")
                .requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.literal("give_all").executes(ctx -> {
                    ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                    p.equipStack(EquipmentSlot.HEAD, new ItemStack(ModItems.HELM_OF_POSEIDON));
                    p.equipStack(EquipmentSlot.CHEST, new ItemStack(ModItems.AEGIS_CHESTPLATE_OF_ZEUS));
                    p.equipStack(EquipmentSlot.LEGS, new ItemStack(ModItems.GREAVES_OF_ARES));
                    p.equipStack(EquipmentSlot.FEET, new ItemStack(ModItems.TALARIA_OF_HERMES));
                    p.equipStack(EquipmentSlot.OFFHAND, new ItemStack(ModItems.AEGIS_SHIELD_OF_ATHENA));
                    p.getInventory().offerOrDrop(new ItemStack(ModItems.THUNDERBOLT_SPEAR_OF_ZEUS));
                    p.getInventory().offerOrDrop(new ItemStack(ModItems.SUNBLADE_OF_APOLLO));
                    p.getInventory().offerOrDrop(new ItemStack(ModItems.TREMOR_MACE_OF_HADES));
                    p.getInventory().offerOrDrop(new ItemStack(ModItems.MOONBOW_OF_ARTEMIS));
                    p.getInventory().offerOrDrop(new ItemStack(ModItems.FORGE_PICKAXE_OF_HEPHAESTUS));
                    p.getInventory().offerOrDrop(new ItemStack(ModItems.HEARTH_GREATAXE_OF_HESTIA));
                    p.getInventory().offerOrDrop(new ItemStack(ModItems.TERRA_SHOVEL_OF_DEMETER));
                    p.sendMessage(Text.literal("§6§l✦ تم تجهيزك بالطقم الأولمبي الأسطوري الكامل مع الأجنحة الذهبية 3D!"), false);
                    return 1;
                }))
                .then(CommandManager.literal("awaken_aether").executes(ctx -> {
                    ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                    OlympusWorldState ws = OlympusWorldState.get(p.getServer());
                    AetherCoreSystem.awakenAetherCore(p, ws);
                    AetherCoreSystem.upgradeCoreLayer(p, ws);
                    AetherCoreSystem.upgradeCoreLayer(p, ws);
                    return 1;
                }))
                .then(CommandManager.literal("spawn_warmark").executes(ctx -> {
                    ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                    WarMarksGenerator.spawnWarMark(p.getServerWorld(), p.getBlockPos().east(8), p.getRandom().nextInt(3));
                    p.sendMessage(Text.literal("§6✦ تم توليد معلم أثري من ندوب الحرب أمامك!"), false);
                    return 1;
                }))
                .then(CommandManager.literal("spawn_shrine")
                    .then(CommandManager.argument("stage", IntegerArgumentType.integer(1, 12)).executes(ctx -> {
                        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                        int stage = IntegerArgumentType.getInteger(ctx, "stage");
                        OlympusShrineManager.buildShrineAt(p.getServerWorld(), p.getBlockPos().north(10), stage);
                        p.sendMessage(Text.literal("§6✦ تم بناء ضريح المحطة " + stage + " بالختم البنفسجي والصندوق أمامك!"), false);
                        return 1;
                    }))
                )
            );
        });
    }
}
