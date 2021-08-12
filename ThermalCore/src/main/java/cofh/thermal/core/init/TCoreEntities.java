package cofh.thermal.core.init;

import cofh.lib.entity.AbstractGrenadeEntity;
import cofh.lib.util.AreaUtils;
import cofh.lib.util.Utils;
import cofh.thermal.core.entity.item.*;
import cofh.thermal.core.entity.monster.BasalzEntity;
import cofh.thermal.core.entity.monster.BlitzEntity;
import cofh.thermal.core.entity.monster.BlizzEntity;
import cofh.thermal.core.entity.projectile.*;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.gen.Heightmap;

import static cofh.thermal.core.ThermalCore.ENTITIES;
import static cofh.thermal.core.init.TCoreIDs.*;
import static cofh.thermal.core.init.TCoreReferences.*;

public class TCoreEntities {

    private TCoreEntities() {

    }

    public static void register() {

        ENTITIES.register(ID_BASALZ, () -> EntityType.Builder.create(BasalzEntity::new, EntityClassification.MONSTER).size(0.6F, 1.8F).immuneToFire().build(ID_BASALZ));
        ENTITIES.register(ID_BLIZZ, () -> EntityType.Builder.create(BlizzEntity::new, EntityClassification.MONSTER).size(0.6F, 1.8F).build(ID_BLIZZ));
        ENTITIES.register(ID_BLITZ, () -> EntityType.Builder.create(BlitzEntity::new, EntityClassification.MONSTER).size(0.6F, 1.8F).build(ID_BLITZ));

        ENTITIES.register(ID_BASALZ_PROJECTILE, () -> EntityType.Builder.<BasalzProjectileEntity>create(BasalzProjectileEntity::new, EntityClassification.MISC).size(0.3125F, 0.3125F).build(ID_BASALZ_PROJECTILE));
        ENTITIES.register(ID_BLIZZ_PROJECTILE, () -> EntityType.Builder.<BlizzProjectileEntity>create(BlizzProjectileEntity::new, EntityClassification.MISC).size(0.3125F, 0.3125F).build(ID_BLIZZ_PROJECTILE));
        ENTITIES.register(ID_BLITZ_PROJECTILE, () -> EntityType.Builder.<BlitzProjectileEntity>create(BlitzProjectileEntity::new, EntityClassification.MISC).size(0.3125F, 0.3125F).build(ID_BLITZ_PROJECTILE));

        ENTITIES.register(ID_EXPLOSIVE_GRENADE, () -> EntityType.Builder.<ExplosiveGrenadeEntity>create(ExplosiveGrenadeEntity::new, EntityClassification.MISC).size(0.25F, 0.25F).build(ID_EXPLOSIVE_GRENADE));

        ENTITIES.register(ID_SLIME_GRENADE, () -> EntityType.Builder.<SlimeGrenadeEntity>create(SlimeGrenadeEntity::new, EntityClassification.MISC).size(0.25F, 0.25F).build(ID_SLIME_GRENADE));
        ENTITIES.register(ID_REDSTONE_GRENADE, () -> EntityType.Builder.<RedstoneGrenadeEntity>create(RedstoneGrenadeEntity::new, EntityClassification.MISC).size(0.25F, 0.25F).build(ID_REDSTONE_GRENADE));
        ENTITIES.register(ID_GLOWSTONE_GRENADE, () -> EntityType.Builder.<GlowstoneGrenadeEntity>create(GlowstoneGrenadeEntity::new, EntityClassification.MISC).size(0.25F, 0.25F).build(ID_GLOWSTONE_GRENADE));
        ENTITIES.register(ID_ENDER_GRENADE, () -> EntityType.Builder.<EnderGrenadeEntity>create(EnderGrenadeEntity::new, EntityClassification.MISC).size(0.25F, 0.25F).build(ID_ENDER_GRENADE));

        ENTITIES.register(ID_PHYTO_GRENADE, () -> EntityType.Builder.<PhytoGrenadeEntity>create(PhytoGrenadeEntity::new, EntityClassification.MISC).size(0.25F, 0.25F).build(ID_PHYTO_GRENADE));

        ENTITIES.register(ID_FIRE_GRENADE, () -> EntityType.Builder.<FireGrenadeEntity>create(FireGrenadeEntity::new, EntityClassification.MISC).size(0.25F, 0.25F).build(ID_FIRE_GRENADE));
        ENTITIES.register(ID_EARTH_GRENADE, () -> EntityType.Builder.<EarthGrenadeEntity>create(EarthGrenadeEntity::new, EntityClassification.MISC).size(0.25F, 0.25F).build(ID_EARTH_GRENADE));
        ENTITIES.register(ID_ICE_GRENADE, () -> EntityType.Builder.<IceGrenadeEntity>create(IceGrenadeEntity::new, EntityClassification.MISC).size(0.25F, 0.25F).build(ID_ICE_GRENADE));
        ENTITIES.register(ID_LIGHTNING_GRENADE, () -> EntityType.Builder.<LightningGrenadeEntity>create(LightningGrenadeEntity::new, EntityClassification.MISC).size(0.25F, 0.25F).build(ID_LIGHTNING_GRENADE));

//        AbstractGrenadeEntity updraftGrenade = new AbstractGrenadeEntity(, null) {
//            public int effectDuration = 15;
//
//            @Override
//            protected Item getDefaultItem() {
//
//                return FIRE_GRENADE_ITEM;
//            }
//
//            @Override
//            protected void onImpact(RayTraceResult result) {
//
//                if (Utils.isServerWorld(world)) {
//                    if (!this.isInWater()) {
//                        AreaUtils.igniteNearbyEntities(this, world, this.getPosition(), radius, effectDuration);
//                        AreaUtils.igniteSpecial(this, world, this.getPosition(), radius, true, true, func_234616_v_());
//                        AreaUtils.igniteNearbyGround(this, world, this.getPosition(), radius, 0.2);
//                        makeAreaOfEffectCloud();
//                    }
//                    this.world.setEntityState(this, (byte) 3);
//                    this.remove();
//                }
//                if (result.getType() == RayTraceResult.Type.ENTITY && this.ticksExisted < 10) {
//                    return;
//                }
//                this.world.addParticle(ParticleTypes.EXPLOSION, this.getPosX(), this.getPosY(), this.getPosZ(), 1.0D, 0.0D, 0.0D);
//                this.world.playSound(this.getPosX(), this.getPosY(), this.getPosZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 0.5F, (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F, false);
//            }
//
//            private void makeAreaOfEffectCloud() {
//
//                AreaEffectCloudEntity cloud = new AreaEffectCloudEntity(world, getPosX(), getPosY(), getPosZ());
//                cloud.setRadius(1);
//                cloud.setParticleData(ParticleTypes.FLAME);
//                cloud.setDuration(CLOUD_DURATION);
//                cloud.setWaitTime(0);
//                cloud.setRadiusPerTick((radius - cloud.getRadius()) / (float) cloud.getDuration());
//
//                world.addEntity(cloud);
//            }
//        };

//        ENTITIES.register("updraft_grenade", () -> EntityType.Builder.<LightningGrenadeEntity>create(LightningGrenadeEntity::new, EntityClassification.MISC).size(0.25F, 0.25F).build(ID_LIGHTNING_GRENADE));

        ENTITIES.register(ID_NUKE_GRENADE, () -> EntityType.Builder.<NukeGrenadeEntity>create(NukeGrenadeEntity::new, EntityClassification.MISC).size(0.25F, 0.25F).build(ID_NUKE_GRENADE));

        ENTITIES.register(ID_SLIME_TNT, () -> EntityType.Builder.<SlimeTNTEntity>create(SlimeTNTEntity::new, EntityClassification.MISC).immuneToFire().size(0.98F, 0.98F).build(ID_SLIME_TNT));
        ENTITIES.register(ID_REDSTONE_TNT, () -> EntityType.Builder.<RedstoneTNTEntity>create(RedstoneTNTEntity::new, EntityClassification.MISC).immuneToFire().size(0.98F, 0.98F).build(ID_REDSTONE_TNT));
        ENTITIES.register(ID_GLOWSTONE_TNT, () -> EntityType.Builder.<GlowstoneTNTEntity>create(GlowstoneTNTEntity::new, EntityClassification.MISC).immuneToFire().size(0.98F, 0.98F).build(ID_GLOWSTONE_TNT));
        ENTITIES.register(ID_ENDER_TNT, () -> EntityType.Builder.<EnderTNTEntity>create(EnderTNTEntity::new, EntityClassification.MISC).immuneToFire().size(0.98F, 0.98F).build(ID_ENDER_TNT));

        ENTITIES.register(ID_PHYTO_TNT, () -> EntityType.Builder.<PhytoTNTEntity>create(PhytoTNTEntity::new, EntityClassification.MISC).immuneToFire().size(0.98F, 0.98F).build(ID_PHYTO_TNT));

        ENTITIES.register(ID_FIRE_TNT, () -> EntityType.Builder.<FireTNTEntity>create(FireTNTEntity::new, EntityClassification.MISC).immuneToFire().size(0.98F, 0.98F).build(ID_FIRE_TNT));
        ENTITIES.register(ID_EARTH_TNT, () -> EntityType.Builder.<EarthTNTEntity>create(EarthTNTEntity::new, EntityClassification.MISC).immuneToFire().size(0.98F, 0.98F).build(ID_EARTH_TNT));
        ENTITIES.register(ID_ICE_TNT, () -> EntityType.Builder.<IceTNTEntity>create(IceTNTEntity::new, EntityClassification.MISC).immuneToFire().size(0.98F, 0.98F).build(ID_ICE_TNT));
        ENTITIES.register(ID_LIGHTNING_TNT, () -> EntityType.Builder.<LightningTNTEntity>create(LightningTNTEntity::new, EntityClassification.MISC).immuneToFire().size(0.98F, 0.98F).build(ID_LIGHTNING_TNT));

        ENTITIES.register(ID_NUKE_TNT, () -> EntityType.Builder.<NukeTNTEntity>create(NukeTNTEntity::new, EntityClassification.MISC).immuneToFire().size(0.98F, 0.98F).build(ID_NUKE_TNT));
    }

    public static void setup() {

        EntitySpawnPlacementRegistry.register(BASALZ_ENTITY, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, BasalzEntity::canSpawn);
        EntitySpawnPlacementRegistry.register(BLITZ_ENTITY, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, BlitzEntity::canSpawn);
        EntitySpawnPlacementRegistry.register(BLIZZ_ENTITY, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, BlizzEntity::canSpawn);
    }

}
