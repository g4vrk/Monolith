package ac.grim.grimac.utils.data;

import static java.lang.System.currentTimeMillis;

public final class ActionState {

    private static final int NEVER = Integer.MAX_VALUE / 2;

    public int flyingPacketsSinceAttack = NEVER;

    public int ticksSinceAttack = NEVER;

    public int ticksSinceBlockPlace = NEVER;

    private long lastAttack = 0;

    public void onAttack() {

        this.flyingPacketsSinceAttack = 0;
        this.ticksSinceAttack = 0;
        this.lastAttack = currentTimeMillis();

    }

    public void onBlockPlace() {

        this.ticksSinceBlockPlace = 0;

    }

    public void onTick() {

        ++this.flyingPacketsSinceAttack;
        ++this.ticksSinceAttack;
        ++this.ticksSinceBlockPlace;

    }

    public long passedMsAfterAttack() {
        return currentTimeMillis() - this.lastAttack;
    }
}
