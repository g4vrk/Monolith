package ac.grim.grimac.utils.data;

import ac.grim.grimac.utils.anticheat.ElapsedTimer;
import ac.grim.grimac.utils.math.MonolithMath;
import lombok.Getter;

import java.util.ArrayDeque;

@Getter
public final class HeuristicData {

    private static final int SENSITIVITY_SAMPLE_SIZE = 40;

    private static final int MIN_VALID_SENSITIVITY = 0;
    private static final int MAX_VALID_SENSITIVITY = 200;
    private static final int MAX_NORMALIZED_SENSITIVITY = 269;
    private static final int LOW_SENSITIVITY_THRESHOLD = 50;

    private static final long CINEMATIC_TIMEOUT_MS = 12L;

    private static final float MIN_PITCH_DELTA = 0.1F;
    private static final float MAX_PITCH_DELTA = 25.0F;

    private static final float DELTA_EPSILON = 1E-4F;

    /*
     * Minecraft mouse sensitivity formula constants
     */
    private static final float MCP_SENSITIVITY_BASE = 0.6F;
    private static final float MCP_SENSITIVITY_OFFSET = 0.2F;
    private static final float MCP_SENSITIVITY_SCALE = 1.2F;


    /*
     * Sensitivity reverse calculation constants
     */
    private static final double SENSITIVITY_GCD_MULTIPLIER = 0.8333;
    private static final double SENSITIVITY_STEP_MULTIPLIER = 1.666;
    private static final double SENSITIVITY_STEP_OFFSET = 0.3333;
    private static final double SENSITIVITY_NORMALIZATION = 200.0;


    /*
     * Cinematic camera detection constants
     */
    private static final float CINEMATIC_MAX_MOUSE_DELTA = 4.0F;
    private static final float CINEMATIC_MIN_MOUSE_DELTA = 0.0F;

    private static final float CINEMATIC_PITCH_MULTIPLIER = 1.073742F;
    private static final float CINEMATIC_PITCH_OFFSET = 0.15F;
    private static final float CINEMATIC_MAX_PITCH_DIFFERENCE = 0.1F;


    /*
     * Smoothness calculation constants
     */
    private static final float YAW_SMOOTHNESS_BASE = 10.0F;
    private static final float PITCH_SMOOTHNESS_BASE = 5.0F;


    private float yaw;
    private float pitch;

    private float lastYaw;
    private float lastPitch;


    private float deltaYaw;
    private float deltaPitch;

    private float lastDeltaYaw;
    private float lastDeltaPitch;


    private float yawAcceleration;
    private float pitchAcceleration;

    private float lastYawAcceleration;
    private float lastPitchAcceleration;


    private float yawSmoothness;
    private float pitchSmoothness;


    private double estimatedSensitivity = -1;
    private double mcpSensitivity = -1;

    private int sensitivity = -1;

    private int mouseDeltaY;

    private final ArrayDeque<Integer> sensitivitySamples = new ArrayDeque<>();

    private final ElapsedTimer cinematicTimer = new ElapsedTimer();


    public void onRotationTick(
            final float yaw,
            final float pitch
    ) {

        this.updateRotation(yaw, pitch);
        this.updateDelta();
        this.updateAcceleration();
        this.calculateMouseDelta();
        this.calculateSmoothness();
        this.detectCinematic();
        this.calculateSensitivity();

    }


    private void updateRotation(
            final float yaw,
            final float pitch
    ) {

        lastYaw = this.yaw;
        lastPitch = this.pitch;

        this.yaw = yaw;
        this.pitch = pitch;

    }


    private void updateDelta(
    ) {

        lastDeltaYaw = deltaYaw;
        lastDeltaPitch = deltaPitch;

        deltaYaw = Math.abs(yaw - lastYaw);
        deltaPitch = Math.abs(pitch - lastPitch);

    }


    private void updateAcceleration(
    ) {

        lastYawAcceleration = yawAcceleration;
        lastPitchAcceleration = pitchAcceleration;

        yawAcceleration = Math.abs(deltaYaw - lastDeltaYaw);
        pitchAcceleration = Math.abs(deltaPitch - lastDeltaPitch);

    }


    private void calculateMouseDelta(
    ) {

        if (mcpSensitivity <= 0) {
            mouseDeltaY = 0;
            return;
        }

        final float sensitivity =
                (float) mcpSensitivity *
                        MCP_SENSITIVITY_BASE +
                        MCP_SENSITIVITY_OFFSET;

        final float gcd =
                sensitivity *
                        sensitivity *
                        sensitivity *
                        MCP_SENSITIVITY_SCALE;

        if (gcd <= 0) {
            mouseDeltaY = 0;
            return;
        }

        mouseDeltaY = (int) (deltaPitch / gcd);

    }


    private void calculateSmoothness(
    ) {

        yawSmoothness = Math.max(
                -10.0f,
                YAW_SMOOTHNESS_BASE -
                        Math.abs(
                                yawAcceleration - lastYawAcceleration
                        )
        );

        pitchSmoothness = Math.max(
                -10.0f,
                PITCH_SMOOTHNESS_BASE -
                        Math.abs(
                                pitchAcceleration - lastPitchAcceleration
                        )
        );

    }


    private void detectCinematic() {

        final float expectedPitch =
                deltaPitch *
                        CINEMATIC_PITCH_MULTIPLIER -
                        (deltaPitch - CINEMATIC_PITCH_OFFSET);

        final float difference =
                Math.abs(deltaPitch - expectedPitch);

        if (isCinematicRotation(difference)) cinematicTimer.reset();

    }


    private boolean isCinematicRotation(
            final float difference
    ) {

        return estimatedSensitivity < 0
                && mouseDeltaY > CINEMATIC_MIN_MOUSE_DELTA
                && mouseDeltaY < CINEMATIC_MAX_MOUSE_DELTA
                && difference <= CINEMATIC_MAX_PITCH_DIFFERENCE;

    }


    private void calculateSensitivity() {

        if (deltaPitch <= MIN_PITCH_DELTA
                || deltaPitch >= MAX_PITCH_DELTA) return;

        final double gcd =
                MonolithMath.getGcd(
                        deltaPitch,
                        lastDeltaPitch
                );

        if (gcd <= 0) return;

        estimatedSensitivity =
                calculateSensitivity(gcd);

        if (estimatedSensitivity <= 0
                || estimatedSensitivity >= MAX_NORMALIZED_SENSITIVITY) return;

        sensitivitySamples.add(
                (int) estimatedSensitivity
        );

        if (sensitivitySamples.size() < SENSITIVITY_SAMPLE_SIZE) return;

        sensitivity =
                MonolithMath.getMode(
                        sensitivitySamples
                );

        this.updateMcpSensitivity();

        sensitivitySamples.clear();

    }


    private double calculateSensitivity(
            final double gcd
    ) {

        final double modifier =
                Math.cbrt(
                        SENSITIVITY_GCD_MULTIPLIER * gcd
                );

        return (
                SENSITIVITY_STEP_MULTIPLIER * modifier
                        - SENSITIVITY_STEP_OFFSET
        ) * SENSITIVITY_NORMALIZATION;

    }


    private void updateMcpSensitivity() {

        if (!validSensitivity()) return;

        mcpSensitivity =
                SensValues.SENSITIVITY_MCP_VALUES
                        .getOrDefault(
                                sensitivity,
                                -1D
                        );

    }


    public boolean validSensitivity() {

        return sensitivity > MIN_VALID_SENSITIVITY
                && sensitivity < MAX_VALID_SENSITIVITY;

    }


    public boolean validSensitivityNormalized() {

        return sensitivity > MIN_VALID_SENSITIVITY
                && sensitivity < MAX_NORMALIZED_SENSITIVITY;

    }


    public boolean cinematicCamera() {

        return cinematicTimer.passedTimeMillis()
                < CINEMATIC_TIMEOUT_MS;

    }


    public boolean tooLowSensitivity() {

        return sensitivity >= MIN_VALID_SENSITIVITY
                && sensitivity < LOW_SENSITIVITY_THRESHOLD;

    }


    public boolean zeroDeltas() {

        return Math.abs(deltaYaw) < DELTA_EPSILON
                && Math.abs(deltaPitch) < DELTA_EPSILON;

    }
}
