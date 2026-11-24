package com.olympus.aether.mixin;

import com.olympus.aether.system.EternalSoulboundHandler;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Shadow public abstract ItemStack getStack();
    @Shadow private int itemAge;

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void olympus$immuneToAllDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (EternalSoulboundHandler.isSoulbound(this.getStack())) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void olympus$returnFromVoidOrDrop(CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (self.getWorld().isClient || !EternalSoulboundHandler.isSoulbound(this.getStack())) return;
        this.itemAge = 0;
        boolean inVoid = self.getY() < self.getWorld().getBottomY() + 1;
        if (inVoid || self.isInLava() || self.isOnFire() || self.age >= 60) {
            EternalSoulboundHandler.returnDroppedRelicToOwner(self, inVoid);
        }
    }
}
