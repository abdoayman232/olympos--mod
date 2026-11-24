package com.olympus.aether.item;

import com.olympus.aether.system.AetherCoreSystem;
import com.olympus.aether.world.OlympusWorldState;
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

public class AetherKeystoneItem extends Item {
    public AetherKeystoneItem(Settings settings) { super(settings); }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient && user instanceof ServerPlayerEntity sp) {
            OlympusWorldState ws = OlympusWorldState.get(sp.getServer());
            AetherCoreSystem.upgradeCoreLayer(sp, ws);
            if (!sp.isCreative()) stack.decrement(1);
        }
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("✦ حجر البصيرة الإيثري (Aether Keystone)").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        tooltip.add(Text.literal("استخدمه لترقية طبقات نواة الإيثر (Layer I → II → III)").formatted(Formatting.LIGHT_PURPLE));
    }
}
