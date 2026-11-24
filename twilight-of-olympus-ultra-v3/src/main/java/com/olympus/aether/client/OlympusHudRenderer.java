package com.olympus.aether.client;

import com.olympus.aether.registry.ModItems;
import com.olympus.aether.system.AetherCoreSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class OlympusHudRenderer {

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null || client.options.hudHidden) return;

        TextRenderer font = client.textRenderer;
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();

        List<String> powers = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        if (player.getEquippedStack(EquipmentSlot.FEET).isOf(ModItems.TALARIA_OF_HERMES)) {
            powers.add("Speed III ✦");
            colors.add(0x55FFFF);
        }
        if (player.getEquippedStack(EquipmentSlot.LEGS).isOf(ModItems.GREAVES_OF_ARES)) {
            powers.add("Strength III ⚔");
            colors.add(0xFF5555);
        }
        if (player.getEquippedStack(EquipmentSlot.CHEST).isOf(ModItems.AEGIS_CHESTPLATE_OF_ZEUS)) {
            powers.add("Flight 🕊");
            colors.add(0xFFD740);
        }
        if (player.getEquippedStack(EquipmentSlot.HEAD).isOf(ModItems.HELM_OF_POSEIDON)) {
            powers.add("Abyss Sight 🔱");
            colors.add(0x46F0FF);
        }
        if (ClientAetherState.unlocked) {
            powers.add("Aether L" + ClientAetherState.coreLayer + " ◈");
            colors.add(0xD866FF);
        }

        if (!powers.isEmpty()) {
            int rowH = 13;
            int startY = sh - (powers.size() * rowH) - 10;
            for (int i = 0; i < powers.size(); i++) {
                String label = powers.get(i);
                int w = font.getWidth(label);
                int x = sw - w - 10;
                int y = startY + i * rowH;
                ctx.fill(x - 5, y - 2, sw - 5, y + 10, 0x88100C1C);
                ctx.drawTextWithShadow(font, label, x, y, colors.get(i));
            }
        }

        if (ClientAetherState.unlocked) {
            int barW = 136;
            int barH = 8;
            int bx = 10;
            int by = sh - 26;

            String layerAr = switch (ClientAetherState.coreLayer) {
                case 1 -> "الطبقة الأولى";
                case 2 -> "الطبقة الثانية";
                default -> "الطبقة الثالثة";
            };
            String header = "✦ نواة الإيثر (" + layerAr + ") [" + ClientAetherState.aether + "/" + ClientAetherState.maxAether + "]";
            ctx.drawTextWithShadow(font, header, bx, by - 12, 0xE288FF);

            ctx.fill(bx - 1, by - 1, bx + barW + 1, by + barH + 1, 0xFFD4AF37);
            ctx.fill(bx, by, bx + barW, by + barH, 0xFF1A082E);

            float ratio = Math.max(0.0F, Math.min(1.0F, (float) ClientAetherState.aether / Math.max(1, ClientAetherState.maxAether)));
            int fillW = (int) (barW * ratio);
            int barCol = ClientAetherState.destructionActive ? 0xFFFF2A85 : 0xFFB83DFF;
            ctx.fill(bx, by, bx + fillW, by + barH, barCol);
            ctx.fill(bx, by, bx + fillW, by + 2, 0xFFF2C8FF);

            int runeIdx = Math.max(0, Math.min(AetherCoreSystem.RUNE_NAMES.length - 1, ClientAetherState.activeRune));
            ctx.drawTextWithShadow(font, "◈ " + AetherCoreSystem.RUNE_NAMES[runeIdx], bx, by + 11, 0xC8A8FF);
        }
    }
}
