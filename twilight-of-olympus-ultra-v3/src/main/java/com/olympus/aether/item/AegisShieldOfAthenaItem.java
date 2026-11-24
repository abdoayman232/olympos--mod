package com.olympus.aether.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;

public class AegisShieldOfAthenaItem extends ShieldItem implements DivineRelic {
    public AegisShieldOfAthenaItem(Settings settings) {
        super(settings);
    }

    @Override public int getOdysseyStage() { return 7; }
    @Override public String getOlympianGod() { return "أثينا (Athena)"; }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient && entity instanceof PlayerEntity p) {
            DivineRelic.bindOwner(stack, p);
            if (p.getItemCooldownManager().isCoolingDown(this)) {
                p.getItemCooldownManager().remove(this);
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("✦ درع أثينا والغورغون — إرث أولمبي وحيد").formatted(Formatting.GOLD, Formatting.BOLD));
        tooltip.add(Text.literal("يصد 100% من الضرر (حتى صرخة الـ Warden) ولا تعطلّه الفؤوس").formatted(Formatting.AQUA));
        tooltip.add(Text.literal("الصد المرتد: يُحجّر المهاجم ويعكس 50% من الضرر عليه!").formatted(Formatting.YELLOW));
        tooltip.add(Text.literal("⚡ مرتبط بالروح أبدياً (لا يُكسر ولا يضيع)").formatted(Formatting.LIGHT_PURPLE));
    }
}
