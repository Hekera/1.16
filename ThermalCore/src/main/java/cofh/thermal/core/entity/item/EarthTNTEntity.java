package cofh.thermal.core.entity.item;

import cofh.lib.entity.AbstractTNTEntity;
import cofh.lib.util.Utils;
import cofh.thermal.core.entity.projectile.EarthGrenadeEntity;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import static cofh.thermal.core.ThermalCore.BLOCKS;
import static cofh.thermal.core.init.TCoreIDs.ID_EARTH_TNT;
import static cofh.thermal.core.init.TCoreReferences.EARTH_TNT_ENTITY;

public class EarthTNTEntity extends AbstractTNTEntity {

    public EarthTNTEntity(EntityType<? extends EarthTNTEntity> type, World worldIn) {

        super(type, worldIn);
    }

    public EarthTNTEntity(World worldIn, double x, double y, double z, @Nullable LivingEntity igniter) {

        super(EARTH_TNT_ENTITY, worldIn, x, y, z, igniter);
    }

    @Override
    public Block getBlock() {

//        return BLOCKS.get(ID_EARTH_TNT);
        return Blocks.DIRT;
    }

    @Override
    protected void explode() {

        if (Utils.isServerWorld(world)) {
            EarthGrenadeEntity.sunderNearbyEntities(this, world, this.getPosition(), radius);
            EarthGrenadeEntity.breakBlocks(this, world, this.getPosition(), radius - 1, tntPlacedBy);
            this.remove();
        }
        this.world.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.getPosX(), this.getPosY(), this.getPosZ(), 1.0D, 0.0D, 0.0D);
        this.world.playSound(this.getPosX(), this.getPosY(), this.getPosZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 2.0F, (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F, false);
    }

}
