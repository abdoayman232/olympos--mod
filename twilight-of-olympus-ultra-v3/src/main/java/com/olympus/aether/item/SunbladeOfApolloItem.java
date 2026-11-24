package com.olympus.aether.item;

import com.olympus.aether.registry.ModToolMaterials;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class SunbladeOfApolloItem extends SwordItem implements DivineRelic {
    public SunbladeOfApolloItem(Settings settings) {
        super(ModToolMaterials.OLYMPIAN, settings.attributeModifiers(
            SwordItem.createAttributeModifiers(ModToolMaterials.OLYMPIAN, 14, -2.0f)
        ));
    }

    @Override public int getOdysseyStage() { return 5; }
    @Override public String getOlympianGod() { return "أبولو (Apollo)"; }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient && entity instanceof PlayerEntity p) DivineRelic.bindOwner(stack, p);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.setOnFireFor(8.0F);
        if (attacker.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1.0, target.getZ(), 12, 0.4, 0.5, 0.4, 0.05);
        }
        return super.postHit(stack, target, attacker);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient && world instanceof ServerWorld sw) {
            Vec3d start = user.getEyePos();
            Vec3d dir = user.getRotationVector().normalize();
            for (int i = 1; i <= 24; i++) {
                Vec3d point = start.add(dir.multiply(i * 0.9));
                sw.spawnParticles(ParticleTypes.FLAME, point.x, point.y, point.z, 5, 0.4, 0.25, 0.4, 0.02);
                sw.spawnParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 2, 0.3, 0.2, 0.3, 0.01);
                Box hitBox = new Box(point.subtract(1.4, 1.4, 1.4), point.add(1.4, 1.4, 1.4));
                for (LivingEntity e : sw.getEntitiesByClass(LivingEntity.class, hitBox, ent -> ent != user && ent.isAlive())) {
                    e.damage(sw.getDamageSources().playerAttack(user), 22.0F);
                    e.setOnFireFor(10.0F);
                }
            }
            sw.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.2F);
            user.getItemCooldownManager().set(this, 45);
        }
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("✦ نصل أبولو الشمسي — إرث أولمبي وحيد").formatted(Formatting.GOLD, Formatting.BOLD));
        tooltip.add(Text.literal("زر الفأرة الأيمن: إطلاق هلال طاقة شمسية يخترق الوحوش").formatted(Formatting.YELLOW));
        tooltip.add(Text.literal("⚡ مرتبط بالروح أبدياً (لا يُكسر ولا يضيع)").formatted(Formatting.LIGHT_PURPLE));
    }
}
