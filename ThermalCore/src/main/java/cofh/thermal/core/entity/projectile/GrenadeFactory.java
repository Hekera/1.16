package cofh.thermal.core.entity.projectile;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class GrenadeFactory {
    public String effect;
    public String transformer;

    public GrenadeFactory(String effect, String transformer) {

        this.effect = effect;
        this.transformer = transformer;
    }

    BaseGrenadeEntity createGrenade(World world, LivingEntity living) {

        return new BaseGrenadeEntity(world, living, this.effect, this.transformer);
    }

    BaseGrenadeEntity createGrenade(World world, double posX, double posY, double posZ) {

        return new BaseGrenadeEntity(world, posX, posY, posZ, this.effect, this.transformer);
    }

    BaseGrenadeEntity createGrenade(World world, double posX, double posY, double posZ, Vector3d motion) {
        BaseGrenadeEntity grenade = createGrenade(world, posX, posY, posZ);
        grenade.setMotion(motion);
        return grenade;
    }
}
