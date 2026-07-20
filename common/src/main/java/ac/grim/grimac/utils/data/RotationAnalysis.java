package ac.grim.grimac.utils.data;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

// Copied directly from https://github.com/KlovyEuV2new/SnowGrim

@Getter
public final class RotationAnalysis {

    private static final float WHOLE_ROTATION_EPSILON = 1E-4F;

    private final int sampleSize;

    private final float lowThreshold;
    private final float highThreshold;

    private final Set<Float> distinctRotations =
            new ObjectOpenHashSet<>();

    private int remainingSamples;

    private float average;
    private float min;
    private float max;

    private int highCount;
    private int lowCount;
    private int roundedCount;


    public RotationAnalysis(
            final int sampleSize,
            final float lowThreshold,
            final float highThreshold
    ) {

        this.sampleSize = sampleSize;
        this.lowThreshold = lowThreshold;
        this.highThreshold = highThreshold;

        this.reset();

    }


    public void process(
            final float rotation
    ) {

        if (complete()) {
            return;
        }

        average += rotation;

        distinctRotations.add(rotation);

        if (rotation > highThreshold) {
            highCount++;
        }

        if (rotation < lowThreshold) {
            lowCount++;
        }

        min = Math.min(
                min,
                rotation
        );

        max = Math.max(
                max,
                rotation
        );

        if (isRoundedRotation(rotation)) {
            roundedCount++;
        }

        remainingSamples--;

        if (complete()) {
            average /= sampleSize;
        }

    }


    public void reset() {

        remainingSamples = sampleSize;

        average = 0F;

        highCount = 0;
        lowCount = 0;
        roundedCount = 0;

        min = Float.POSITIVE_INFINITY;
        max = Float.NEGATIVE_INFINITY;

        distinctRotations.clear();

    }


    public HeuristicsResult getResult() {

        if (!complete()) {
            return null;
        }

        return new HeuristicsResult(
                average,
                min,
                max,
                sampleSize - distinctRotations.size(),
                highCount,
                lowCount,
                roundedCount
        );

    }


    private boolean isRoundedRotation(
            final float rotation
    ) {

        return rotation > 1F
                && rotation % 1.5F != 0F
                && (Math.round(rotation) == 0
                || isWholeNumber(rotation));

    }


    private boolean isWholeNumber(
            final float value
    ) {

        return Math.abs(
                value - Math.round(value)
        ) < WHOLE_ROTATION_EPSILON;

    }


    private boolean complete() {

        return remainingSamples == 0;

    }


    @Getter
    @RequiredArgsConstructor
    public static final class HeuristicsResult {

        private final float average;
        private final float min;
        private final float max;

        private final int duplicates;

        private final int highCount;
        private final int lowCount;
        private final int roundedCount;

    }

}
