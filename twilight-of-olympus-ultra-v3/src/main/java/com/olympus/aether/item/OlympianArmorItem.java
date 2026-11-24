package com.olympus.aether.item;

import com.olympus.aether.OlympusMod;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;

public class OlympianArmorItem extends ArmorItem implements DivineRelic {
    private final int stage;
    private final String godName;
    private final String loreSummary;

    public OlympianArmorItem(RegistryEntry<ArmorMaterial> material, Type type, Settings settings, int stage, String godName, String loreSummary) {
        super(material, type, settings);
        this.stage = stage;
        this.godName = godName;
        this.loreSummary = loreSummary;
    }

    @Override public int getOdysseyStage() { return stage; }
    @Override public String getOlympianGod() { return godName; }

    @Override
    public AttributeModifiersComponent getAttributeModifiers() {
        AttributeModifiersComponent base = super.getAttributeModifiers();
        AttributeModifiersComponent.Builder builder = AttributeModifiersComponent.builder();
        for (AttributeModifiersComponent.Entry entry : base.modifiers()) {
            builder.add(entry.attribute(), entry.modifier(), entry.slot());
        }
        if (this.type == Type.LEGGINGS) {
            builder.add(
                EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
                new EntityAttributeModifier(Identifier.of(OlympusMod.MOD_ID, "ares_knockback"), 1.0, EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.LEGS
            );
        } else if (this.type == Type.BOOTS) {
            builder.add(
                EntityAttributes.GENERIC_STEP_HEIGHT,
                new EntityAttributeModifier(Identifier.of(OlympusMod.MOD_ID, "hermes_step"), 0.65, EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.FEET
            );
        }
        return builder.build();
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient || !(entity instanceof PlayerEntity player)) return;
        DivineRelic.bindOwner(stack, player);
        if (world.getTime() % 10 != 0) return;

        EquipmentSlot reqSlot = this.type.getEquipmentSlot();
        if (player.getEquippedStack(reqSlot) != stack) return;

        if (this.type == Type.HELMET) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 260, 0, true, false, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 260, 2, true, false, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 340, 0, true, false, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.CONDUIT_POWER, 260, 0, true, false, false));
        } else if (this.type == Type.LEGGINGS) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 260, 2, true, false, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 260, 1, true, false, false));
        } else if (this.type == Type.BOOTS) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 260, 2, true, false, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 260, 0, true, false, false));
            if (player.isSprinting() && world instanceof ServerWorld sw) {
                sw.spawnParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 0.1, player.getZ(), 2, 0.2, 0.05, 0.2, 0.01);
            }
        }

        if (hasFullOlympianSet(player)) {
            player.removeStatusEffect(StatusEffects.POISON);
            player.removeStatusEffect(StatusEffects.WITHER);
            player.removeStatusEffect(StatusEffects.BLINDNESS);
            player.removeStatusEffect(StatusEffects.DARKNESS);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 260, 2, true, false, false));
            if (world instanceof ServerWorld sw && world.getTime() % 20 == 0) {
                sw.spawnParticles(ParticleTypes.WAX_ON, player.getX(), player.getY() + 1.1, player.getZ(), 3, 0.45, 0.6, 0.45, 0.02);
            }
        }
    }

    public static boolean hasFullOlympianSet(PlayerEntity player) {
        return player.getEquippedStack(EquipmentSlot.HEAD).getItem() instanceof DivineRelic
            && player.getEquippedStack(EquipmentSlot.CHEST).getItem() instanceof DivineRelic
            && player.getEquippedStack(EquipmentSlot.LEGS).getItem() instanceof DivineRelic
            && player.getEquippedStack(EquipmentSlot.FEET).getItem() instanceof DivineRelic;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("✦ " + godName + " — إرث أولمبي وحيد في العالم").formatted(Formatting.GOLD, Formatting.BOLD));
        tooltip.add(Text.literal(loreSummary).formatted(Formatting.AQUA));
        tooltip.add(Text.literal("⚡ مرتبط بالروح أبدياً (لا يُكسر ولا يضيع)").formatted(Formatting.LIGHT_PURPLE));
    }
}
