package cofh.thermal.core.entity.projectile;

import cofh.lib.entity.AbstractGrenadeEntity;
import cofh.lib.util.AreaUtils;
import cofh.lib.util.Utils;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.state.Property;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cofh.thermal.core.init.TCoreReferences.*;
import static net.minecraft.potion.Effects.GLOWING;

public class BaseGrenadeEntity extends AbstractGrenadeEntity {

    public static int effectDuration = 15; // In seconds
    public String effect;
    public String transformer;

    public BaseGrenadeEntity(EntityType<? extends ProjectileItemEntity> type, World worldIn) {

        super(type, worldIn);
    }

    public BaseGrenadeEntity(World worldIn, double x, double y, double z, String effect, String transformer) {

        super(GRENADE_ENTITY, x, y, z, worldIn);
        this.effect = effect;
        this.transformer = transformer;
    }

    public BaseGrenadeEntity(World worldIn, LivingEntity livingEntityIn, String effect, String transformer) {

        super(GRENADE_ENTITY, livingEntityIn, worldIn);
        this.effect = effect;
        this.transformer = transformer;
    }

    @Override
    protected Item getDefaultItem() {

        return Items.AIR;
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        //effect
        //if special air
//        AirCleanerEntity cleaner = new AirCleanerEntity(this.world, effectDuration * 20, "glow");
//        cleaner.copyLocationAndAnglesFrom(this);
//        world.addEntity(cleaner);
//        System.out.println("Spawned cleaner!");
//        this.remove();
    }

    @Override
    public void readAdditional(CompoundNBT nbt) {

        super.readAdditional(nbt);
        this.effect = nbt.getString("effect");
    }

    @Override
    public void writeAdditional(CompoundNBT nbt) {

        super.writeAdditional(nbt);
        nbt.putString("effect", this.effect);
    }
}
