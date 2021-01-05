package cofh.core.tileentity;

import cofh.core.network.packet.client.TileGuiPacket;
import cofh.core.util.helpers.FluidHelper;
import cofh.lib.tileentity.ITileCallback;
import cofh.lib.tileentity.ITilePacketHandler;
import cofh.lib.util.IConveyableData;
import cofh.lib.util.Utils;
import cofh.lib.util.control.ISecurable;
import cofh.lib.util.helpers.XpHelper;
import cofh.lib.xp.XpStorage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nullable;

public class TileCoFH extends TileEntity implements ITileCallback, ITilePacketHandler, IConveyableData {

    protected int numPlayersUsing;

    public TileCoFH(TileEntityType<?> tileEntityTypeIn) {

        super(tileEntityTypeIn);
    }

    @Override
    public void onLoad() {

        super.onLoad();

        if (world != null && Utils.isClientWorld(world) && !hasClientUpdate()) {
            world.tickableTileEntities.remove(this);
        }
        validate();
    }

    public int getPlayersUsing() {

        return numPlayersUsing;
    }

    public void addPlayerUsing() {

        ++numPlayersUsing;
    }

    public void removePlayerUsing() {

        --numPlayersUsing;
    }

    public void receiveGuiNetworkData(int id, int data) {

    }

    public void sendGuiNetworkData(Container container, IContainerListener player) {

        if (player instanceof ServerPlayerEntity && (!(player instanceof FakePlayer))) {
            TileGuiPacket.sendToClient(this, (ServerPlayerEntity) player);
        }
    }

    // region HELPERS
    public TileCoFH worldContext(BlockState state, IBlockReader world) {

        return this;
    }

    public int getComparatorInputOverride() {

        return 0;
    }

    public int getLightValue() {

        return 0;
    }

    public void neighborChanged(Block blockIn, BlockPos fromPos) {

    }

    public boolean onActivatedDelegate(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, BlockRayTraceResult result) {

        return getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).map(handler -> FluidHelper.interactWithHandler(player.getHeldItem(hand), handler, player, hand)).orElse(false);
    }

    public boolean hasClientUpdate() {

        return false;
    }
    // endregion

    // region GUI
    public boolean canOpenGui() {

        return this instanceof INamedContainerProvider;
    }

    public boolean canPlayerChange(PlayerEntity player) {

        return !(this instanceof ISecurable) || ((ISecurable) this).canAccess(player);
    }

    public boolean playerWithinDistance(PlayerEntity player, double distanceSq) {

        return pos.distanceSq(player.getPositionVec(), true) <= distanceSq;
    }

    public boolean clearEnergy(int coil) {

        return false;
    }

    public boolean claimXP(PlayerEntity player) {

        if (getXpStorage() != null) {
            int xp = getXpStorage().getStored();
            XpHelper.addXPToPlayer(player, xp);
            getXpStorage().clear();
            return true;
        }
        return false;

    }

    public void spawnXpOrbs(int xp, Vector3d pos) {

        if (world == null) {
            return;
        }
        while (xp > 0) {
            int orbAmount = ExperienceOrbEntity.getXPSplit(xp);
            xp -= orbAmount;
            world.addEntity(new ExperienceOrbEntity(world, pos.x, pos.y, pos.z, orbAmount));
        }
    }

    public XpStorage getXpStorage() {

        return null;
    }
    // endregion

    // region TIME CHECKS
    public static final int TIME_CONSTANT = 32;

    public static final int TIME_CONSTANT_2X = TIME_CONSTANT * 2;

    public static final int TIME_CONSTANT_HALF = TIME_CONSTANT / 2;
    public static final int TIME_CONSTANT_QUARTER = TIME_CONSTANT / 4;
    public static final int TIME_CONSTANT_EIGHTH = TIME_CONSTANT / 8;

    protected final boolean timeCheck() {

        return world.getGameTime() % TIME_CONSTANT == 0;
    }

    protected final boolean timeCheckHalf() {

        return world.getGameTime() % TIME_CONSTANT_HALF == 0;
    }

    protected final boolean timeCheckQuarter() {

        return world.getGameTime() % TIME_CONSTANT_QUARTER == 0;
    }

    protected final boolean timeCheckEighth() {

        return world.getGameTime() % TIME_CONSTANT_EIGHTH == 0;
    }
    // endregion

    // region NETWORK
    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {

        return new SUpdateTileEntityPacket(pos, 0, getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {

        return this.write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {

        read(this.cachedBlockState, pkt.getNbtCompound());
    }

    public void setActive(boolean active) {

    }
    // endregion

    // region ITileCallback
    @Override
    public Block block() {

        return getBlockState().getBlock();
    }

    @Override
    public BlockState state() {

        return getBlockState();
    }

    @Override
    public BlockPos pos() {

        return pos;
    }

    @Override
    public World world() {

        return world;
    }
    // endregion
}
