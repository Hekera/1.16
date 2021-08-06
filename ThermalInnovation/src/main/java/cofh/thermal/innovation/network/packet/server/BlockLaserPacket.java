package cofh.thermal.innovation.network.packet.server;

import cofh.core.CoFHCore;
import cofh.lib.network.packet.IPacketServer;
import cofh.lib.network.packet.PacketBase;
import cofh.thermal.innovation.item.RFLaserItem;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import static cofh.lib.util.constants.Constants.PACKET_LASER_BLOCK;

public class BlockLaserPacket extends PacketBase implements IPacketServer {

    protected BlockPos pos;
    protected Direction face;
    protected float distance;

    public BlockLaserPacket() {

        super(PACKET_LASER_BLOCK, CoFHCore.PACKET_HANDLER);
    }

    @Override
    public void handleServer(ServerPlayerEntity player) {

        ItemStack stack = player.getHeldItem(player.getActiveHand());
        Item item = stack.getItem();

        if (item instanceof RFLaserItem && pos != null) {
            ((RFLaserItem) item).useRanged(stack, player, pos, face, distance);
        }
    }

    @Override
    public void write(PacketBuffer buf) {

        buf.writeBlockPos(pos);
        buf.writeInt(face.getIndex());
        buf.writeFloat(distance);
    }

    @Override
    public void read(PacketBuffer buf) {

        pos = buf.readBlockPos();
        face = Direction.byIndex(buf.readInt());
        distance = buf.readFloat();
    }

    public static void sendToServer(BlockPos blockPos, Direction face, float distance) {

        BlockLaserPacket packet = new BlockLaserPacket();
        packet.pos = blockPos;
        packet.face = face;
        packet.distance = distance;
        packet.sendToServer();
    }
}
