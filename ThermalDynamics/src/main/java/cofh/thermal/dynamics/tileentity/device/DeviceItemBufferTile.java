package cofh.thermal.dynamics.tileentity.device;

import cofh.thermal.dynamics.inventory.container.device.DeviceItemBufferContainer;
import cofh.thermal.lib.tileentity.DeviceTileBase;
import cofh.thermal.lib.tileentity.ThermalTileBase;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nullable;

import static cofh.lib.util.StorageGroup.ACCESSIBLE;
import static cofh.lib.util.StorageGroup.INTERNAL;
import static cofh.thermal.dynamics.init.TDynReferences.DEVICE_ITEM_BUFFER_TILE;
import static cofh.thermal.lib.common.ThermalConfig.deviceAugments;

public class DeviceItemBufferTile extends DeviceTileBase {

    protected boolean inputLock;
    protected boolean outputLock;

    public DeviceItemBufferTile() {

        super(DEVICE_ITEM_BUFFER_TILE);

        inventory.addSlots(ACCESSIBLE, 9);

        inventory.addSlots(INTERNAL, 9);

        addAugmentSlots(deviceAugments);
        initHandlers();
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory inventory, PlayerEntity player) {

        return new DeviceItemBufferContainer(i, world, pos, inventory, player);
    }

    // region NETWORK

    // CONFIG
    @Override
    public PacketBuffer getConfigPacket(PacketBuffer buffer) {

        super.getConfigPacket(buffer);

        return buffer;
    }

    @Override
    public void handleConfigPacket(PacketBuffer buffer) {

        super.handleConfigPacket(buffer);
    }

    // GUI
    @Override
    public PacketBuffer getGuiPacket(PacketBuffer buffer) {

        super.getGuiPacket(buffer);

        return buffer;
    }

    @Override
    public void handleGuiPacket(PacketBuffer buffer) {

        super.handleGuiPacket(buffer);
    }
    // endregion

    // region NBT
    @Override
    public void read(BlockState state, CompoundNBT nbt) {

        super.read(state, nbt);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {

        super.write(nbt);

        return nbt;
    }
    // endregion

}
