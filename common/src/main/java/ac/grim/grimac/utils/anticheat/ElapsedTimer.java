package ac.grim.grimac.utils.anticheat;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import static java.lang.System.currentTimeMillis;

@NoArgsConstructor
public class ElapsedTimer {

    private long lastTime = currentTimeMillis();

    public double passedTimeMillis() {

        return currentTimeMillis() - this.lastTime;

    }

    public void reset() {
        lastTime = currentTimeMillis();
    }

    public static boolean isTransaction(
            final @NotNull PacketTypeCommon packetType
    ) {
        return packetType == PacketType.Play.Client.PONG || packetType == PacketType.Play.Client.WINDOW_CONFIRMATION;
    }

}
