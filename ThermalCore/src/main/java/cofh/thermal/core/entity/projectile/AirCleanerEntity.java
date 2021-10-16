package cofh.thermal.core.entity.projectile;

import cofh.core.tileentity.SpecialAirTile;
import cofh.lib.util.AreaUtils;
import cofh.lib.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static cofh.thermal.core.init.TCoreReferences.AIR_CLEANER_ENTITY;

public class AirCleanerEntity extends Entity {

    protected int duration;
    protected int radius;
    protected SpecialAirTile.SpecialAirConfig config;

    public AirCleanerEntity(EntityType<? extends Entity> type, World worldIn) {

        super(type, worldIn);
    }

    public AirCleanerEntity(World worldIn, int duration, int radius, SpecialAirTile.SpecialAirConfig config) {

        super(AIR_CLEANER_ENTITY, worldIn);
        this.duration = duration;
        this.radius = radius;
        this.config = config;
    }

    public AirCleanerEntity(World worldIn, int duration, int radius, String config) {

        this(worldIn, duration, radius, AreaUtils.airConfigs.get(config));
    }

    @Override
    protected void registerData() {

    }

    public void tick() {

        super.tick();
        if (Utils.isServerWorld(this.world) && this.ticksExisted >= duration) {
            float r = 5;
            float r2 = r * r;
            for (BlockPos iterPos : BlockPos.getAllInBoxMutable(this.getPosition().add(-r, -r, -r), this.getPosition().add(r, r, r))) {
                double distance = iterPos.distanceSq(this.getPositionVec(), true);
                if (distance < r2) {
                    TileEntity tile = this.world.getTileEntity(iterPos);
                    if (tile instanceof SpecialAirTile) {
                        ((SpecialAirTile) tile).removeConfig(this.config);
                    }
                }
            }
            this.remove();
        }
    }

    @Override
    protected void readAdditional(CompoundNBT nbt) {

        this.duration = nbt.getInt("duration");
        this.radius = nbt.getInt("radius");
        this.config = AreaUtils.airConfigs.get(nbt.getString("config"));
    }

    @Override
    protected void writeAdditional(CompoundNBT nbt) {

        nbt.putInt("duration", this.duration);
        nbt.putInt("radius", this.radius);
        nbt.putString("config", this.config.name);
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return new SSpawnObjectPacket(this);
    }
}
