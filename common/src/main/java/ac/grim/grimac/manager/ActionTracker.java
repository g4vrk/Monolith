package ac.grim.grimac.manager;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.data.packetentity.PacketEntity;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import org.jetbrains.annotations.NotNull;

public class ActionTracker extends Check implements PacketCheck {

    public ActionTracker(@NotNull GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {

        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {

            final WrapperPlayClientInteractEntity action =
                    new WrapperPlayClientInteractEntity(event);

            if (action.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {

                final PacketEntity entity = player.compensatedEntities.getEntity(action.getEntityId());

                if (entity != null && entity.getType() == EntityTypes.PLAYER) {
                    player.actionState.onAttack();
                }
            }

        } else if (isTickPacketIncludingNonMovement(event.getPacketType())) {

            player.actionState.flyingPacketsSinceAttack++;

        } if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {

            final WrapperPlayClientPlayerBlockPlacement playClientPlayerBlockPlacement =
                    new WrapperPlayClientPlayerBlockPlacement(event);

            if (playClientPlayerBlockPlacement.getFace() != BlockFace.OTHER) {
                player.actionState.onBlockPlace();
            }

        }

        if (isTickPacket(event.getPacketType())) {
            player.actionState.onTick();
        }
    }
}
