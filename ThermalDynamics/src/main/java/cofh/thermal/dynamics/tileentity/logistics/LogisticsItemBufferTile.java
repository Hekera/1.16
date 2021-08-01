package cofh.thermal.dynamics.tileentity.logistics;

import cofh.lib.inventory.ItemStorageCoFH;
import cofh.thermal.dynamics.inventory.BufferItemInv;
import cofh.thermal.dynamics.inventory.BufferItemStorage;
import cofh.thermal.dynamics.inventory.container.logistics.LogisticsItemBufferContainer;
import cofh.thermal.lib.tileentity.ThermalTileSecurable;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

import static cofh.lib.util.StorageGroup.ACCESSIBLE;
import static cofh.lib.util.StorageGroup.INTERNAL;
import static cofh.lib.util.constants.NBTTags.TAG_ITEM_INV;
import static cofh.thermal.dynamics.init.TDynReferences.LOGISTICS_ITEM_BUFFER_TILE;

public class LogisticsItemBufferTile extends ThermalTileSecurable implements INamedContainerProvider {

    protected BufferItemInv inventory = new BufferItemInv(this, TAG_ITEM_INV);

    protected boolean checkNBT;
    protected boolean latch;

    protected boolean inputLock;
    protected boolean outputLock;

    public LogisticsItemBufferTile() {

        super(LOGISTICS_ITEM_BUFFER_TILE);

        BufferItemStorage[] accessible = new BufferItemStorage[9];
        ItemStorageCoFH[] internal = new ItemStorageCoFH[9];

        for (int i = 0; i < 9; ++i) {
            internal[i] = new ItemStorageCoFH();
            accessible[i] = new BufferItemStorage(internal[i]);
        }
        for (int i = 0; i < 9; ++i) {
            inventory.addSlot(accessible[i], ACCESSIBLE);
        }
        for (int i = 0; i < 9; ++i) {
            inventory.addSlot(internal[i], INTERNAL);
        }
        inventory.initHandlers();
        inventory.setConditions(() -> !inputLock, () -> !outputLock);
    }

    @Override
    public int invSize() {

        return inventory.getSlots();
    }

    public BufferItemInv getItemInv() {

        return inventory;
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory inventory, PlayerEntity player) {

        return new LogisticsItemBufferContainer(i, world, pos, inventory, player);
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

        inventory.read(nbt);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {

        super.write(nbt);

        inventory.write(nbt);

        return nbt;
    }
    // endregion

    // region INamedContainerProvider
    @Override
    public ITextComponent getDisplayName() {

        return new TranslationTextComponent(this.getBlockState().getBlock().getTranslationKey());
    }
    // endregion
}
