package com.olympus.aether.item;

import com.olympus.aether.OlympusMod;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MaceItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class TremorMaceOfHadesItem extends MaceItem implements DivineRelic {
    public TremorMaceOfHadesItem(Settings settings) {
        super(settings.attributeModifiers(
            AttributeModifiersComponent.builder()
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE,
                    new EntityAttributeModifier(Identifier.of(OlympusMod.MOD_ID, "hades_mace_damage"), 18.0, EntityAttributeModifier.Operation.ADD_VALUE),
                    AttributeModifierSlot.MAINHAND)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED,
                    new EntityAttributeModifier(Identifier.of(OlympusMod.MOD_ID, "hades_mace_speed"), -2.7, EntityAttributeModifier.Operation.ADD_VALUE),
                    AttributeModifierSlot.MAINHAND)
                .build()
        ));
    }

    @Override public int getOdysseyStage() { return 9; }
    @Override public String getOlympianGod() { return "هاديس (Hades)"; }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient && entity instanceof PlayerEntity p) DivineRelic.bindOwner(stack, p);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.getWorld().isClient && attacker.getWorld() instanceof ServerWorld sw) {
            float fallDist = attacker.fallDistance;
            if (fallDist > 2.0F) {
                float bonusDamage = fallDist * 6.5F;
                target.damage(sw.getDamageSources().mobAttack(attacker), bonusDamage);
                attacker.fallDistance = 0.0F;
                attacker.setVelocity(attacker.getVelocity().x, 0.55, attacker.getVelocity().z);
                attacker.velocityModified = true;

                Box area = target.getBoundingBox().expand(6.5, 3.0, 6.5);
                for (LivingEntity nearby : sw.getEntitiesByClass(LivingEntity.class, area, e -> e != attacker && e.isAlive())) {
                    Vec3d diff = nearby.getPos().subtract(target.getPos());
                    Vec3d push = (diff.lengthSquared() > 0.001 ? diff.normalize() : new Vec3d(0, 1, 0)).multiply(1.4).add(0, 0.75, 0);
                    nearby.setVelocity(push);
                    nearby.velocityModified = true;
                    if (nearby != target) {
                        nearby.damage(sw.getDamageSources().magic(), 10.0F + fallDist * 1.5F);
                    }
                }
                sw.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 0.5, target.getZ(), 45, 2.5, 0.5, 2.5, 0.12);
                sw.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, target.getX(), target.getY() + 0.5, target.getZ(), 1, 0, 0, 0, 0);
                sw.playSound(null, target.getBlockPos(), SoundEvents.ITEM_MACE_SMASH_GROUND_HEAVY, SoundCategory.PLAYERS, 1.5F, 0.85F);
            }
        }
        return super.postHit(stack, target, attacker);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("✦ صولجان زلزال تارتاروس لهاديس — إرث أولمبي وحيد").formatted(Formatting.GOLD, Formatting.BOLD));
        tooltip.add(Text.literal("يتضاعف الضرر بلا حد مع ارتفاع السقوط + موجة أرواح زرقاء زلزالية").formatted(Formatting.DARK_AQUA));
        tooltip.add(Text.literal("⚡ مرتبط بالروح أبدياً (لا يُكسر ولا يضيع)").formatted(Formatting.LIGHT_PURPLE));
    }
}
