package cofh.thermal.dynamics.init;

import cofh.thermal.dynamics.block.TileBlockEnderTunnel;
import cofh.thermal.dynamics.tileentity.device.EnderTunnelTile;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntityType;

import static cofh.thermal.core.ThermalCore.TILE_ENTITIES;
import static cofh.thermal.core.util.RegistrationHelper.registerBlock;
import static cofh.thermal.dynamics.init.TDynIDs.ID_ENDER_TUNNEL;
import static cofh.thermal.dynamics.init.TDynReferences.ENDER_TUNNEL_BLOCK;

public class TDynBlocks {

    private TDynBlocks() {

    }

    public static void register() {

        registerBlock(ID_ENDER_TUNNEL, () -> new TileBlockEnderTunnel(AbstractBlock.Properties.create(Material.IRON), EnderTunnelTile::new));

        TILE_ENTITIES.register(ID_ENDER_TUNNEL, () -> TileEntityType.Builder.create(EnderTunnelTile::new, ENDER_TUNNEL_BLOCK).build(null));

        registerDucts();
    }

    // region HELPERS
    private static void registerDucts() {

    }
    // endregion
}
