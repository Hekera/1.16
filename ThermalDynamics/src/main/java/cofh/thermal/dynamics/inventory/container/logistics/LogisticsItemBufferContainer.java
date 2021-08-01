package cofh.thermal.dynamics.inventory.container.logistics;

import cofh.core.inventory.container.TileContainer;
import cofh.lib.inventory.container.slot.SlotCoFH;
import cofh.lib.inventory.wrapper.InvWrapperCoFH;
import cofh.thermal.dynamics.inventory.container.slot.SlotFalseBuffer;
import cofh.thermal.dynamics.tileentity.logistics.LogisticsItemBufferTile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static cofh.thermal.dynamics.init.TDynReferences.LOGISTICS_ITEM_BUFFER_CONTAINER;

public class LogisticsItemBufferContainer extends TileContainer {

    protected boolean showConfigSlots;

    public final LogisticsItemBufferTile tile;

    public LogisticsItemBufferContainer(int windowId, World world, BlockPos pos, PlayerInventory inventory, PlayerEntity player) {

        super(LOGISTICS_ITEM_BUFFER_CONTAINER, windowId, world, pos, inventory, player);
        this.tile = (LogisticsItemBufferTile) world.getTileEntity(pos);
        InvWrapperCoFH tileInv = new InvWrapperCoFH(this.tile.getItemInv());

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                addSlot(new SlotCoFH(tileInv, j + i * 3, 62 + j * 18, 17 + i * 18).setEnabled(() -> !showConfigSlots));
            }
        }
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                addSlot(new SlotFalseBuffer(tileInv, 9 + j + i * 3, 62 + j * 18, 17 + i * 18).setEnabled(() -> showConfigSlots));
            }
        }
        bindPlayerInventory(inventory);
    }

    public void setShowConfigSlots(boolean showConfigSlots) {

        this.showConfigSlots = showConfigSlots;
    }

    public boolean getShowConfigSlots() {

        return showConfigSlots;
    }

}
