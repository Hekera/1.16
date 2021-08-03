package cofh.thermal.innovation.event;

import cofh.thermal.core.client.renderer.item.LaserRenderType;
import cofh.thermal.core.client.renderer.item.LaserRenderer;
import cofh.thermal.innovation.item.RFLaserItem;
import cofh.lib.util.helpers.MathHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

import static cofh.lib.util.constants.Constants.ID_COFH_CORE;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ID_COFH_CORE)
public class InoClientEvents {
    private InoClientEvents() {

    }

    @SubscribeEvent
    static void renderWorldLastEvent(RenderWorldLastEvent event) {
        List<AbstractClientPlayerEntity> otherPlayers = Minecraft.getInstance().world.getPlayers();
        ClientPlayerEntity player = Minecraft.getInstance().player;

        for (PlayerEntity otherPlayer : otherPlayers) {
            if (player.getDistanceSq(otherPlayer) > 1024)
                continue;

            Hand hand = otherPlayer.getActiveHand();
            if (otherPlayer.isHandActive() && otherPlayer.getActiveItemStack().getItem() instanceof RFLaserItem) {
                InoClientEvents.renderLaserHelper(event, player, otherPlayer, hand);
            }
        }
    }

    static void renderLaserHelper(RenderWorldLastEvent event, ClientPlayerEntity player, PlayerEntity otherPlayer, Hand hand) {
        Minecraft instance = Minecraft.getInstance();
        float ticks = instance.getRenderPartialTicks();
        ItemStack stack = otherPlayer.getActiveItemStack();
        float range = ((RFLaserItem) stack.getItem()).getRange(stack);
        Vector3d cameraPosOffset = otherPlayer.getEyePosition(ticks).subtract(instance.gameRenderer.getActiveRenderInfo().getProjectedView());

        MatrixStack matrix = event.getMatrixStack();
        matrix.push();
        matrix.translate(cameraPosOffset.getX(), cameraPosOffset.length() > 0.5F ? cameraPosOffset.getY() : 0, cameraPosOffset.getZ());
        matrix.rotate(Vector3f.YP.rotationDegrees(MathHelper.interpolate(-otherPlayer.rotationYaw, -otherPlayer.prevRotationYaw, ticks)));
        matrix.rotate(Vector3f.XP.rotationDegrees(MathHelper.interpolate(otherPlayer.rotationPitch, otherPlayer.rotationPitch, ticks)));

        Vector4f startPoint4f;
        Vector4f endPoint4f = new Vector4f(0, 0, range, 1.0F);
        if (otherPlayer.equals(player) && cameraPosOffset.length() < 0.5F) {
            adjustBobbing(player, matrix, ticks, 0.9F);
            HandSide handSide = instance.gameSettings.mainHand;
            handSide = hand == Hand.MAIN_HAND ? handSide : handSide.opposite();
            Vector3d offset = new Vector3d(0.05F * (handSide == HandSide.LEFT ? 1 : -1), -0.01F, 0.1F);
            Vector3d movement = new Vector3d((MathHelper.interpolate(player.prevRotationYaw, player.rotationYaw, ticks) - MathHelper.interpolate(player.prevRenderArmYaw, player.renderArmYaw, ticks)) * 0.00023,
                    (MathHelper.interpolate(player.prevRotationPitch, player.rotationPitch, ticks) - MathHelper.interpolate(player.prevRenderArmPitch, player.renderArmPitch, ticks)) * 0.00016,
                    (1 - player.getFovModifier()) * 0.1F);

            startPoint4f = new Vector4f((float) (offset.getX() + movement.getX()), (float) (offset.getY() + movement.getY()), (float) (offset.getZ() + movement.getZ()), 1.0F);
        }
        else {
            adjustBobbing(player, matrix, ticks, 1.24F);
            Vector3d offset = new Vector3d(0.25F * (hand == Hand.MAIN_HAND ? -1 : 1), 0, 0.30F);
            startPoint4f = new Vector4f((float) offset.getX(), (float) offset.getY(), (float) offset.getZ(), 1.0F);
        }

        Matrix4f positionMatrix = matrix.getLast().getMatrix();
        startPoint4f.transform(positionMatrix);
        endPoint4f.transform(positionMatrix);
        Vector3f startPoint = new Vector3f(startPoint4f.getX(), startPoint4f.getY(), startPoint4f.getZ());
        Vector3f endPoint = new Vector3f(endPoint4f.getX(), endPoint4f.getY(), endPoint4f.getZ());

        IRenderTypeBuffer.Impl buffer = instance.getRenderTypeBuffers().getBufferSource();
        IVertexBuilder builder = buffer.getBuffer(LaserRenderType.LASER);

        LaserRenderer.renderLaser(startPoint, endPoint, 0.02F, 1.0F, 0.1F, 0.1F, 0.6F, builder);
        LaserRenderer.renderLaser(startPoint, endPoint, 0.008F, 1.0F, 1.0F, 1.0F, 0.8F, builder);

        matrix.pop();
        buffer.finish();
    }

    static void adjustBobbing(PlayerEntity player, MatrixStack matrixStackIn, float partialTicks, float adjustment) {
        float f1 = -MathHelper.interpolate(player.prevDistanceWalkedModified, player.distanceWalkedModified, 1.0F + partialTicks);
        float f2 = MathHelper.interpolate(player.prevCameraYaw, player.cameraYaw, partialTicks);
        matrixStackIn.translate(MathHelper.sin(f1 * Math.PI) * f2 * 0.5 * adjustment, Math.abs(MathHelper.cos(f1 * Math.PI) * f2) * adjustment, 0.0D);
        matrixStackIn.rotate(Vector3f.ZP.rotationDegrees((float) MathHelper.sin(f1 * Math.PI) * f2 * 3.0F));
        matrixStackIn.rotate(Vector3f.XP.rotationDegrees((float) Math.abs(MathHelper.cos(f1 * Math.PI - 0.2) * f2) * 5.0F));
    }
}
