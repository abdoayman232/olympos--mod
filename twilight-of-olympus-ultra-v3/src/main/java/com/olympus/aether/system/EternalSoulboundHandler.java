package com.olympus.aether.system;

import com.olympus.aether.item.DivineRelic;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

public class EternalSoulboundHandler {
    public record SavedSlot(int section, int index, ItemStack stack) {}
    private static final Map<UUID, List<SavedSlot>> DEATH_VAULT = new HashMap<>();

    public static boolean isSoulbound(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof DivineRelic;
    }

    public static void saveBeforeDeath(PlayerEntity player) {
        if (player == null || player.getWorld().isClient) return;
        PlayerInventory inv = player.getInventory();
        List<SavedSlot> saved = new ArrayList<>();

        for (int i = 0; i < inv.main.size(); i++) {
            if (isSoulbound(inv.main.get(i))) {
                saved.add(new SavedSlot(0, i, inv.main.get(i).copy()));
                inv.main.set(i, ItemStack.EMPTY);
            }
        }
        for (int i = 0; i < inv.armor.size(); i++) {
            if (isSoulbound(inv.armor.get(i))) {
                saved.add(new SavedSlot(1, i, inv.armor.get(i).copy()));
                inv.armor.set(i, ItemStack.EMPTY);
            }
        }
        for (int i = 0; i < inv.offHand.size(); i++) {
            if (isSoulbound(inv.offHand.get(i))) {
                saved.add(new SavedSlot(2, i, inv.offHand.get(i).copy()));
                inv.offHand.set(i, ItemStack.EMPTY);
            }
        }
        if (!saved.isEmpty()) DEATH_VAULT.put(player.getUuid(), saved);
    }

    public static void restoreAfterRespawn(ServerPlayerEntity newPlayer) {
        List<SavedSlot> saved = DEATH_VAULT.remove(newPlayer.getUuid());
        if (saved == null || saved.isEmpty()) return;

        PlayerInventory inv = newPlayer.getInventory();
        for (SavedSlot slot : saved) {
            if (slot.section() == 1 && inv.armor.get(slot.index()).isEmpty()) inv.armor.set(slot.index(), slot.stack());
            else if (slot.section() == 2 && inv.offHand.get(slot.index()).isEmpty()) inv.offHand.set(slot.index(), slot.stack());
            else if (slot.section() == 0 && inv.main.get(slot.index()).isEmpty()) inv.main.set(slot.index(), slot.stack());
            else inv.offerOrDrop(slot.stack());
        }
        newPlayer.sendMessage(Text.literal("⚡ عاد إرث الأولمب الخالد إلى روحك بعد الإحياء!").formatted(Formatting.GOLD, Formatting.BOLD), false);
        newPlayer.getServerWorld().spawnParticles(ParticleTypes.END_ROD, newPlayer.getX(), newPlayer.getY() + 1.0, newPlayer.getZ(), 20, 0.5, 0.7, 0.5, 0.05);
    }

    public static void returnDroppedRelicToOwner(ItemEntity itemEntity, boolean fromVoid) {
        if (itemEntity.getWorld().isClient) return;
        ItemStack stack = itemEntity.getStack();
        if (!isSoulbound(stack)) return;

        MinecraftServer server = itemEntity.getServer();
        if (server == null) return;

        UUID ownerId = DivineRelic.getOwnerUuid(stack);
        ServerPlayerEntity owner = ownerId != null ? server.getPlayerManager().getPlayer(ownerId) : null;
        if (owner == null && !server.getPlayerManager().getPlayerList().isEmpty()) {
            owner = server.getPlayerManager().getPlayerList().get(0);
        }

        if (owner != null && owner.isAlive()) {
            ItemStack copy = stack.copy();
            if (!owner.getInventory().insertStack(copy)) {
                owner.getEnderChestInventory().addStack(copy);
                owner.sendMessage(Text.literal("✦ حقيبتك ممتلئة — حُفظ الإرث الأولمبي بأمان في صندوق الـ Ender Chest!").formatted(Formatting.LIGHT_PURPLE), false);
            } else {
                String msg = fromVoid
                    ? "⚡ إرث الأولمب لا يُدفن في العدم — عادت القطعة إلى يدك من الفراغ!"
                    : "⚡ عادت القطعة الأولمبية المرتبطة بروحك إليك تلقائياً!";
                owner.sendMessage(Text.literal(msg).formatted(Formatting.GOLD), true);
            }
            ServerWorld sw = owner.getServerWorld();
            sw.spawnParticles(ParticleTypes.PORTAL, owner.getX(), owner.getY() + 1.0, owner.getZ(), 20, 0.4, 0.6, 0.4, 0.1);
            sw.playSound(null, owner.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.8F, 1.4F);
            itemEntity.discard();
        } else if (fromVoid) {
            itemEntity.setNoGravity(true);
            itemEntity.setVelocity(0, 0.25, 0);
            itemEntity.refreshPositionAfterTeleport(itemEntity.getX(), itemEntity.getWorld().getBottomY() + 70, itemEntity.getZ());
        }
    }
}
