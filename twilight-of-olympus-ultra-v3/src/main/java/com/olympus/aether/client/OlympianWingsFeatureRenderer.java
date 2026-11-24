package com.olympus.aether.client;

import com.olympus.aether.OlympusMod;
import com.olympus.aether.registry.ModItems;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class OlympianWingsFeatureRenderer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    private static final Identifier WINGS_TEXTURE = Identifier.of(OlympusMod.MOD_ID, "textures/entity/olympian_wings.png");

    public OlympianWingsFeatureRenderer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
        super(context);
    }

    @Override
    public void render(
        MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
        AbstractClientPlayerEntity player, float limbAngle, float limbDistance,
        float tickDelta, float animationProgress, float headYaw, float headPitch
    ) {
        if (!player.getEquippedStack(EquipmentSlot.CHEST).isOf(ModItems.AEGIS_CHESTPLATE_OF_ZEUS)) return;

        matrices.push();
        this.getContextModel().body.rotate(matrices);
        matrices.translate(0.0F, 0.14F, 0.18F);

        boolean flying = player.isFallFlying() || !player.isOnGround();
        float flapSpeed = flying ? 0.32F : 0.08F;
        float flapAmp = flying ? 26.0F : 7.5F;
        float baseSpread = flying ? 18.0F : 30.0F;
        float flapAngle = baseSpread + MathHelper.sin(animationProgress * flapSpeed) * flapAmp;

        int fullBright = LightmapTextureManager.MAX_LIGHT_COORDINATE;
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(WINGS_TEXTURE));

        matrices.push();
        matrices.translate(0.10F, 0.0F, 0.0F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-flapAngle));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(flying ? -14.0F : -6.0F));
        drawWingQuad(matrices, consumer, fullBright, 1.55F, 1.55F, false);
        matrices.pop();

        matrices.push();
        matrices.translate(-0.10F, 0.0F, 0.0F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(flapAngle));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(flying ? 14.0F : 6.0F));
        drawWingQuad(matrices, consumer, fullBright, 1.55F, 1.55F, true);
        matrices.pop();

        matrices.pop();
    }

    private static void drawWingQuad(MatrixStack matrices, VertexConsumer vc, int light, float width, float height, boolean mirror) {
        MatrixStack.Entry entry = matrices.peek();
        float x1 = mirror ? -width : width;
        float y0 = -height * 0.42F;
        float y1 = height * 0.58F;
        float zTip = 0.24F;

        vc.vertex(entry, 0.0F, y0, 0.0F).color(255, 255, 255, 255).texture(0.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, 1.0F);
        vc.vertex(entry, x1,   y0, zTip).color(255, 255, 255, 255).texture(1.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, 1.0F);
        vc.vertex(entry, x1,   y1, zTip).color(255, 255, 255, 255).texture(1.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, 1.0F);
        vc.vertex(entry, 0.0F, y1, 0.0F).color(255, 255, 255, 255).texture(0.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, 1.0F);
    }
}
