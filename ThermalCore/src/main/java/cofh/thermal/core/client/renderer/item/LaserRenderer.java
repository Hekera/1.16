package cofh.thermal.core.client.renderer.item;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.math.vector.*;

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

    public static Vector3f vector3fAdd(Vector3f vector, float x, float y, float z) {
        return new Vector3f(vector.getX() + x, vector.getY() + y, vector.getZ() + z);
    }

}
