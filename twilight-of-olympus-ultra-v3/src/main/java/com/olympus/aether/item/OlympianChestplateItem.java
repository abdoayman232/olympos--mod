package com.olympus.aether.item;

import net.fabricmc.fabric.api.entity.event.v1.FabricElytraItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class OlympianChestplateItem extends OlympianArmorItem implements FabricElytraItem {
    public OlympianChestplateItem(RegistryEntry<ArmorMaterial> material, Settings settings) {
        super(material, Type.CHESTPLATE, settings, 11, "زيوس ونايكي (Zeus & Nike)",
            "أعلى حماية في العالم + أجنحة نورانية مدمجة (Shift أثناء الطيران للدفع الهوائي بلا ألعاب نارية)");
    }

    @Override
    public boolean useCustomElytra(LivingEntity entity, ItemStack chestStack, boolean tickElytra) {
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        if (world.isClient || !(entity instanceof PlayerEntity player)) return;
        if (player.getEquippedStack(EquipmentSlot.CHEST) != stack) return;

        if (world.getTime() % 10 == 0) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 260, 0, true, false, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 260, 2, true, false, false));
        }

        if (player.isFallFlying()) {
            if (world instanceof ServerWorld sw) {
                Vec3d back = player.getRotationVector().multiply(-0.6);
                sw.spawnParticles(ParticleTypes.END_ROD, player.getX() + back.x, player.getY() + 0.8 + back.y, player.getZ() + back.z, 3, 0.35, 0.15, 0.35, 0.01);
                sw.spawnParticles(ParticleTypes.FLAME, player.getX() + back.x, player.getY() + 0.7 + back.y, player.getZ() + back.z, 1, 0.25, 0.1, 0.25, 0.01);
            }
            if (player.isSneaking()) {
                Vec3d look = player.getRotationVector();
                Vec3d boosted = player.getVelocity().add(look.x * 0.14, look.y * 0.14 + 0.03, look.z * 0.14);
                if (boosted.length() > 2.2) boosted = boosted.normalize().multiply(2.2);
                player.setVelocity(boosted);
                player.velocityModified = true;
                if (world.getTime() % 15 == 0) {
                    world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PHANTOM_FLAP, SoundCategory.PLAYERS, 0.6f, 1.4f);
                }
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        tooltip.add(Text.literal("🕊 أجنحة الأولمب المدمجة: طيران حر + Shift للدفع السماوي").formatted(Formatting.YELLOW));
    }
}
