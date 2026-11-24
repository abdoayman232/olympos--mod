package com.olympus.aether.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class MoonbowOfArtemisItem extends BowItem implements DivineRelic {
    public MoonbowOfArtemisItem(Settings settings) {
        super(settings);
    }

    @Override public int getOdysseyStage() { return 2; }
    @Override public String getOlympianGod() { return "أرتميس (Artemis)"; }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient && entity instanceof PlayerEntity p) DivineRelic.bindOwner(stack, p);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand);
        return TypedActionResult.consume(user.getStackInHand(hand));
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return;
        int used = this.getMaxUseTime(stack, user) - remainingUseTicks;
        float pull = getPullProgress(used);
        if (pull < 0.1F) return;

        if (!world.isClient && world instanceof ServerWorld sw) {
            ArrowEntity arrow = new ArrowEntity(world, player, new ItemStack(Items.ARROW), stack);
            arrow.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, pull * 4.2F, 0.0F);
            arrow.setNoGravity(true);
            arrow.setCritical(true);
            arrow.setDamage(9.5D);
            arrow.addEffect(new StatusEffectInstance(StatusEffects.GLOWING, 300, 0));
            arrow.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
            sw.spawnEntity(arrow);
            sw.spawnParticles(ParticleTypes.END_ROD, player.getX(), player.getEyeY(), player.getZ(), 8, 0.2, 0.2, 0.2, 0.05);
            sw.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.5F);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("✦ قوس أرتميس القمري — إرث أولمبي وحيد").formatted(Formatting.GOLD, Formatting.BOLD));
        tooltip.add(Text.literal("أسهم نور فضية لا نهائية بلا جاذبية وتكشف الأعداء بتوهج قمري").formatted(Formatting.AQUA));
        tooltip.add(Text.literal("⚡ مرتبط بالروح أبدياً (لا يُكسر ولا يضيع)").formatted(Formatting.LIGHT_PURPLE));
    }
}
