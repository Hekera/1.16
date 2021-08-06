package cofh.thermal.innovation.network.packet.server;

import cofh.core.CoFHCore;
import cofh.lib.network.packet.IPacketServer;
import cofh.lib.network.packet.PacketBase;
import cofh.thermal.innovation.item.RFLaserItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import static cofh.lib.util.constants.Constants.PACKET_LASER_ENTITY;

public class EntityLaserPacket extends PacketBase implements IPacketServer {

    protected int entityId;
    protected float distance;

    public EntityLaserPacket() {

        super(PACKET_LASER_ENTITY, CoFHCore.PACKET_HANDLER);
    }

    @Override
    public void handleServer(ServerPlayerEntity player) {

        ItemStack stack = player.getHeldItem(player.getActiveHand());
        Item item = stack.getItem();
        Entity target = player.world.getEntityByID(entityId);

        if (item instanceof RFLaserItem && target != null) {
            ((RFLaserItem) item).attackRanged(stack, player, target, distance);
        }
    }

    @Override
    public void write(PacketBuffer buf) {

        buf.writeInt(entityId);
        buf.writeFloat(distance);
    }

    @Override
    public void read(PacketBuffer buf) {

        entityId = buf.readInt();
        distance = buf.readFloat();
    }

    public static void sendToServer(Entity entity, float distance) {

        EntityLaserPacket packet = new EntityLaserPacket();
        packet.entityId = entity.getEntityId();
        packet.distance = distance;
        packet.sendToServer();
    }
}