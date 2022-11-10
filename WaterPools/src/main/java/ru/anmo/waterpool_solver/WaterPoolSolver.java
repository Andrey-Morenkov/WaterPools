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




    private MaxHeightEntry findMaxHeight(int[] landscapeSection, int startIndexInclusive, int stopIndexExclusive) {
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



    private long calculateWater(int[] landscapeSection,
                                int startIndexInclusive,
                                int stopIndexExclusive,
                                Border leftBorder,
                                Border rightBorder) {
        //TODO: prettify
        int currLength = stopIndexExclusive - startIndexInclusive;
        if (currLength <= 0) {
            return 0;
        }
        if (currLength == 1) {
            if (leftBorder == Border.WALL && rightBorder == Border.WALL) {
                int leftWallHeight = landscapeSection[startIndexInclusive - 1];
                int rightWallHeight = landscapeSection[stopIndexExclusive];

                return Math.min(leftWallHeight, rightWallHeight) - landscapeSection[startIndexInclusive];
            }
            return 0;
        }

        MaxHeightEntry maxPeaks = findMaxHeight(landscapeSection, startIndexInclusive, stopIndexExclusive);
        int leftPeak = maxPeaks.getPositions().first();
        int rightPeak = maxPeaks.getPositions().last();

        if (rightBorder == Border.WALL) {
            int sum = 0;
            for (int i = leftPeak + 1; i < stopIndexExclusive; i++) {
                sum += maxPeaks.getHeight() - landscapeSection[i];
            }
            return sum + calculateWater(landscapeSection, startIndexInclusive, leftPeak, leftBorder, Border.WALL);
        }
        if (leftBorder == Border.WALL) {
            int sum = 0;
            for (int i = rightPeak - 1; i >= startIndexInclusive; i--) {
                sum += maxPeaks.getHeight() - landscapeSection[i];
            }
            return sum + calculateWater(landscapeSection, rightPeak + 1, stopIndexExclusive, Border.WALL, rightBorder);
        }

        int sum = 0;
        for (int i = leftPeak + 1; i < rightPeak; i++) {
            int waterCount = maxPeaks.getHeight() - landscapeSection[i];
            sum += waterCount;
        }
        return sum + calculateWater(landscapeSection, startIndexInclusive, leftPeak, leftBorder, Border.WALL) + calculateWater(landscapeSection, rightPeak + 1, stopIndexExclusive, Border.WALL, leftBorder);
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
