package com.olympus.aether.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.UUID;

public interface DivineRelic {
    int getOdysseyStage();
    String getOlympianGod();

    static void bindOwner(ItemStack stack, PlayerEntity player) {
        if (stack.isEmpty() || player == null) return;
        NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        if (!nbt.containsUuid("olympus_owner")) {
            nbt.putUuid("olympus_owner", player.getUuid());
            nbt.putString("olympus_owner_name", player.getName().getString());
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        }
    }

    static UUID getOwnerUuid(ItemStack stack) {
        if (stack.isEmpty()) return null;
        NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        return nbt.containsUuid("olympus_owner") ? nbt.getUuid("olympus_owner") : null;
    }
}
