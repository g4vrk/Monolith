package ac.grim.grimac.utils.math;

import ac.grim.grimac.utils.data.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

@UtilityClass
public class MonolithMath {

    private static final float EPSILON = 1E-4F;

    public double gcd(
            final double a,
            final double b
    ) {
        return gcd(a, b, EPSILON);
    }

    public double gcd(
            final double a,
            final double b,
            final double epsilon
    ) {
        if (b <= epsilon) {
            return a;
        }

        double remainder = a % b;

        if (remainder < 0.0D) {
            remainder += b;
        }

        if (Math.abs(remainder) <= epsilon) {
            remainder = 0.0D;
        }

        return gcd(b, remainder, epsilon);
    }

    public int getMode(
            final @NotNull Collection<? extends Number> array
    ) {

        int mode = (Integer) array.toArray()[0];
        int maxCount = 0;

        for (final Number value : array) {

            int count = 1;

            for (final Number i : array) {

                if (i.equals(value)) {
                    ++count;
                }

                if (count > maxCount) {
                    mode = (Integer) value;

                    maxCount = count;
                }

            }

        }

        return mode;

    }

    public double distXZ(
            final double x1,
            final double x2,
            final double z1,
            final double z2
    ) {
        double dx = x2 - x1;
        double dz = z2 - z1;
        return Math.sqrt(dx * dx + dz * dz);
    }

    private double getMedian(
            final @NotNull List<Double> data
    ) {
        if (data.size() % 2 == 0) {
            return (data.get(data.size() / 2) + data.get(data.size() / 2 - 1)) / 2.0;
        }

        return data.get(data.size() / 2);
    }

    public @NotNull Pair<List<Double>, List<Double>> getOutliers(
            final @NotNull Collection<? extends Number> collection
    ) {

        final List<Double> values = new ObjectArrayList<>();

        for (final Number number : collection) {
            values.add(number.doubleValue());
        }

        final double q1 = getMedian(values.subList(0, values.size() / 2));
        final double q3 = getMedian(values.subList(values.size() / 2, values.size()));

        final double iqr = Math.abs(q1 - q3);

        final double lowThreshold = q1 - 1.5 * iqr, highThreshold = q3 + 1.5 * iqr;

        final Pair<List<Double>, List<Double>> tuple = new Pair<>(new ObjectArrayList<>(), new ObjectArrayList<>());

        for (final Double value : values) {
            if (value < lowThreshold) {
                tuple.first().add(value);
            } else if (value > highThreshold) {
                tuple.second().add(value);
            }
        }

        return tuple;
    }

    public int getDistinct(
            final @NotNull Collection<? extends Number> data
    ) {

        long count = 0L;

        final Set<Number> uniqueValues = new ObjectOpenHashSet<>();
        for (final Number datum : data) {
            if (uniqueValues.add(datum)) {
                count++;
            }
        }

        return (int) count;
    }

    public @NotNull List<Float> getJiffDelta(
            final @NotNull List<? extends Number> data,
            final int depth
    ) {
        List<Float> result = new ObjectArrayList<>();

        for (final Number n : data) {
            result.add(n.floatValue());
        }

        for(int i = 0; i < depth; ++i) {
            List<Float> calculate = new ObjectArrayList<>();
            float old = Float.MIN_VALUE;

            for (float n : result) {
                if (old == Float.MIN_VALUE) {
                    old = n;
                } else {
                    calculate.add(Math.abs(Math.abs(n) - Math.abs(old)));
                    old = n;
                }
            }
            result = new ObjectArrayList<>(calculate);
        }
        return result;
    }

    public double ksgoTest(
            final @NotNull List<? extends Number> data,
            final @NotNull Function<Double, Double> cdfFunction
    ) {
        final List<Double> sorted = new ObjectArrayList<>();

        for (final Number datum : data) {
            Double doubleValue = datum.doubleValue();
            sorted.add(doubleValue);
        }

        sorted.sort(null);

        int n = sorted.size();

        double dStatistic = 0.0;

        for(int i = 0; i < n; ++i) {
            double empiricalCDF = (double)(i + 1) / (double)n;
            double theoreticalCDF = cdfFunction.apply(sorted.get(i));
            dStatistic = Math.max(dStatistic, Math.abs(empiricalCDF - theoreticalCDF));
        }
        return dStatistic;
    }

    public double getAverage(
            final @NotNull Collection<? extends Number> data,
            final boolean trues
    ) {
        double sum = 0.0;

        Number number;

        for(Iterator<? extends Number> var3 = data.iterator(); var3.hasNext(); sum += number.doubleValue()) {
            number = var3.next();
        }

        double result = sum / (double)data.size();

        return Double.isNaN(result) ? 0.0 : result;
    }

    public double getAverage(
            final @Nullable Collection<? extends Number> data
    ) {

        if (data == null || data.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;

        for (final Number number : data) {
            sum += number.doubleValue();
        }

        return sum / data.size();
    }

    public double getVariance(
            final @NotNull Collection<? extends Number> data
    ) {
        int count = 0;
        double sum = 0.0;
        double variance = 0.0;

        for (final Number number : data) {
            sum += number.doubleValue();
            ++count;
        }

        final double average = sum / count;

        for (final Number number : data) {
            variance += Math.pow(number.doubleValue() - average, 2.0);
        }

        return variance;
    }

    public double getStandardDeviation(
            final @NotNull Collection<? extends Number> data
    ) {
        final double variance = getVariance(data);

        return Math.sqrt(variance);
    }

    public boolean equals(
            final double v1,
            final double v2,
            final float epsilon
    ) {
        return Math.abs(v1 - v2) < epsilon;
    }

    public boolean equals(
            final double v1,
            final double v2
    ) {
        return equals(v1, v2, EPSILON);
    }

}
