package cofh.thermal.dynamics.client.gui.device;

import cofh.lib.util.helpers.StringHelper;
import cofh.thermal.dynamics.inventory.container.device.DeviceItemBufferContainer;
import cofh.thermal.dynamics.tileentity.device.DeviceItemBufferTile;
import cofh.thermal.lib.client.gui.ThermalTileScreenBase;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import static cofh.core.util.helpers.GuiHelper.generatePanelInfo;
import static cofh.lib.util.constants.Constants.ID_THERMAL;

public class DeviceItemBufferScreen extends ThermalTileScreenBase<DeviceItemBufferContainer> {

    public static final String TEX_PATH = ID_THERMAL + ":textures/gui/devices/item_buffer.png";
    public static final ResourceLocation TEXTURE = new ResourceLocation(TEX_PATH);

    protected DeviceItemBufferTile tile;

    public DeviceItemBufferScreen(DeviceItemBufferContainer container, PlayerInventory inv, ITextComponent titleIn) {

        super(container, inv, container.tile, StringHelper.getTextComponent("block.thermal.device_item_buffer"));
        tile = container.tile;
        texture = TEXTURE;
        info = generatePanelInfo("info.thermal.device_item_buffer");
    }

    @Override
    public void init() {

        super.init();

    }

}