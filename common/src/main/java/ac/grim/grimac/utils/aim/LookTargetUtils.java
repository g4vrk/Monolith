package ac.grim.grimac.utils.aim;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.grim.grimac.utils.data.Pair;
import ac.grim.grimac.utils.data.packetentity.PacketEntity;
import ac.grim.grimac.utils.nmsutil.ReachUtils;
import ac.grim.grimac.utils.nmsutil.Ray;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.util.Vector3d;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class LookTargetUtils {

    private static final double DEFAULT_MAX_DISTANCE = 6.0D;

    private static final double DEFAULT_MAX_DEGREES = 15.0D;

    private static final double BOX_EXPAND = 0.1D;


    public boolean isLookingAtEntity(
            final @NotNull GrimPlayer player
    ) {

        return isLookingAtEntity(player, DEFAULT_MAX_DISTANCE);

    }


    public boolean isLookingAtEntity(
            final @NotNull GrimPlayer player,
            final double maxDistance
    ) {

        return getLookedAtEntity(player, maxDistance) != null;

    }


    public @Nullable PacketEntity getLookedAtEntity(
            final @NotNull GrimPlayer player,
            final double maxDistance
    ) {

        final Vector3d eyePos =
                new Vector3d(
                        player.x,
                        player.y + player.getEyeHeight(),
                        player.z
                );

        final Ray ray =
                new Ray(
                        player,
                        eyePos.getX(),
                        eyePos.getY(),
                        eyePos.getZ(),
                        player.yaw,
                        player.pitch
                );

        final Vector3d endPos =
                ray.getPointAtDistance(maxDistance);

        PacketEntity closest = null;

        double closestDistanceSquared = Double.MAX_VALUE;

        for (final Int2ObjectMap.Entry<PacketEntity> entry
                : player.compensatedEntities.entityMap.int2ObjectEntrySet()) {

            final PacketEntity entity = entry.getValue();

            if (entity == null
                    || entity.isDead) continue;

            final SimpleCollisionBox box =
                    entity.getPossibleCollisionBoxes()
                            .expand(BOX_EXPAND);

            final Pair<Vector3d, BlockFace> intercept =
                    ReachUtils.calculateIntercept(
                            box,
                            eyePos,
                            endPos
                    );

            if (intercept.first() == null) continue;

            final double distanceSquared =
                    eyePos.distanceSquared(intercept.first());

            if (distanceSquared < closestDistanceSquared) {
                closestDistanceSquared = distanceSquared;
                closest = entity;
            }

        }

        return closest;

    }


    public boolean hasEntityNearLook(
            final @NotNull GrimPlayer player
    ) {

        return hasEntityNearLook(player, DEFAULT_MAX_DEGREES, DEFAULT_MAX_DISTANCE);

    }


    public boolean hasEntityNearLook(
            final @NotNull GrimPlayer player,
            final double maxDegrees,
            final double maxDistance
    ) {

        return getClosestAngleToLook(player, maxDistance) <= maxDegrees;

    }


    public double getClosestAngleToLook(
            final @NotNull GrimPlayer player,
            final double maxDistance
    ) {

        final Vector3d eyePos =
                new Vector3d(
                        player.x,
                        player.y + player.getEyeHeight(),
                        player.z
                );

        final Vector3d lookDirection =
                Ray.calculateDirection(
                        player,
                        player.yaw,
                        player.pitch
                );

        double closestAngle = Double.MAX_VALUE;

        for (final Int2ObjectMap.Entry<PacketEntity> entry
                : player.compensatedEntities.entityMap.int2ObjectEntrySet()) {

            final PacketEntity entity = entry.getValue();

            if (entity == null
                    || entity.isDead) continue;

            final SimpleCollisionBox box =
                    entity.getPossibleCollisionBoxes();

            final Vector3d boxCenter =
                    new Vector3d(
                            (box.minX + box.maxX) / 2.0D,
                            (box.minY + box.maxY) / 2.0D,
                            (box.minZ + box.maxZ) / 2.0D
                    );

            final Vector3d toEntity =
                    boxCenter.subtract(eyePos);

            final double distance =
                    Math.sqrt(
                            toEntity.dot(toEntity)
                    );

            if (distance <= 1E-4D
                    || distance > maxDistance) continue;

            final double cosAngle =
                    lookDirection.dot(toEntity) / distance;

            final double angleDegrees =
                    Math.toDegrees(
                            Math.acos(
                                    Math.min(1.0D, Math.max(-1.0D, cosAngle))
                            )
                    );

            if (angleDegrees < closestAngle) {
                closestAngle = angleDegrees;
            }

        }

        return closestAngle;

    }

}
