package ru.anmo.waterpool_solver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;

@Component
public class WaterPoolSolver implements IWaterPoolSolver{

    @Autowired
    private ExecutorService executor;

    private final Integer mParallelTaskCount;
    private final int MIN_LENGTH_PER_TASK = 5;

    public WaterPoolSolver(@Qualifier("parallel") Integer parallelTaskCount) {
        mParallelTaskCount = parallelTaskCount;
    }

    @Override
    public long calculateWaterAmount(int[] landscape) {
        // Landscapes with 0,1,2 lengths are always dry
        if (landscape.length < 3) {
            return 0;
        }

        return calculateWater(landscape, 0, landscape.length, Border.EMPTY, Border.EMPTY);
    }



    private static long calculateWater(int[] landscapeSection,
                                       int startIndexInclusive,
                                       int endIndexExclusive,
                                       final Border leftBorder,
                                       final Border rightBorder) {

        int currentSectionLength = endIndexExclusive - startIndexInclusive;
        if (currentSectionLength <= 0) {
            return 0;
        }
        if (currentSectionLength == 1) {
            if (leftBorder == Border.WALL && rightBorder == Border.WALL) {
                return calculateWaterSingleSectionBetweenWalls(landscapeSection, startIndexInclusive, endIndexExclusive);
            }
            return calculateWaterSingleSectionWhenAnySideIsEmpty();
        }

        MaxHeightEntry maxPeaks = findMaxHeight(landscapeSection, startIndexInclusive, endIndexExclusive);
        int mostLeftPeak  = maxPeaks.getPositions().first();
        int mostRightPeak = maxPeaks.getPositions().last();

        if (rightBorder == Border.WALL) {
            long waterAmountFromLeftPeakToEnd = calculateWaterBetweenPositions(landscapeSection, mostLeftPeak + 1, endIndexExclusive, maxPeaks.getHeight());
            return waterAmountFromLeftPeakToEnd
                    + calculateWaterFromStartToLeftPeak(landscapeSection, startIndexInclusive, mostLeftPeak, leftBorder);
        }
        if (leftBorder == Border.WALL) {
            long waterAmountFromStartToRightPeak = calculateWaterBetweenPositions(landscapeSection, startIndexInclusive, mostRightPeak, maxPeaks.getHeight());
            return waterAmountFromStartToRightPeak
                    + calculateWaterFromRightPeakToEnd(landscapeSection, mostRightPeak + 1, endIndexExclusive, rightBorder);
        }

        long waterAmountBetweenLeftAndRightPeaks = calculateWaterBetweenPositions(landscapeSection, mostLeftPeak + 1, mostRightPeak, maxPeaks.getHeight());
        return waterAmountBetweenLeftAndRightPeaks
                + calculateWaterFromStartToLeftPeak(landscapeSection, startIndexInclusive, mostLeftPeak, leftBorder)
                + calculateWaterFromRightPeakToEnd(landscapeSection, mostRightPeak + 1, endIndexExclusive, rightBorder);
    }


    private static long calculateWaterFromStartToLeftPeak(int[] landscapeSection, int startIndexInclusive, int stopIndexExclusive, Border leftBorder) {
        return calculateWater(landscapeSection, startIndexInclusive, stopIndexExclusive, leftBorder, Border.WALL);
    }

    private static long calculateWaterFromRightPeakToEnd(int[] landscapeSection, int startIndexInclusive, int stopIndexExclusive, Border rightBorder) {
        return calculateWater(landscapeSection, startIndexInclusive, stopIndexExclusive, Border.WALL, rightBorder);
    }



    private static MaxHeightEntry findMaxHeight(int[] landscapeSection, int startIndexInclusive, int stopIndexExclusive) {
        // TODO: parallel
        int maxHeight = -1;
        SortedSet<Integer> maxPositions = new TreeSet<>();

        for (int i = startIndexInclusive; i < stopIndexExclusive; i++) {
            if (landscapeSection[i] > maxHeight) {
                maxHeight = landscapeSection[i];
                maxPositions.clear();
                maxPositions.add(i);
                continue;
            }
            if (landscapeSection[i] == maxHeight) {
                maxPositions.add(i);
            }
        }

        return new MaxHeightEntry(maxHeight, maxPositions);
    }

    private static int calculateWaterSingleSectionBetweenWalls(int[] landscapeSection,
                                                        int startIndexInclusive,
                                                        int stopIndexExclusive) {
        int leftWallHeight  = landscapeSection[startIndexInclusive - 1];
        int rightWallHeight = landscapeSection[stopIndexExclusive];

        return Math.min(leftWallHeight, rightWallHeight) - landscapeSection[startIndexInclusive];
    }

    private static int calculateWaterSingleSectionWhenAnySideIsEmpty() {
        return 0;
    }

    private static long calculateWaterBetweenPositions(int[] landscapeSection,
                                                       int startPositionInclusive,
                                                       int endPositionExclusive,
                                                       int targetHeight) {
        long sum = 0;
        for (int i = startPositionInclusive; i < endPositionExclusive; i++) {
            sum += targetHeight - landscapeSection[i];
        }
        return sum;
    }





    private static class MaxHeightEntry {
        private int mHeight = -1;
        private final SortedSet<Integer> mPositions;

        MaxHeightEntry(int height, SortedSet<Integer> positions) {
            mHeight = height;
            mPositions = positions;
        }

        public int getHeight() {
            return mHeight;
        }

        public SortedSet<Integer> getPositions() {
            return mPositions;
        }
    }

    private enum Border {
        EMPTY,
        WALL
    }
}
