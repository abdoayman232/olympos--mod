package com.olympus.aether.item;

import com.olympus.aether.OlympusMod;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class ThunderboltSpearItem extends Item implements DivineRelic {
    public ThunderboltSpearItem(Settings settings) {
        super(settings.attributeModifiers(
            AttributeModifiersComponent.builder()
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE,
                    new EntityAttributeModifier(Identifier.of(OlympusMod.MOD_ID, "zeus_spear_damage"), 21.0, EntityAttributeModifier.Operation.ADD_VALUE),
                    AttributeModifierSlot.MAINHAND)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED,
                    new EntityAttributeModifier(Identifier.of(OlympusMod.MOD_ID, "zeus_spear_speed"), -2.2, EntityAttributeModifier.Operation.ADD_VALUE),
                    AttributeModifierSlot.MAINHAND)
                .add(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE,
                    new EntityAttributeModifier(Identifier.of(OlympusMod.MOD_ID, "zeus_spear_reach"), 3.0, EntityAttributeModifier.Operation.ADD_VALUE),
                    AttributeModifierSlot.MAINHAND)
                .build()
        ));
    }

    @Override public int getOdysseyStage() { return 10; }
    @Override public String getOlympianGod() { return "زيوس (Zeus)"; }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient && entity instanceof PlayerEntity p) DivineRelic.bindOwner(stack, p);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient && world instanceof ServerWorld sw) {
            Vec3d start = user.getEyePos();
            Vec3d dir = user.getRotationVector().normalize();
            Vec3d impact = start.add(dir.multiply(32.0));

            for (int i = 1; i <= 36; i++) {
                Vec3d p = start.add(dir.multiply(i * 0.9));
                sw.spawnParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 6, 0.15, 0.15, 0.15, 0.08);
                sw.spawnParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 2, 0.08, 0.08, 0.08, 0.01);

                Box box = new Box(p.subtract(1.2, 1.2, 1.2), p.add(1.2, 1.2, 1.2));
                List<LivingEntity> targets = sw.getEntitiesByClass(LivingEntity.class, box, e -> e != user && e.isAlive());
                for (LivingEntity target : targets) {
                    target.damage(sw.getDamageSources().lightningBolt(), 26.0F);
                    impact = target.getPos();
                }
                if (!sw.getBlockState(BlockPos.ofFloored(p)).isAir()) {
                    impact = p;
                    break;
                }
            }

            LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(sw);
            if (bolt != null) {
                bolt.refreshPositionAfterTeleport(impact.x, impact.y, impact.z);
                sw.spawnEntity(bolt);
            }
            sw.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 0.9F, 1.2F);
            user.sendMessage(Text.literal("⚡ عاد رمح الصاعقة إلى يدك فوراً!").formatted(Formatting.AQUA), true);
            user.getItemCooldownManager().set(this, 35);
        }
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("✦ رمح الصاعقة لزيوس — إرث أولمبي وحيد").formatted(Formatting.GOLD, Formatting.BOLD));
        tooltip.add(Text.literal("مدى قتالي طويل (+3 بلوكات) + رمية برق تخترق الأعداء وتعود فوراً").formatted(Formatting.AQUA));
        tooltip.add(Text.literal("⚡ مرتبط بالروح أبدياً (لا يُكسر ولا يضيع)").formatted(Formatting.LIGHT_PURPLE));
    }
}
