package cofh.thermal.dynamics.client.gui.logistics;

import cofh.core.client.gui.ContainerScreenCoFH;
import cofh.core.client.gui.element.ElementButton;
import cofh.core.client.gui.element.SimpleTooltip;
import cofh.lib.util.helpers.StringHelper;
import cofh.thermal.dynamics.inventory.container.logistics.LogisticsItemBufferContainer;
import cofh.thermal.dynamics.tileentity.logistics.LogisticsItemBufferTile;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import static cofh.core.util.helpers.GuiHelper.generatePanelInfo;
import static cofh.lib.util.constants.Constants.ID_COFH_CORE;
import static cofh.lib.util.constants.Constants.ID_THERMAL;
import static cofh.lib.util.helpers.SoundHelper.playClickSound;

public class LogisticsItemBufferScreen extends ContainerScreenCoFH<LogisticsItemBufferContainer> {

    public static final String TEX_PATH = ID_THERMAL + ":textures/gui/logistics/item_buffer.png";
    public static final ResourceLocation TEXTURE = new ResourceLocation(TEX_PATH);

    public static final String TEX_SHOW_INV_SLOTS = ID_COFH_CORE + ":textures/gui/filters/filter_deny_list.png";
    public static final String TEX_SHOW_CONFIG_SLOTS = ID_COFH_CORE + ":textures/gui/filters/filter_allow_list.png";

    public static final String TEX_IGNORE_NBT = ID_COFH_CORE + ":textures/gui/filters/filter_ignore_nbt.png";
    public static final String TEX_USE_NBT = ID_COFH_CORE + ":textures/gui/filters/filter_use_nbt.png";

    protected LogisticsItemBufferTile tile;

    public LogisticsItemBufferScreen(LogisticsItemBufferContainer container, PlayerInventory inv, ITextComponent titleIn) {

        super(container, inv, StringHelper.getTextComponent("block.thermal.logistics_item_buffer"));
        tile = container.tile;
        texture = TEXTURE;
        info = generatePanelInfo("info.thermal.logistics_item_buffer");
    }

    @Override
    public void init() {

        super.init();

        addButtons();
    }

    // region ELEMENTS
    protected void addButtons() {

        addElement(new ElementButton(this, 33, 21) {

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {

                container.setShowConfigSlots(true);
                playClickSound(0.7F);
                return true;
            }
        }
                .setSize(20, 20)
                .setTexture(TEX_SHOW_INV_SLOTS, 40, 20)
                .setTooltipFactory(new SimpleTooltip(new TranslationTextComponent("<Show Configuration Slots>")))
                .setVisible(() -> !container.getShowConfigSlots()));

        addElement(new ElementButton(this, 33, 21) {

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {

                container.setShowConfigSlots(false);
                playClickSound(0.4F);
                return true;
            }
        }
                .setSize(20, 20)
                .setTexture(TEX_SHOW_CONFIG_SLOTS, 40, 20)
                .setTooltipFactory(new SimpleTooltip(new TranslationTextComponent("<Show Inventory Slots>")))
                .setVisible(() -> container.getShowConfigSlots()));

        //        addElement(new ElementButton(this, 132, 44) {
        //
        //            @Override
        //            public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        //
        //                container.setCheckNBT(true);
        //                playClickSound(0.7F);
        //                return true;
        //            }
        //        }
        //                .setSize(20, 20)
        //                .setTexture(TEX_IGNORE_NBT, 40, 20)
        //                .setTooltipFactory(new SimpleTooltip(new TranslationTextComponent("info.cofh.filter.checkNBT.0")))
        //                .setVisible(() -> !container.getCheckNBT()));
        //
        //        addElement(new ElementButton(this, 132, 44) {
        //
        //            @Override
        //            public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        //
        //                container.setCheckNBT(false);
        //                playClickSound(0.4F);
        //                return true;
        //            }
        //        }
        //                .setSize(20, 20)
        //                .setTexture(TEX_USE_NBT, 40, 20)
        //                .setTooltipFactory(new SimpleTooltip(new TranslationTextComponent("info.cofh.filter.checkNBT.1")))
        //                .setVisible(() -> container.getCheckNBT()));
    }
    // endregion
}