package cofh.thermal.core.util;

import cofh.lib.util.AreaUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;

import java.awt.geom.Area;
import java.util.function.Consumer;
import cofh.thermal.core.init.TCoreReferences;
import cofh.lib.util.references.CoreReferences;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static cofh.lib.util.AreaUtils.HORZ_MAX;

public enum Element {
    FIRE(0, "fire", AreaUtils::igniteLiving, AreaUtils::igniteBlock, 0xDCA22B, 0.05F),
    ICE(1, "ice", AreaUtils::chillLiving, AreaUtils::freezeBlock, 0x1DB6E4, 0.09F),
    EARTH(2, "earth", AreaUtils::sunderLiving, AreaUtils::breakEarthBlock, 0x3B2E28, 0.03F),
    LIGHTNING(3, "lightning", AreaUtils::shockLiving, AreaUtils::zapBlock, 0xF5F258, 0.08F);

    private final int index;
    private final String id;
    private final IElementEffectApplier effectApplier;
    private final IElementBlockTransformer blockTransformer;
    private final int laserColor;
    private final float laserProgressModifier;
    //TODO: sounds

    private Element(int index, String id, IElementEffectApplier effectApplier, IElementBlockTransformer blockTransformer, int laserColor, float laserProgressModifier) {
        this.index = index;
        this.id = id;
        this.effectApplier = effectApplier;
        this.blockTransformer = blockTransformer;
        this.laserProgressModifier = laserProgressModifier;
        this.laserColor = laserColor;
    }

    public int getIndex() {
        return this.index;
    }

    public String getId() {
        return this.id;
    }

    public IElementEffectApplier getEffectApplier() {
        return this.effectApplier;
    }

    public IElementBlockTransformer getBlockTransformer() {
        return this.blockTransformer;
    }

    public int getLaserColor() {
        return this.laserColor;
    }

    public float getLaserProgressModifier() {
        return this.laserProgressModifier;
    }

    public String toString() {
        return this.getId();
    }

    public interface IElementEffectApplier {
        void applyEffect(LivingEntity target, int duration, int power);
    }

    public interface IElementBlockTransformer {
        boolean transformBlock(Entity user, World world, BlockPos pos, Direction face);

        default void transformSphereArea(Entity user, World world, BlockPos pos, float radius, float chance) {

            float f = Math.min(AreaUtils.HORZ_MAX, radius);
            float v = Math.min(AreaUtils.VERT_MAX, radius);
            float f2 = f * f;

            for (BlockPos iterPos : BlockPos.getAllInBoxMutable(pos.add(-f, -v, -f), pos.add(f, v, f))) {
                double distance = iterPos.distanceSq(pos);
                if (distance < f2 && world.rand.nextDouble() < chance) {
                    transformBlock(user, world, iterPos, Direction.DOWN);
                }
            }
        }

        default void transformCylinderArea(Entity user, World world, BlockPos pos, float radius, float height, float chance) {

            float f = Math.min(AreaUtils.HORZ_MAX, radius);
            float v = Math.min(AreaUtils.VERT_MAX, height);
            float f2 = f * f;

            for (BlockPos iterPos : BlockPos.getAllInBoxMutable(pos.add(-f, -v, -f), pos.add(f, v, f))) {
                double distance = Math.pow(iterPos.getX() - pos.getZ(), 2) + Math.pow(iterPos.getZ() - pos.getZ(), 2);
                if (distance < f2 && world.rand.nextDouble() < chance) {
                    transformBlock(user, world, iterPos, Direction.DOWN);
                }
            }
        }
    }
}
