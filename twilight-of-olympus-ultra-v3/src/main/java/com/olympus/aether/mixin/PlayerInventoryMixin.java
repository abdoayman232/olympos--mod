package com.olympus.aether.mixin;

import com.olympus.aether.system.EternalSoulboundHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {
    @Shadow @Final public PlayerEntity player;

    @Inject(method = "dropAll", at = @At("HEAD"))
    private void olympus$keepDivineRelicsOnDeath(CallbackInfo ci) {
        EternalSoulboundHandler.saveBeforeDeath(this.player);
    }
}
