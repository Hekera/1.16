package cofh.thermal.innovation.client.renderer.tool;

import cofh.lib.util.helpers.MathHelper;
import cofh.thermal.innovation.item.RFLaserItem;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.*;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

public class LaserRenderer {

    public static void renderLaser(Vector3f startPoint, Vector3f endPoint, float laserWidth, float r, float g, float b, float alpha, IVertexBuilder builder) {
        float halfWidth = laserWidth * 0.5F;

        Vector3f[] points = new Vector3f[]{vector3fAdd(startPoint, -halfWidth, halfWidth, 0),
                vector3fAdd(endPoint, -halfWidth, halfWidth, 0)};

        //Sides
        for (byte i = 0; i < 16; i++) {
            int bit1 = i >> 1 & 1;
            int bit2 = (i + 1) >> 1 & 1;
            builder.addVertex(points[bit2].getX() + laserWidth * ((i + (i >> 3 & 1) * 2 - 2) >> 2 & 1),
                    points[bit2].getY() - laserWidth * ((i + (i >> 3 & 1) * 2) >> 2 & 1),
                    points[bit2].getZ(), r, g, b, alpha, bit1, bit2, OverlayTexture.NO_OVERLAY, 15728880, 0, 1, 0);
        }

        //Ends
        for (byte i = 0; i < 8; i++) {
            int bit1 = i >> 1 & 1;
            int bit2 = (i + 1) >> 1 & 1;
            int bit3 = i >> 2 & 1;
            builder.addVertex(points[bit3].getX() + laserWidth * bit2,
                    points[bit3].getY() - laserWidth * ((i + 6) >> 2 & 1),
                    points[bit3].getZ(), r, g, b, alpha, bit1, bit2, OverlayTexture.NO_OVERLAY, 15728880, 0, 1, 0);
        }
    }

    public static void renderLaserHelper(RenderWorldLastEvent event, ClientPlayerEntity player, PlayerEntity otherPlayer, Hand hand) {
        Minecraft instance = Minecraft.getInstance();
        float ticks = instance.getRenderPartialTicks();
        boolean isFirstPerson = instance.gameSettings.getPointOfView().func_243192_a();
        ItemStack stack = otherPlayer.getActiveItemStack();
        RFLaserItem item = (RFLaserItem) stack.getItem();
        float range = item.getRange(stack);
        int color = item.getLaserColor(stack);
        float r = (color >> 16 & 255) * 0.0039215F;
        float g = (color >> 8 & 255) * 0.0039215F;
        float b = (color & 255) * 0.0039215F;
        Vector3d cameraPosOffset = otherPlayer.getEyePosition(ticks).subtract(instance.gameRenderer.getActiveRenderInfo().getProjectedView());

        MatrixStack matrix = event.getMatrixStack();
        matrix.push();
        matrix.translate(cameraPosOffset.getX(), isFirstPerson ? 0 : cameraPosOffset.getY(), cameraPosOffset.getZ());
        matrix.rotate(Vector3f.YP.rotationDegrees(MathHelper.interpolate(-otherPlayer.rotationYaw, -otherPlayer.prevRotationYaw, ticks)));
        matrix.rotate(Vector3f.XP.rotationDegrees(MathHelper.interpolate(otherPlayer.rotationPitch, otherPlayer.rotationPitch, ticks)));

        Vector4f startPoint4f;
        Vector4f endPoint4f = new Vector4f(0, 0, range, 1.0F);
        if (otherPlayer.equals(player) && isFirstPerson) {
            if (instance.gameSettings.viewBobbing)
                adjustBobbing(player, matrix, ticks, 0.5F, 0.5F);
            HandSide handSide = instance.gameSettings.mainHand;
            handSide = hand == Hand.MAIN_HAND ? handSide : handSide.opposite();
            Vector3d offset = new Vector3d(0.30F * (handSide == HandSide.LEFT ? 1 : -1), -0.055F, 0.6F);
            Vector3d movement = new Vector3d((MathHelper.interpolate(player.prevRotationYaw, player.rotationYaw, ticks) - MathHelper.interpolate(player.prevRenderArmYaw, player.renderArmYaw, ticks)) * 0.0012,
                    (MathHelper.interpolate(player.prevRotationPitch, player.rotationPitch, ticks) - MathHelper.interpolate(player.prevRenderArmPitch, player.renderArmPitch, ticks)) * 0.0008,
                    (1 - player.getFovModifier()) * 0.5F);

            startPoint4f = new Vector4f((float) (offset.getX() + movement.getX()), (float) (offset.getY() + movement.getY()), (float) (offset.getZ() + movement.getZ()), 1.0F);
        }
        else {
            if (instance.gameSettings.viewBobbing)
                adjustBobbing(player, matrix, ticks, -1.0F, 1.218F);
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

        LaserRenderer.renderLaser(startPoint, endPoint, 0.1F, r, g, b, 0.8F, builder);
        LaserRenderer.renderLaser(startPoint, endPoint, 0.04F, 1.0F, 1.0F, 1.0F, 0.9F, builder);

        matrix.pop();
        buffer.finish();
    }

    static void adjustBobbing(PlayerEntity player, MatrixStack matrixStackIn, float partialTicks, float xAdjust, float yAdjust) {
        float f1 = -MathHelper.interpolate(player.prevDistanceWalkedModified, player.distanceWalkedModified, 1.0F + partialTicks);
        float f2 = MathHelper.interpolate(player.prevCameraYaw, player.cameraYaw, partialTicks);
        matrixStackIn.translate(MathHelper.sin(f1 * Math.PI) * f2 * 0.5 * xAdjust, Math.abs(MathHelper.cos(f1 * Math.PI) * f2) * yAdjust, 0.0D);
        matrixStackIn.rotate(Vector3f.ZP.rotationDegrees((float) MathHelper.sin(f1 * Math.PI) * f2 * 3.0F));
        matrixStackIn.rotate(Vector3f.XP.rotationDegrees((float) Math.abs(MathHelper.cos(f1 * Math.PI - 0.2) * f2) * 5.0F));
    }

    public static Vector3f vector3fAdd(Vector3f vector, float x, float y, float z) {
        return new Vector3f(vector.getX() + x, vector.getY() + y, vector.getZ() + z);
    }

    public static class LaserRenderType extends RenderType {

        public static final RenderType LASER = RenderType.makeType("laser",
                DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_QUADS, 256,
                RenderType.State.getBuilder().texture(NO_TEXTURE)
                        .layer(field_239235_M_)
                        .transparency(TRANSLUCENT_TRANSPARENCY)
                        .depthTest(DEPTH_ALWAYS)
                        .cull(CULL_ENABLED)
                        .lightmap(LIGHTMAP_DISABLED)
                        .writeMask(COLOR_WRITE)
                        .build(false));


        public LaserRenderType(String nameIn, VertexFormat formatIn, int drawModeIn, int bufferSizeIn, boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {

            super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
        }
    }

}
