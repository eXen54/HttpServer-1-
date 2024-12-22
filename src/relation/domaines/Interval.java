package relation.domaines;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Interval {
    private final double minVal;
    private final double maxVal;

    /**
     * Creates a new Interval with the specified minimum and maximum values.
     * @param minVal The minimum value of the interval
     * @param maxVal The maximum value of the interval
     * @throws IllegalArgumentException if minVal is greater than maxVal
     */
    public Interval(double minVal, double maxVal) {
        if (minVal > maxVal) {
            throw new IllegalArgumentException("Minimum value cannot be greater than maximum value");
        }
        this.minVal = minVal;
        this.maxVal = maxVal;
    }

    public double getMinVal() {
        return minVal;
    }

    public double getMaxVal() {
        return maxVal;
    }

    /**
     * Checks if a value is within this interval.
     * @param obj The value to check
     * @return true if the value is within the interval, false otherwise
     * @throws NumberFormatException if the object cannot be parsed to a double
     * @throws IllegalArgumentException if obj is null
     */
    public boolean isInInterval(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        double val = Double.parseDouble(obj.toString());
        return val >= this.minVal && val <= this.maxVal;
    }
    public static boolean isInInterval(Interval interval, Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        double val = Double.parseDouble(obj.toString());
        return val >= interval.minVal && val <= interval.maxVal;
    }

    /**
     * Checks if two intervals overlap.
     * @param one First interval
     * @param other Second interval
     * @return true if the intervals overlap, false otherwise
     * @throws IllegalArgumentException if either interval is null
     */
    public static boolean overlaps(Interval one, Interval other) {
        if (one == null || other == null) {
            throw new IllegalArgumentException("Intervals cannot be null");
        }
        // Fixed the logic - original was incorrect
        return !(one.maxVal < other.minVal || other.maxVal < one.minVal);
    }

    /**
     * Checks if two intervals are adjacent.
     * @param one First interval
     * @param other Second interval
     * @return true if the intervals are adjacent, false otherwise
     * @throws IllegalArgumentException if either interval is null
     */
    public static boolean isAdjacent(Interval one, Interval other) {
        if (one == null || other == null) {
            throw new IllegalArgumentException("Intervals cannot be null");
        }
        // Fixed typo in 'onea' and added precision comparison
        return Double.compare(one.maxVal, other.minVal) == 0 ||
               Double.compare(other.maxVal, one.minVal) == 0;
    }

    /**
     * Creates a new interval representing the union of two intervals.
     * @param one First interval
     * @param other Second interval
     * @return A new interval representing the union
     * @throws IllegalArgumentException if the intervals don't overlap and aren't adjacent
     */
    public static Interval union(Interval one, Interval other) {
        if (one == null || other == null) {
            throw new IllegalArgumentException("Intervals cannot be null");
        }
        // Fixed logic - should check if they DO overlap OR are adjacent
        if (!overlaps(one, other) && !isAdjacent(one, other)) {
            throw new IllegalArgumentException("Cannot merge non-overlapping, non-adjacent intervals");
        }
        return new Interval(
                Math.min(one.minVal, other.minVal),
                Math.max(one.maxVal, other.maxVal)
        );
    }

    /**
     * Creates a new interval representing the intersection of two intervals.
     * @param one First interval
     * @param other Second interval
     * @return A new interval representing the intersection, or null if the intervals don't overlap
     * @throws IllegalArgumentException if either interval is null
     */
    public static Interval intersection(Interval one, Interval other) {
        if (one == null || other == null) {
            throw new IllegalArgumentException("Intervals cannot be null");
        }
        // Fixed logic - should check if they DON'T overlap
        if (!overlaps(one, other)) {
            return null;
        }
        return new Interval(
                Math.max(one.minVal, other.minVal),
                Math.min(one.maxVal, other.maxVal)
        );
    }

    /**
     * Returns a string representation of the interval.
     * @return A string in the format "[minVal, maxVal]"
     */
    @Override
    public String toString() {
        return String.format("[%.2f, %.2f]", minVal, maxVal);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Interval other)) return false;
        // Using Double.compare for more precise comparison
        return Double.compare(this.minVal, other.minVal) == 0 &&
               Double.compare(this.maxVal, other.maxVal) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minVal, maxVal);
    }
    /**
     * Merges a list of intervals, combining overlapping and adjacent intervals.
     * @param intervals List of intervals to merge
     * @return List of merged intervals
     */
    public static List<Interval> mergeIntervals(List<Interval> intervals) {
        if (intervals == null || intervals.isEmpty()) {
            return new ArrayList<>();
        }

        // Sort intervals by minVal
        intervals.sort((a, b) -> Double.compare(a.getMinVal(), b.getMinVal()));

        List<Interval> merged = new ArrayList<>();
        Interval current = intervals.get(0);

        for (int i = 1; i < intervals.size(); i++) {
            Interval next = intervals.get(i);
            if (overlaps(current, next) || isAdjacent(current, next)) {
                current = union(current, next);
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);

        return merged;
    }

    /**
     * Intersects two lists of intervals.
     * @param list1 First list of intervals
     * @param list2 Second list of intervals
     * @return List of intersected intervals
     */
    public static List<Interval> intersectIntervalLists(
            List<Interval> list1,
            List<Interval> list2) {
        List<Interval> result = new ArrayList<>();

        for (Interval i1 : list1) {
            for (Interval i2 : list2) {
                Interval intersected = intersection(i1, i2);
                if (intersected != null) {
                    result.add(intersected);
                }
            }
        }

        return mergeIntervals(result);
    }

    /**
     * Computes the difference between two lists of intervals.
     * @param list1 First list of intervals
     * @param list2 Second list of intervals
     * @return List of intervals representing the difference
     */
    public static List<Interval> differenceIntervalLists(
           List<Interval> list1,
            List<Interval> list2) {
        if (list2.isEmpty()) {
            return new ArrayList<>(list1);
        }

        List<Interval> result = new ArrayList<>();

        // Merge all intervals in list2
        List<Interval> mergedList2 = mergeIntervals(list2);

        // For each interval in list1
        for (Interval interval : list1) {
            List<Interval> remainingPieces = new ArrayList<>();
            remainingPieces.add(interval);

            // Subtract each interval from list2
            for (Interval subtract : mergedList2) {
                List<Interval> newPieces = new ArrayList<>();

                for (Interval piece : remainingPieces) {
                    if (overlaps(piece, subtract)) {
                        // Add pieces before and after the subtracted interval
                        if (piece.getMinVal() < subtract.getMinVal()) {
                            newPieces.add(new Interval(piece.getMinVal(), subtract.getMinVal()));
                        }
                        if (piece.getMaxVal() > subtract.getMaxVal()) {
                            newPieces.add(new Interval(subtract.getMaxVal(), piece.getMaxVal()));
                        }
                    } else {
                        newPieces.add(piece);
                    }
                }
                remainingPieces = newPieces;
            }
            result.addAll(remainingPieces);
        }

        return mergeIntervals(result);
    }
}