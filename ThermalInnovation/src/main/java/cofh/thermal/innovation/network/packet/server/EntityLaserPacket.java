package cofh.thermal.innovation.network.packet.server;

import cofh.core.CoFHCore;
import cofh.lib.network.packet.IPacketServer;
import cofh.lib.network.packet.PacketBase;
import cofh.lib.util.constants.Constants;
import cofh.thermal.innovation.item.RFLaserItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPartEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EntityDamageSource;

public class EntityLaserPacket extends PacketBase implements IPacketServer {
    protected int entityId;
    protected float distance;

    public EntityLaserPacket() {

        super(18, CoFHCore.PACKET_HANDLER); //TODO: real id
    }

    @Override
    public void handleServer(ServerPlayerEntity player) {

        Entity target = player.world.getEntityByID(entityId);
        ItemStack stack = player.getHeldItem(player.getActiveHand());
        Item item = stack.getItem();

        if (target != null && item instanceof RFLaserItem) {
            float damage = ((RFLaserItem) item).getRangedAttackDamage(stack, player.getDistance(target));
            if (damage <= 0.001F) {
                return;
            }

            if (target instanceof EnderDragonPartEntity) {
                target = ((EnderDragonPartEntity) target).getParent();
            }
            if (target instanceof EnderDragonEntity) {
                damage = damage * 0.5F;
            }

            target.hurtResistantTime = 0;
            if (target instanceof LivingEntity) {
                ((LivingEntity) target).getAttribute(Attributes.KNOCKBACK_RESISTANCE).applyNonPersistentModifier(new AttributeModifier(Constants.UUID_ENCH_BULWARK_KNOCKBACK_RESISTANCE, "Laser Damage", 1.0D, AttributeModifier.Operation.ADDITION));
            }

            target.attackEntityFrom(new EntityDamageSource("laser", player) , damage);

            if (target instanceof LivingEntity) {
                ((LivingEntity) target).getAttribute(Attributes.KNOCKBACK_RESISTANCE).removeModifier(Constants.UUID_ENCH_BULWARK_KNOCKBACK_RESISTANCE);
            }
            target.hurtResistantTime = 10;
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

    public static void sendToServer(int entityId, float distance) {

        EntityLaserPacket packet = new EntityLaserPacket();
        packet.entityId = entityId;
        packet.distance = distance;
        packet.sendToServer();
    }

}
