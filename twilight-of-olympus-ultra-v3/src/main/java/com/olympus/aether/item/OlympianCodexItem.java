package com.olympus.aether.item;

import com.olympus.aether.lore.OlympusLoreLibrary;
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

public class OlympianCodexItem extends Item implements DivineRelic {
    public OlympianCodexItem(Settings settings) { super(settings); }
    @Override public int getOdysseyStage() { return 0; }
    @Override public String getOlympianGod() { return "سجل الأولمب"; }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient && entity instanceof PlayerEntity p) DivineRelic.bindOwner(stack, p);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient && user instanceof ServerPlayerEntity sp) {
            OlympusWorldState ws = OlympusWorldState.get(sp.getServer());
            sp.sendMessage(Text.literal("§6§l═══ سجل الأوديسة والملحمة الإغريقية ═══"), false);
            for (int i = 1; i <= 12; i++) {
                OlympusLoreLibrary.StageMeta m = OlympusLoreLibrary.getStage(i);
                String status = ws.claimedRelics.contains(i) ? "§a✔ مكتمل" : (i == ws.currentStage ? "§e▶ المحطة الحالية" : "§8🔒 مختوم");
                sp.sendMessage(Text.literal(status + " §f[" + i + "] " + m.relicNameAr() + " §7— " + m.structureHintAr()), false);
            }
            OlympusWorldState.AetherData ad = ws.getOrCreateAether(sp.getUuid());
            sp.sendMessage(Text.literal(ad.unlocked
                ? "§5✦ نواة الإيثر: مُستيقظة (الطبقة " + ad.coreLayer + " | البصيرة: " + ad.insightXp + ")"
                : "§8✦ نواة الإيثر: نائمة (تستيقظ بعد إكمال الختم 12)"), false);
        }
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("✦ سجل القصة والمحطات الإثنتي عشرة").formatted(Formatting.GOLD, Formatting.BOLD));
        tooltip.add(Text.literal("زر الفأرة الأيمن: استعراض تقدمك في الأوديسة وحالة نواة الإيثر").formatted(Formatting.AQUA));
    }
}
