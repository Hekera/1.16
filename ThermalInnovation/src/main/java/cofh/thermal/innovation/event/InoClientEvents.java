package cofh.thermal.innovation.event;

import cofh.thermal.innovation.client.renderer.tool.LaserRenderer;
import cofh.thermal.innovation.item.RFLaserItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

import static cofh.lib.util.constants.Constants.ID_COFH_CORE;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ID_COFH_CORE)
public class InoClientEvents {
    private InoClientEvents() {

    }

    @SubscribeEvent
    static void renderWorldLastEvent(RenderWorldLastEvent event) {
        List<AbstractClientPlayerEntity> otherPlayers = Minecraft.getInstance().world.getPlayers();
        ClientPlayerEntity player = Minecraft.getInstance().player;

        for (PlayerEntity otherPlayer : otherPlayers) {
            if (player.getDistanceSq(otherPlayer) > 1024)
                continue;

            Hand hand = otherPlayer.getActiveHand();
            if (otherPlayer.isHandActive() && otherPlayer.getActiveItemStack().getItem() instanceof RFLaserItem) {
                LaserRenderer.renderLaserHelper(event, player, otherPlayer, hand);
            }
        }
    }
}
