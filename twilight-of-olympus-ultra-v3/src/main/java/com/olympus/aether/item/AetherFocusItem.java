package com.olympus.aether.item;

import com.olympus.aether.system.AetherCoreSystem;
import com.olympus.aether.world.OlympusWorldState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class AetherFocusItem extends Item implements DivineRelic {
    public AetherFocusItem(Settings settings) { super(settings); }
    @Override public int getOdysseyStage() { return 12; }
    @Override public String getOlympianGod() { return "إرث الإيثر (Aether Core)"; }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient && entity instanceof PlayerEntity p) DivineRelic.bindOwner(stack, p);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient && user instanceof ServerPlayerEntity sp) {
            OlympusWorldState ws = OlympusWorldState.get(sp.getServer());
            if (sp.isSneaking()) {
                AetherCoreSystem.cycleRune(sp, ws);
            } else {
                AetherCoreSystem.castActiveRune(sp, ws);
                sp.getItemCooldownManager().set(this, 12);
            }
        }
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("✦ بؤرة الإيثر البنفسجي (Godrunes Focus)").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        tooltip.add(Text.literal("Shift + زر أيمن: تبديل الحرف الإلهي (Godrune)").formatted(Formatting.LIGHT_PURPLE));
        tooltip.add(Text.literal("زر أيمن: تفعيل القدرة (God Step / Blade / Destruction / Aroa / Static Void / Regis)").formatted(Formatting.AQUA));
    }
}
