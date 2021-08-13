package cofh.thermal.core.entity.projectile;

import cofh.lib.entity.AbstractGrenadeEntity;
import cofh.lib.util.AreaUtils;
import cofh.lib.util.Utils;
import net.minecraft.entity.*;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

import static cofh.thermal.core.init.TCoreReferences.*;
import static net.minecraft.potion.Effects.GLOWING;

public class BaseGrenadeEntity extends AbstractGrenadeEntity {

    public static int effectDuration = 15; // In seconds

    public BaseGrenadeEntity(EntityType<? extends ProjectileItemEntity> type, World worldIn) {

        super(type, worldIn);
    }

    public BaseGrenadeEntity(World worldIn, double x, double y, double z, @Nullable LivingEntity livingEntityIn) {

        super(GRENADE_ENTITY, x, y, z, worldIn);
        if (livingEntityIn != null) {
            this.setShooter(livingEntityIn);
        }
    }

    @Override
    protected Item getDefaultItem() {

        return null;
    }

    @Override
    protected void onImpact(RayTraceResult result) {

        return;
    }
}
