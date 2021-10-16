package cofh.lib.util;

import cofh.core.block.nyi.SpecialAirBlock;
import cofh.core.network.packet.client.PlayerMotionPacket;
import cofh.core.tileentity.SpecialAirTile;
import cofh.core.tileentity.SpecialAirTile.SpecialAirConfig;
import cofh.lib.util.helpers.MathHelper;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.monster.EndermiteEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

import static cofh.lib.util.references.CoreReferences.*;
import static net.minecraft.block.Blocks.*;

public class AreaUtils {

    private AreaUtils() {

    }

    public static final int HORZ_MAX = 16;
    public static final int VERT_MAX = 8;
    public static final Set<BlockState> REPLACEABLE_AIR = new ObjectOpenHashSet<>(new BlockState[]{AIR.getDefaultState(), CAVE_AIR.getDefaultState()});

    // region EXPERIMENTAL

    // endregion EXPERIMENTAL



    // region ELEMENTAL

    public static final IEffectApplier igniteLiving = (target, duration, power, source) -> {
        target.removePotionEffect(CHILLED);
        if (!target.isImmuneToFire() && !target.isInWater() && target.getFireTimer() <= 0) {
            target.setFire(duration);
        }
    };

    public static final IEffectApplier chillLiving = (target, duration, power, source) -> {
        if (target.getFireTimer() > 0) {
            target.forceFireTicks(0);
        }
        target.addPotionEffect(new EffectInstance(CHILLED, duration, power));
    };

    public static final IEffectApplier sunderLiving = (target, duration, power, source) ->
        target.addPotionEffect(new EffectInstance(SUNDERED, duration, power));

    public static final IEffectApplier shockLiving = (target, duration, power, source) -> {
        if (!target.isPotionActive(LIGHTNING_RESISTANCE)) {
            target.addPotionEffect(new EffectInstance(SHOCKED, duration, power));
        }
    };

    public static final IBlockTransformer fireTransform = (entity, world, pos, face) -> {
        boolean succeeded = false;
        BlockState state = world.getBlockState(pos);

        if (AreaUtils.isUnlitCampfire(state)) {
            succeeded |= world.setBlockState(pos, state.with(BlockStateProperties.LIT, true));
        }
        if (state.isAir(world, pos)) {
            if (AbstractFireBlock.canLightBlock(world, pos, face)) {
                succeeded |= world.setBlockState(pos, AbstractFireBlock.getFireForPlacement(world, pos), 11);
            }
        }
        return succeeded;
    };

    public static final IBlockTransformer iceTransform = (entity, world, pos, face) -> {
        boolean succeeded = false;
        BlockState state = world.getBlockState(pos);
        //TODO permanent stuff based on config
        boolean permanentWater = true;
        boolean permanentLava = true;

        // CAMPFIRE/FIRE
        if (AreaUtils.isLitCampfire(state)) {
            succeeded |= world.setBlockState(pos, state.with(BlockStateProperties.LIT, false));
        }
        // SNOW
        if (world.isAirBlock(pos) && AreaUtils.isValidSnowPosition(world, pos)) {
            succeeded |= world.setBlockState(pos, SNOW.getDefaultState());
        }
        // FIRE
        if (state.getBlock() == FIRE) {
            succeeded |= world.setBlockState(pos, AIR.getDefaultState());
        }
        // WATER
        boolean isFull = state.getBlock() == WATER && state.get(FlowingFluidBlock.LEVEL) == 0;
        if (state.getMaterial() == Material.WATER && isFull && state.isValidPosition(world, pos) && world.placedBlockCollides(state, pos, ISelectionContext.dummy())) {
            succeeded |= world.setBlockState(pos, permanentWater ? ICE.getDefaultState() : FROSTED_ICE.getDefaultState());
            if (!permanentWater) {
                world.getPendingBlockTicks().scheduleTick(pos, FROSTED_ICE, net.minecraft.util.math.MathHelper.nextInt(world.rand, 60, 120));
            }
        }
        // LAVA
        isFull = state.getBlock() == LAVA && state.get(FlowingFluidBlock.LEVEL) == 0;
        if (state.getMaterial() == Material.LAVA && isFull && state.isValidPosition(world, pos) && world.placedBlockCollides(state, pos, ISelectionContext.dummy())) {
            succeeded |= world.setBlockState(pos, permanentLava ? OBSIDIAN.getDefaultState() : GLOSSED_MAGMA.getDefaultState());
            if (!permanentLava) {
                world.getPendingBlockTicks().scheduleTick(pos, GLOSSED_MAGMA, net.minecraft.util.math.MathHelper.nextInt(world.rand, 60, 120));
            }
        }
        return succeeded;
    };

    public static final IBlockTransformer iceSurfaceTransform = (entity, world, pos, face) -> {
        boolean succeeded = false;
        BlockState state = world.getBlockState(pos);
        //TODO permanent stuff based on config
        boolean permanentWater = true;
        boolean permanentLava = true;

        // SNOW
        if (world.isAirBlock(pos) && AreaUtils.isValidSnowPosition(world, pos)) {
            succeeded |= world.setBlockState(pos, SNOW.getDefaultState());
        }
        BlockPos above = pos.offset(Direction.UP);
        if (world.getBlockState(above).isAir(world, above)) {
            boolean isFull = state.getBlock() == WATER && state.get(FlowingFluidBlock.LEVEL) == 0;
            if (state.getMaterial() == Material.WATER && isFull && state.isValidPosition(world, pos) && world.placedBlockCollides(state, pos, ISelectionContext.dummy())) {
                succeeded |= world.setBlockState(pos, permanentWater ? ICE.getDefaultState() : FROSTED_ICE.getDefaultState());
                if (!permanentWater) {
                    world.getPendingBlockTicks().scheduleTick(pos, FROSTED_ICE, net.minecraft.util.math.MathHelper.nextInt(world.rand, 60, 120));
                }
            }
            // LAVA
            isFull = state.getBlock() == LAVA && state.get(FlowingFluidBlock.LEVEL) == 0;
            if (state.getMaterial() == Material.LAVA && isFull && state.isValidPosition(world, pos) && world.placedBlockCollides(state, pos, ISelectionContext.dummy())) {
                succeeded |= world.setBlockState(pos, permanentLava ? OBSIDIAN.getDefaultState() : GLOSSED_MAGMA.getDefaultState());
                if (!permanentLava) {
                    world.getPendingBlockTicks().scheduleTick(pos, GLOSSED_MAGMA, net.minecraft.util.math.MathHelper.nextInt(world.rand, 60, 120));
                }
            }
        }
        return succeeded;
    };

    public static final IBlockTransformer earthTransform = (entity, world, pos, face) -> {
        boolean succeeded = false;
        BlockState state = world.getBlockState(pos);
        Material material = state.getMaterial();
        if (material == Material.ROCK || material == Material.EARTH || state.getBlock() instanceof SnowyDirtBlock) {
            succeeded |= Utils.destroyBlock(world, pos, true, entity);
        }
        return succeeded;
    };

    public static final IBlockTransformer lightningTransform = (entity, world, pos, face) -> {
        boolean succeeded = false;
        BlockState state = world.getBlockState(pos);
        if (state.isAir(world, pos)) {
            if (isValidLightningBoltPosition(world, pos, 1.0F)) {
                succeeded |= world.setBlockState(pos, LIGHTNING_AIR.getDefaultState());
            }
        }
        return succeeded;
    };

    public static final Element[] ELEMENTS = {
            new Element("fire", igniteLiving, fireTransform),
            new Element("ice", chillLiving, iceTransform),
            new Element("earth", sunderLiving, earthTransform),
            new Element("lightning", shockLiving, lightningTransform)};

    // endregion ELEMENTAL

    // region GRENADE
    public static final HashMap<String, SpecialAirConfig> airConfigs = new HashMap<>();

    static {
        putAirConfig("glow", (byte) 15, (byte) 0, chillLiving, ParticleTypes.INSTANT_EFFECT);
        putAirConfig("signal", (byte) 7, (byte) 15, null, RedstoneParticleData.REDSTONE_DUST);
    }

    private static void putAirConfig(String name, byte light, byte power, @Nullable AreaUtils.IEffectApplier effect, IParticleData particle) {

        airConfigs.put(name, new SpecialAirConfig(name, light, power, effect, particle));
    }

    // endregion GRENADE

    // region CONVERSION

    public static final IBlockTransformer signalAirTransform = getConversionTransform(REPLACEABLE_AIR, SIGNAL_AIR.getDefaultState(), false);
    public static final IBlockTransformer glowAirTransform = getAirTransform(airConfigs.get("glow"));
    public static final IEffectApplier glowLiving = (target, duration, power, source) -> {
        target.addPotionEffect(new EffectInstance(Effects.GLOWING, duration, power));
        if (target.getCreatureAttribute() == CreatureAttribute.UNDEAD) {
            target.attackEntityFrom(DamageSource.causeExplosionDamage(source instanceof LivingEntity ? (LivingEntity) source : null), 4.0F);
            target.setFire(duration);
        }
    };
    public static final IBlockTransformer enderAirTransform = getConversionTransform(REPLACEABLE_AIR, ENDER_AIR.getDefaultState(), false);
    public static final IEffectApplier enderfereLiving = (target, duration, power, source) -> {
        if (target instanceof EndermanEntity || target instanceof EndermiteEntity) {
            target.addPotionEffect(new EffectInstance(ENDERFERENCE, duration, power));
            target.attackEntityFrom(DamageSource.causeExplosionDamage(source instanceof LivingEntity ? (LivingEntity) source : null), 4.0F);
        }
    };
    public static final IEffectApplier slimeLiving = new IEffectApplier() {
        @Override
        public void applyEffect(LivingEntity target, int duration, int power, @Nullable Entity source) {
            target.addPotionEffect(new EffectInstance(SLIMED, duration, power, false, true));


        }

        @Override
        public void applyEffectNearby(World worldIn, BlockPos pos, Predicate<? super LivingEntity> filter, int radius, int duration, int power, @Nullable Entity source) {

            AxisAlignedBB area = new AxisAlignedBB(pos.add(-radius, -radius, -radius), pos.add(1 + radius, 1 + radius, 1 + radius));
            List<LivingEntity> mobs = worldIn.getEntitiesWithinAABB(LivingEntity.class, area, filter);
            for (LivingEntity mob : mobs) {
                applyEffect(mob, duration, power, null);

                if (source != null) {
                    double d5 = mob.getPosX() - source.getPosX();
                    double d7 = mob.getPosY() - source.getPosY();
                    double d9 = mob.getPosZ() - source.getPosZ();
                    double d13 = net.minecraft.util.math.MathHelper.sqrt(d5 * d5 + d7 * d7 + d9 * d9);

                    if (d13 != 0.0D) {
                        d5 = d5 / d13;
                        d7 = d7 / d13;
                        d9 = d9 / d13;
                        double d12 = Math.sqrt(source.getDistanceSq(mob) / 32.0D);
                        double d14 = Explosion.getBlockDensity(source.getPositionVec(), mob);
                        double d11 = (radius - d12) * d14;
                        d11 *= (1.0D - mob.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                        if (mob instanceof ServerPlayerEntity) {
                            d11 /= 4.0D;
                            PlayerMotionPacket.sendToClient(d5 * d11, d7 * d11, d9 * d11, (ServerPlayerEntity) mob);
                        } else {
                            mob.setMotion(mob.getMotion().add(d5 * d11, d7 * d11, d9 * d11));
                        }
                    }
                }
            }
        }
    };

    public static final IBlockTransformer myceliumTransform = getConversionTransform(new ObjectOpenHashSet<>(new BlockState[]{DIRT.getDefaultState(), GRASS_BLOCK.getDefaultState()}), MYCELIUM.getDefaultState(), true);
    public static final IBlockTransformer grassTransform = getConversionTransform(DIRT.getDefaultState(), GRASS_BLOCK.getDefaultState(), true);

    // endregion CONVERSION

    // region GROWTH

    public static final IBlockTransformer growMushrooms = new IBlockTransformer() {
        @Override
        public boolean transformBlock(Entity entity, World world, BlockPos pos, Direction face) {

            Block below = world.getBlockState(pos.offset(Direction.DOWN)).getBlock();
            if (world.getBlockState(pos).isAir(world, pos) && (below.equals(MYCELIUM) || below.equals(PODZOL))) {
                return world.setBlockState(pos, world.rand.nextBoolean() ? BROWN_MUSHROOM.getDefaultState() : RED_MUSHROOM.getDefaultState());
            }
            return false;
        }

        @Override
        public void transformArea(Entity entity, World world, BlockPos pos, float radius, float chance, int max) {

            float f = Math.min(HORZ_MAX, radius);
            float v = Math.min(VERT_MAX, radius);
            float f2 = f * f;

            if (transformBlock(entity, world, pos.offset(Direction.UP), Direction.DOWN)) {
                max--;
            }
            for (BlockPos iterPos : BlockPos.getAllInBoxMutable(pos.add(-f, -v, -f), pos.add(f, v, f))) {
                if (max <= 0) {
                    return;
                }
                double distance = iterPos.distanceSq(entity.getPositionVec(), true);
                if (distance < f2) {
                    if (world.rand.nextDouble() < 0.5 - (distance / f2) && transformBlock(entity, world, iterPos, Direction.DOWN)) {
                        max--;
                    }
                }
            }
        }
    };

    public static final IBlockTransformer growPlants = (entity, world, pos, face) -> {

        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof IGrowable) {
            IGrowable growable = (IGrowable) state.getBlock();
            if (!world.isRemote && growable.canGrow(world, pos, state, world.isRemote) && growable.canUseBonemeal(world, world.rand, pos, state)) {
                // TODO: Remove try/catch when Mojang fixes base issue.
                try {
                    growable.grow((ServerWorld) world, world.rand, pos, state);
                } catch (Exception e) {
                    // Vanilla issue causes bamboo to crash if grown close to world height
                    if (!(growable instanceof BambooBlock)) {
                        throw e;
                    }
                }
                return true;
            }
        }
        return false;
    };

    // endregion GROWTH

    // region HELPERS

    private static IBlockTransformer getConversionTransform(Set<BlockState> replaceable, BlockState replacement, boolean requireAir) {
        if (requireAir)
            return (entity, worldIn, pos, face) -> {
                BlockPos above = pos.offset(Direction.UP);
                if (worldIn.getBlockState(above).isAir(worldIn, above) && replaceable.contains(worldIn.getBlockState(pos))) {
                    return worldIn.setBlockState(pos, replacement);
                }
                return false;
            };
        else
            return (entity, worldIn, pos, face) -> {
                if (replaceable.contains(worldIn.getBlockState(pos))) {
                    return worldIn.setBlockState(pos, replacement);
                }
                return false;
            };
    }

    private static IBlockTransformer getConversionTransform(BlockState replaceable, BlockState replacement, boolean requireAir) {
        if (requireAir)
            return (entity, worldIn, pos, face) -> {
                BlockPos above = pos.offset(Direction.UP);
                if (worldIn.getBlockState(above).isAir(worldIn, above) && replaceable.equals(worldIn.getBlockState(pos))) {
                    return worldIn.setBlockState(pos, replacement);
                }
                return false;
            };
        else
            return (entity, worldIn, pos, face) -> {
                if (replaceable.equals(worldIn.getBlockState(pos))) {
                    return worldIn.setBlockState(pos, replacement);
                }
                return false;
            };
    }

    private static IBlockTransformer getAirTransform(SpecialAirConfig config) {
        return (entity, worldIn, pos, face) -> {
            boolean succeeded = false;
            if (REPLACEABLE_AIR.contains(worldIn.getBlockState(pos))) {
                succeeded = worldIn.setBlockState(pos, SPECIAL_AIR.getDefaultState());
                TileEntity tile = worldIn.getTileEntity(pos);
                if (tile instanceof SpecialAirTile) {
                    ((SpecialAirTile) tile).addConfig(config);
                }
            }
            return succeeded;
        };
    }

    public static boolean isLitCampfire(BlockState state) {

        return state.getBlock() instanceof CampfireBlock && state.get(BlockStateProperties.LIT);
    }

    public static boolean isUnlitCampfire(BlockState state) {

        return state.getBlock() instanceof CampfireBlock && !state.get(BlockStateProperties.WATERLOGGED) && !state.get(BlockStateProperties.LIT);
    }

    public static boolean isUnlitTNT(BlockState state) {

        return state.getBlock() instanceof TNTBlock;
    }

    // endregion HELPERS

    // region INTERFACES

    public interface IEffectApplier {
        void applyEffect(LivingEntity target, int duration, int power, @Nullable Entity source);

        default void applyEffectNearby(World worldIn, BlockPos pos, Predicate<? super LivingEntity> filter, int radius, int duration, int power, @Nullable Entity source) {

            AxisAlignedBB area = new AxisAlignedBB(pos.add(-radius, -radius, -radius), pos.add(1 + radius, 1 + radius, 1 + radius));
            worldIn.getEntitiesWithinAABB(LivingEntity.class, area, filter)
                    .forEach(livingEntity -> this.applyEffect(livingEntity, duration, power, source));
        }

        default void applyEffectNearby(World worldIn, BlockPos pos, int radius, int duration, int power, @Nullable Entity source) {

            applyEffectNearby(worldIn, pos, EntityPredicates.IS_ALIVE, radius, duration, power, source);
        }

        default void applyEffectNearby(World worldIn, BlockPos pos, int radius, int duration, int power) {

            applyEffectNearby(worldIn, pos, radius, duration, power, null);
        }
    }

    public interface IBlockTransformer {
        boolean transformBlock(Entity entity, World world, BlockPos pos, Direction face);

        default void transformArea(Entity entity, World world, BlockPos pos, float radius) {
            transformArea(entity, world, pos, radius, 1.0F);
        }

        default void transformArea(Entity entity, World world, BlockPos pos, float radius, float chance) {

            float f = Math.min(HORZ_MAX, radius);
            float v = Math.min(VERT_MAX, radius);
            float f2 = f * f;

            for (BlockPos iterPos : BlockPos.getAllInBoxMutable(pos.add(-f, -v, -f), pos.add(f, v, f))) {
                double distance = iterPos.distanceSq(entity.getPositionVec(), true);
                if (distance < f2 && (chance > 0.99999F || world.rand.nextDouble() < chance)) {
                    transformBlock(entity, world, iterPos, Direction.DOWN);
                }
            }
        }

        default void transformArea(Entity entity, World world, BlockPos pos, float radius, float chance, int max) {

            float f = Math.min(HORZ_MAX, radius);
            float v = Math.min(VERT_MAX, radius);
            float f2 = f * f;

            if (transformBlock(entity, world, pos, Direction.DOWN)) {
                max--;
            }
            for (BlockPos iterPos : BlockPos.getAllInBoxMutable(pos.add(-f, -v, -f), pos.add(f, v, f))) {
                if (max <= 0) {
                    return;
                }
                double distance = iterPos.distanceSq(entity.getPositionVec(), true);
                if (distance < f2 && (chance > 0.99999F || world.rand.nextDouble() < chance) && transformBlock(entity, world, iterPos, Direction.DOWN)) {
                   max--;
                }
            }
        }
    }

    public static class Element {
        public final String name;
        public final IEffectApplier effectApplier;
        public final IBlockTransformer blockTransformer;

        public Element(String name, IEffectApplier effectApplier, IBlockTransformer blockTransformer) {
            this.name = name;
            this.effectApplier = effectApplier;
            this.blockTransformer = blockTransformer;
        }

        public boolean isElement() {
            return effectApplier != null && blockTransformer != null;
        }
    }

    public static class GrenadeConfig {
        public final String name;
        public final IEffectApplier effectApplier;
        public final IBlockTransformer blockTransformer;

        public GrenadeConfig(String name, IEffectApplier effectApplier, IBlockTransformer blockTransformer) {
            this.name = name;
            this.effectApplier = effectApplier;
            this.blockTransformer = blockTransformer;
        }
    }

    // endregion INTERFACES

    // OLD
    // region BURNING
    public static void igniteNearbyEntities(Entity entity, World worldIn, BlockPos pos, int radius, int duration) {

        AxisAlignedBB area = new AxisAlignedBB(pos.add(-radius, -radius, -radius), pos.add(1 + radius, 1 + radius, 1 + radius));
        List<LivingEntity> mobs = worldIn.getEntitiesWithinAABB(LivingEntity.class, area, EntityPredicates.IS_ALIVE);
        mobs.removeIf(Entity::isInWater);
        mobs.removeIf(Entity::isImmuneToFire);
        mobs.removeIf(mob -> mob instanceof EndermanEntity);
        for (LivingEntity mob : mobs) {
            mob.setFire(duration);
        }
    }

    public static void igniteNearbyGround(Entity entity, World worldIn, BlockPos pos, int radius, double chance) {

        float f = (float) Math.min(HORZ_MAX, radius);
        float v = (float) Math.min(VERT_MAX, radius);
        float f2 = f * f;
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (BlockPos blockpos : BlockPos.getAllInBoxMutable(pos.add(-f, -v, -f), pos.add(f, v, f))) {
            double distance = blockpos.distanceSq(entity.getPositionVec(), true);
            if (distance < f2) {
                mutable.setPos(blockpos.getX(), blockpos.getY() + 1, blockpos.getZ());
                BlockState blockstate1 = worldIn.getBlockState(mutable);
                if (blockstate1.isAir(worldIn, mutable)) {
                    if (isValidFirePosition(worldIn, mutable, chance)) {
                        worldIn.setBlockState(mutable, ((FireBlock) FIRE).getStateForPlacement(worldIn, mutable));
                    }
                }
            }
        }
    }

    public static void igniteSpecial(Entity entity, World worldIn, BlockPos pos, int radius, boolean campfire, boolean tnt, @Nullable Entity igniter) {

        float f = (float) Math.min(HORZ_MAX, radius);
        float v = (float) Math.min(VERT_MAX, radius);
        float f2 = f * f;

        for (BlockPos blockpos : BlockPos.getAllInBoxMutable(pos.add(-f, -v, -f), pos.add(f, v, f))) {
            double distance = blockpos.distanceSq(entity.getPositionVec(), true);
            if (distance < f2) {
                BlockState state = worldIn.getBlockState(blockpos);
                if (campfire && isUnlitCampfire(state)) {
                    worldIn.setBlockState(blockpos, state.with(BlockStateProperties.LIT, true));
                } else if (tnt && isUnlitTNT(state)) {
                    state.getBlock().catchFire(state, worldIn, blockpos, Direction.UP, igniter instanceof LivingEntity ? (LivingEntity) igniter : null);
                    worldIn.setBlockState(blockpos, AIR.getDefaultState());
                }
            }
        }
    }

    public static boolean isValidFirePosition(World worldIn, BlockPos pos, double chance) {

        BlockPos below = pos.down();
        BlockState state = worldIn.getBlockState(below);
        if (Block.doesSideFillSquare(state.getCollisionShape(worldIn, below), Direction.UP)) {
            return state.getMaterial().isFlammable() || worldIn.rand.nextDouble() < chance; // Random chance.
        }
        return false;
    }

    // endregion

    // region FREEZING
    public static void freezeNearbyGround(Entity entity, World worldIn, BlockPos pos, int radius) {

        BlockState state = SNOW.getDefaultState();
        float f = (float) Math.min(HORZ_MAX, radius);
        float v = (float) Math.min(VERT_MAX, radius);
        float f2 = f * f;
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (BlockPos blockpos : BlockPos.getAllInBoxMutable(pos.add(-f, -v, -f), pos.add(f, v, f))) {
            double distance = blockpos.distanceSq(entity.getPositionVec(), true);
            if (distance < f2) {
                mutable.setPos(blockpos.getX(), blockpos.getY() + 1, blockpos.getZ());
                BlockState blockstate1 = worldIn.getBlockState(mutable);
                if (blockstate1.isAir(worldIn, mutable)) {
                    if (worldIn.getBiome(mutable).getTemperature(blockpos) < 0.8F && isValidSnowPosition(worldIn, mutable)) {
                        worldIn.setBlockState(mutable, state);
                    }
                }
            }
        }
    }

    public static void freezeSpecial(Entity entity, World worldIn, BlockPos pos, int radius, boolean campfire, boolean fire) {

        float f = (float) Math.min(HORZ_MAX, radius);
        float v = (float) Math.min(VERT_MAX, radius);
        float f2 = f * f;

        for (BlockPos blockpos : BlockPos.getAllInBoxMutable(pos.add(-f, -v, -f), pos.add(f, v, f))) {
            double distance = blockpos.distanceSq(entity.getPositionVec(), true);
            if (distance < f2) {
                BlockState state = worldIn.getBlockState(blockpos);
                if (campfire && isLitCampfire(state)) {
                    worldIn.setBlockState(blockpos, state.with(BlockStateProperties.LIT, false));
                } else if (fire && state.getBlock() == FIRE) {
                    worldIn.setBlockState(blockpos, AIR.getDefaultState());
                }
            }
        }
    }

    public static void freezeSurfaceWater(Entity entity, World worldIn, BlockPos pos, int radius, boolean permanent) {

        BlockState state = permanent ? ICE.getDefaultState() : FROSTED_ICE.getDefaultState();
        float f = (float) Math.min(HORZ_MAX, radius);
        float v = (float) Math.min(VERT_MAX, radius);
        float f2 = f * f;
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (BlockPos blockpos : BlockPos.getAllInBoxMutable(pos.add(-f, -v, -f), pos.add(f, v, f))) {
            double distance = blockpos.distanceSq(entity.getPositionVec(), true);
            if (distance < f2) {
                mutable.setPos(blockpos.getX(), blockpos.getY() + 1, blockpos.getZ());
                BlockState blockstate1 = worldIn.getBlockState(mutable);
                if (blockstate1.isAir(worldIn, mutable)) {
                    BlockState blockstate2 = worldIn.getBlockState(blockpos);
                    boolean isFull = blockstate2.getBlock() == WATER && blockstate2.get(FlowingFluidBlock.LEVEL) == 0;
                    if (blockstate2.getMaterial() == Material.WATER && isFull && state.isValidPosition(worldIn, blockpos) && worldIn.placedBlockCollides(state, blockpos, ISelectionContext.dummy())) {
                        worldIn.setBlockState(blockpos, state);
                        if (!permanent) {
                            worldIn.getPendingBlockTicks().scheduleTick(blockpos, FROSTED_ICE, MathHelper.nextInt(worldIn.rand, 60, 120));
                        }
                    }
                }
            }
        }
    }

    public static void freezeAllWater(Entity entity, World worldIn, BlockPos pos, int radius, boolean permanent) {

        BlockState state = permanent ? ICE.getDefaultState() : FROSTED_ICE.getDefaultState();
        float f = (float) Math.min(HORZ_MAX, radius);
        float v = (float) Math.min(VERT_MAX, radius);
        float f2 = f * f;

        for (BlockPos blockpos : BlockPos.getAllInBoxMutable(pos.add(-f, -v, -f), pos.add(f, v, f))) {
            double distance = blockpos.distanceSq(entity.getPositionVec(), true);
            if (distance < f2) {
                BlockState blockstate2 = worldIn.getBlockState(blockpos);
                boolean isFull = blockstate2.getBlock() == WATER && blockstate2.get(FlowingFluidBlock.LEVEL) == 0;
                if (blockstate2.getMaterial() == Material.WATER && isFull && state.isValidPosition(worldIn, blockpos) && worldIn.placedBlockCollides(state, blockpos, ISelectionContext.dummy())) {
                    worldIn.setBlockState(blockpos, state);
                    if (!permanent) {
                        worldIn.getPendingBlockTicks().scheduleTick(blockpos, FROSTED_ICE, MathHelper.nextInt(worldIn.rand, 60, 120));
                    }
                }
            }
        }
    }

    public static void freezeSurfaceLava(Entity entity, World worldIn, BlockPos pos, int radius, boolean permanent) {

        if (GLOSSED_MAGMA == null && !permanent) {
            return;
        }
        BlockState state = permanent ? OBSIDIAN.getDefaultState() : GLOSSED_MAGMA.getDefaultState();
        float f = (float) Math.min(HORZ_MAX, radius);
        float v = (float) Math.min(VERT_MAX, radius);
        float f2 = f * f;
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (BlockPos blockpos : BlockPos.getAllInBoxMutable(pos.add(-f, -v, -f), pos.add(f, v, f))) {
            double distance = blockpos.distanceSq(entity.getPositionVec(), true);
            if (distance < f2) {
                mutable.setPos(blockpos.getX(), blockpos.getY() + 1, blockpos.getZ());
                BlockState blockstate1 = worldIn.getBlockState(mutable);
                if (blockstate1.isAir(worldIn, mutable)) {
                    BlockState blockstate2 = worldIn.getBlockState(blockpos);
                    boolean isFull = blockstate2.getBlock() == LAVA && blockstate2.get(FlowingFluidBlock.LEVEL) == 0;
                    if (blockstate2.getMaterial() == Material.LAVA && isFull && state.isValidPosition(worldIn, blockpos) && worldIn.placedBlockCollides(state, blockpos, ISelectionContext.dummy())) {
                        worldIn.setBlockState(blockpos, state);
                        if (!permanent) {
                            worldIn.getPendingBlockTicks().scheduleTick(blockpos, GLOSSED_MAGMA, MathHelper.nextInt(worldIn.rand, 60, 120));
                        }
                    }
                }
            }
        }
    }

    public static void freezeAllLava(Entity entity, World worldIn, BlockPos pos, int radius, boolean permanent) {

        if (GLOSSED_MAGMA == null && !permanent) {
            return;
        }
        BlockState state = permanent ? OBSIDIAN.getDefaultState() : GLOSSED_MAGMA.getDefaultState();
        float f = (float) Math.min(HORZ_MAX, radius);
        float v = (float) Math.min(VERT_MAX, radius);
        float f2 = f * f;

        for (BlockPos blockpos : BlockPos.getAllInBoxMutable(pos.add(-f, -v, -f), pos.add(f, v, f))) {
            double distance = blockpos.distanceSq(entity.getPositionVec(), true);
            if (distance < f2) {
                BlockState blockstate2 = worldIn.getBlockState(blockpos);
                boolean isFull = blockstate2.getBlock() == LAVA && blockstate2.get(FlowingFluidBlock.LEVEL) == 0;
                if (blockstate2.getMaterial() == Material.LAVA && isFull && state.isValidPosition(worldIn, blockpos) && worldIn.placedBlockCollides(state, blockpos, ISelectionContext.dummy())) {
                    worldIn.setBlockState(blockpos, state);
                    if (!permanent) {
                        worldIn.getPendingBlockTicks().scheduleTick(blockpos, GLOSSED_MAGMA, MathHelper.nextInt(worldIn.rand, 60, 120));
                    }
                }
            }
        }
    }

    public static boolean isValidSnowPosition(World worldIn, BlockPos pos) {

        BlockState state = worldIn.getBlockState(pos.down());
        Block block = state.getBlock();
        if (block == ICE || block == PACKED_ICE || block == BARRIER || block == FROSTED_ICE || block == GLOSSED_MAGMA) {
            return false;
        }
        return Block.doesSideFillSquare(state.getCollisionShape(worldIn, pos.down()), Direction.UP) || block == SNOW && state.get(SnowBlock.LAYERS) == 8;
    }
    // endregion

    // region AREA TRANSFORMS / MISC
    private static boolean isValidLightningBoltPosition(World worldIn, BlockPos pos, double chance) {

        BlockPos below = pos.down();
        BlockState state = worldIn.getBlockState(below);
        if (worldIn.canSeeSky(pos) && Block.doesSideFillSquare(state.getCollisionShape(worldIn, below), Direction.UP)) {
            return worldIn.rand.nextDouble() < chance; // Random chance.
        }
        return false;
    }

    public static void transformArea(Entity entity, World worldIn, BlockPos pos, BlockState replaceable, BlockState replacement, int radius, boolean requireAir) {

        float f = (float) Math.min(HORZ_MAX, radius);
        float v = (float) Math.min(VERT_MAX, radius);
        float f2 = f * f;
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        if (requireAir) {
            for (BlockPos iterPos : BlockPos.getAllInBoxMutable(pos.add(-f, -v, -f), pos.add(f, v, f))) {
                double distance = iterPos.distanceSq(entity.getPositionVec(), true);
                if (distance < f2) {
                    mutable.setPos(iterPos.getX(), iterPos.getY() + 1, iterPos.getZ());
                    BlockState blockstate1 = worldIn.getBlockState(mutable);
                    if (blockstate1.isAir(worldIn, mutable)) {
                        if (worldIn.getBlockState(iterPos) == replaceable) {
                            worldIn.setBlockState(iterPos, replacement);
                        }
                    }
                }
            }
        } else {
            for (BlockPos iterPos : BlockPos.getAllInBoxMutable(pos.add(-f, -v, -f), pos.add(f, v, f))) {
                double distance = iterPos.distanceSq(entity.getPositionVec(), true);
                if (distance < f2) {
                    if (worldIn.getBlockState(iterPos) == replaceable) {
                        worldIn.setBlockState(iterPos, replacement);
                    }
                }
            }
        }
    }

    public static void transformArea(Entity entity, World worldIn, BlockPos pos, Set<BlockState> replaceable, BlockState replacement, int radius, boolean requireAir) {

        float f = (float) Math.min(HORZ_MAX, radius);
        float v = (float) Math.min(VERT_MAX, radius);
        float f2 = f * f;
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        if (requireAir) {
            for (BlockPos blockpos : BlockPos.getAllInBoxMutable(pos.add(-f, -v, -f), pos.add(f, v, f))) {
                double distance = blockpos.distanceSq(entity.getPositionVec(), true);
                if (distance < f2) {
                    mutable.setPos(blockpos.getX(), blockpos.getY() + 1, blockpos.getZ());
                    BlockState blockstate1 = worldIn.getBlockState(mutable);
                    if (blockstate1.isAir(worldIn, mutable)) {
                        if (replaceable.contains(worldIn.getBlockState(blockpos))) {
                            worldIn.setBlockState(blockpos, replacement);
                        }
                    }
                }
            }
        } else {
            for (BlockPos iterPos : BlockPos.getAllInBoxMutable(pos.add(-f, -v, -f), pos.add(f, v, f))) {
                if (iterPos.withinDistance(entity.getPositionVec(), f)) {
                    if (replaceable.contains(worldIn.getBlockState(iterPos))) {
                        worldIn.setBlockState(iterPos, replacement);
                    }
                }
            }
        }
    }

    public static void transformGrass(Entity entity, World worldIn, BlockPos pos, int radius) {

        transformArea(entity, worldIn, pos, DIRT.getDefaultState(), GRASS_BLOCK.getDefaultState(), radius, true);
    }

    public static void transformMycelium(Entity entity, World worldIn, BlockPos pos, int radius) {

        Set<BlockState> replaceable = new ObjectOpenHashSet<>();
        Collections.addAll(replaceable, DIRT.getDefaultState(), GRASS_BLOCK.getDefaultState());
        transformArea(entity, worldIn, pos, replaceable, MYCELIUM.getDefaultState(), radius, true);
    }

    public static void transformSignalAir(Entity entity, World worldIn, BlockPos pos, int radius) {

        Set<BlockState> replaceable = new ObjectOpenHashSet<>();
        Collections.addAll(replaceable, AIR.getDefaultState(), CAVE_AIR.getDefaultState());
        transformArea(entity, worldIn, pos, replaceable, SIGNAL_AIR.getDefaultState(), radius, false);
    }

    public static void transformGlowAir(Entity entity, World worldIn, BlockPos pos, int radius) {

        Set<BlockState> replaceable = new ObjectOpenHashSet<>();
        Collections.addAll(replaceable, AIR.getDefaultState(), CAVE_AIR.getDefaultState());
        transformArea(entity, worldIn, pos, replaceable, GLOW_AIR.getDefaultState(), radius, false);
    }

    public static void transformEnderAir(Entity entity, World worldIn, BlockPos pos, int radius) {

        Set<BlockState> replaceable = new ObjectOpenHashSet<>();
        Collections.addAll(replaceable, AIR.getDefaultState(), CAVE_AIR.getDefaultState());
        transformArea(entity, worldIn, pos, replaceable, ENDER_AIR.getDefaultState(), radius, false);
    }

    public static void zapNearbyGround(Entity entity, World worldIn, BlockPos pos, int radius, double chance, int max) {

        float f = (float) Math.min(HORZ_MAX, radius);
        float v = (float) Math.min(VERT_MAX, radius);
        float f2 = f * f;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int count = 0;

        for (BlockPos blockpos : BlockPos.getAllInBoxMutable(pos.add(-f, -v, -f), pos.add(f, v, f))) {
            if (count >= max) {
                return;
            }
            double distance = blockpos.distanceSq(entity.getPositionVec(), true);
            if (distance < f2) {
                mutable.setPos(blockpos.getX(), blockpos.getY() + 1, blockpos.getZ());
                BlockState blockstate1 = worldIn.getBlockState(mutable);
                if (blockstate1.isAir(worldIn, mutable)) {
                    if (isValidLightningBoltPosition(worldIn, mutable, chance)) {
                        worldIn.setBlockState(mutable, LIGHTNING_AIR.getDefaultState());
                        ++count;
                    }
                }
            }
        }
    }

    public static void growMushrooms(Entity entity, World worldIn, BlockPos pos, int radius, int count) {

        float f = (float) Math.min(HORZ_MAX, radius);
        float v = (float) Math.min(VERT_MAX, radius);
        float f2 = f * f;
        int grow = 0;
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        mutable.setPos(entity.getPosition().up());
        BlockState blockstate1 = worldIn.getBlockState(mutable);
        if (blockstate1.isAir(worldIn, mutable)) {
            if (isValidMushroomPosition(worldIn, entity.getPosition(), 1.0)) {
                worldIn.setBlockState(mutable, worldIn.rand.nextBoolean() ? BROWN_MUSHROOM.getDefaultState() : RED_MUSHROOM.getDefaultState());
                ++grow;
            }
        }
        for (BlockPos iterPos : BlockPos.getAllInBoxMutable(pos.add(-f, -v, -f), pos.add(f, v, f))) {
            if (grow >= count) {
                return;
            }
            double distance = iterPos.distanceSq(entity.getPositionVec(), true);
            if (distance < f2) {
                mutable.setPos(iterPos.getX(), iterPos.getY() + 1, iterPos.getZ());
                blockstate1 = worldIn.getBlockState(mutable);
                if (blockstate1.isAir(worldIn, mutable)) {
                    if (isValidMushroomPosition(worldIn, iterPos, 0.5 - (distance / f2))) {
                        worldIn.setBlockState(mutable, worldIn.rand.nextBoolean() ? BROWN_MUSHROOM.getDefaultState() : RED_MUSHROOM.getDefaultState());
                        ++grow;
                    }
                }
            }
        }
    }

    private static boolean isValidMushroomPosition(World worldIn, BlockPos pos, double chance) {

        Block block = worldIn.getBlockState(pos).getBlock();
        return worldIn.rand.nextDouble() < chance && (block == MYCELIUM || block == PODZOL);
    }

    public static void growPlants(Entity entity, World worldIn, BlockPos pos, int radius) {

        float f = (float) Math.min(HORZ_MAX, radius);
        float v = (float) Math.min(VERT_MAX, radius);
        float f2 = f * f;

        BlockState state;
        for (BlockPos iterPos : BlockPos.getAllInBoxMutable(pos.add(-f, -v, -f), pos.add(f, v, f))) {
            double distance = iterPos.distanceSq(entity.getPositionVec(), true);
            if (distance < f2) {
                state = worldIn.getBlockState(iterPos);
                if (state.getBlock() instanceof IGrowable) {
                    IGrowable growable = (IGrowable) state.getBlock();
                    if (growable.canGrow(worldIn, iterPos, state, worldIn.isRemote)) {
                        if (!worldIn.isRemote) {
                            if (growable.canUseBonemeal(worldIn, worldIn.rand, iterPos, state)) {
                                // TODO: Remove try/catch when Mojang fixes base issue.
                                try {
                                    growable.grow((ServerWorld) worldIn, worldIn.rand, iterPos, state);
                                } catch (Exception e) {
                                    // Vanilla issue causes bamboo to crash if grown close to world height
                                    if (!(growable instanceof BambooBlock)) {
                                        throw e;
                                    }
                                }
                                // growable.grow((ServerWorld) worldIn, worldIn.rand, pos, state);
                                // ++grow;
                            }
                        }
                    }
                }
            }
        }
    }

    public static void growPlants(Entity entity, World worldIn, BlockPos pos, int radius, int count) {

        float f = (float) Math.min(HORZ_MAX, radius);
        float v = (float) Math.min(VERT_MAX, radius);
        float f2 = f * f;
        int grow = 0;

        BlockState state = worldIn.getBlockState(entity.getPosition());
        if (state.getBlock() instanceof IGrowable) {
            IGrowable growable = (IGrowable) state.getBlock();
            if (growable.canGrow(worldIn, pos, state, worldIn.isRemote)) {
                if (!worldIn.isRemote) {
                    if (growable.canUseBonemeal(worldIn, worldIn.rand, pos, state)) {
                        // TODO: Remove try/catch when Mojang fixes base issue.
                        try {
                            growable.grow((ServerWorld) worldIn, worldIn.rand, pos, state);
                            ++grow;
                        } catch (Exception e) {
                            // Vanilla issue causes bamboo to crash if grown close to world height
                            if (!(growable instanceof BambooBlock)) {
                                throw e;
                            }
                        }
                    }
                }
            }
        }
        for (BlockPos iterPos : BlockPos.getAllInBoxMutable(pos.add(-f, -v, -f), pos.add(f, v, f))) {
            if (grow >= count) {
                return;
            }
            double distance = iterPos.distanceSq(entity.getPositionVec(), true);
            if (distance < f2) {
                state = worldIn.getBlockState(iterPos);
                if (state.getBlock() instanceof IGrowable) {
                    IGrowable growable = (IGrowable) state.getBlock();
                    if (growable.canGrow(worldIn, iterPos, state, worldIn.isRemote)) {
                        if (!worldIn.isRemote) {
                            if (growable.canUseBonemeal(worldIn, worldIn.rand, iterPos, state)) {
                                // TODO: Remove try/catch when Mojang fixes base issue.
                                try {
                                    growable.grow((ServerWorld) worldIn, worldIn.rand, iterPos, state);
                                    ++grow;
                                } catch (Exception e) {
                                    // Vanilla issue causes bamboo to crash if grown close to world height
                                    if (!(growable instanceof BambooBlock)) {
                                        throw e;
                                    }
                                }
                                // growable.grow((ServerWorld) worldIn, worldIn.rand, pos, state);
                                // ++grow;
                            }
                        }
                    }
                }
            }
        }
    }
    // endregion
}
