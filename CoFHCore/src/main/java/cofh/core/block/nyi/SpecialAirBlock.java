package cofh.core.block.nyi;

import cofh.core.tileentity.GlowAirTile;
import cofh.core.tileentity.SignalAirTile;
import cofh.core.tileentity.SpecialAirTile;
import cofh.lib.util.Utils;
import cofh.lib.util.helpers.MathHelper;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class SpecialAirBlock extends AirBlock {

    public static final IntegerProperty LIGHT = IntegerProperty.create("light", 0, 15);
    public static final int effectDuration = 2;
    public static final int effectPower = 0;

    public SpecialAirBlock(Properties builder) {

        super(builder);
        this.setDefaultState(this.stateContainer.getBaseState().with(LIGHT, 0));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {

        builder.add(LIGHT);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {

        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {

        return new SpecialAirTile();
    }

    @Override
    public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {

        TileEntity tile = blockAccess.getTileEntity(pos);
        return tile instanceof SpecialAirTile ? ((SpecialAirTile) tile).getPower() : 0;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof SpecialAirTile) {
            List<IParticleData> particles = ((SpecialAirTile) tile).getParticles();
//            System.out.println(particles.size());
            int randInt = rand.nextInt(Math.max(16, particles.size()));
            if (particles.size() > randInt) {
                Utils.spawnBlockParticlesClient(worldIn, particles.get(randInt), pos, rand, 2);
            }
        }
    }

    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {

        if (Utils.isClientWorld(worldIn)) {
            return;
        }

        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof SpecialAirTile && entityIn instanceof LivingEntity) {
            ((SpecialAirTile) tile).getEffects().forEach(
                    (effect) -> effect.applyEffect((LivingEntity) entityIn, effectDuration, effectPower, null));
        }
    }
}
