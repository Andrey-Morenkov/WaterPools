package ru.anmo.waterpool_solver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class WaterPoolSolver implements IWaterPoolSolver{

    @Autowired
    private ExecutorService executor;

    private static final int LENGTH_PER_TASK = 100;

    @Override
    public long calculateWaterAmount(int[] landscape) {
        // Landscapes with 0,1,2 lengths are always dry
        if (landscape.length < 3) {
            return 0;
        }

        try {
            return calculateWater(landscape, 0, landscape.length, Border.EMPTY, Border.EMPTY);
        }
        catch (Exception e) {
            System.out.println("Something went wrong: " + e.getLocalizedMessage());
            return -1;
        }
        finally {
            executor.shutdown();
        }
    }



    private long calculateWater(final int[] landscapeSection,
                                int startIndexInclusive,
                                int endIndexExclusive,
                                final Border leftBorder,
                                final Border rightBorder) throws Exception {

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
        int mostLeftPeak  = maxPeaks.getMostLeftPosition();
        int mostRightPeak = maxPeaks.getMostRightPosition();

        if (rightBorder == Border.WALL) {
            Future<Long> waterAmountFromLeftPeakToEnd = calculateWaterBetweenPositions(landscapeSection, mostLeftPeak + 1, endIndexExclusive, maxPeaks.getHeight());
            return calculateWaterFromStartToLeftPeak(landscapeSection, startIndexInclusive, mostLeftPeak, leftBorder)
                    + waterAmountFromLeftPeakToEnd.get();
        }
        if (leftBorder == Border.WALL) {
            Future<Long> waterAmountFromStartToRightPeak = calculateWaterBetweenPositions(landscapeSection, startIndexInclusive, mostRightPeak, maxPeaks.getHeight());
            return calculateWaterFromRightPeakToEnd(landscapeSection, mostRightPeak + 1, endIndexExclusive, rightBorder)
                    + waterAmountFromStartToRightPeak.get();
        }

        Future<Long> waterAmountBetweenStartAndLeftPeak  = executor.submit(() -> calculateWaterFromStartToLeftPeak(landscapeSection, startIndexInclusive, mostLeftPeak, leftBorder));
        Future<Long> waterAmountBetweenLeftAndRightPeaks = calculateWaterBetweenPositions(landscapeSection, mostLeftPeak + 1, mostRightPeak, maxPeaks.getHeight());
        Future<Long> waterAmountBetweenRightPeakAndEnd   = executor.submit(() -> calculateWaterFromRightPeakToEnd(landscapeSection, mostRightPeak + 1, endIndexExclusive, rightBorder));

        return waterAmountBetweenLeftAndRightPeaks.get()
                + waterAmountBetweenStartAndLeftPeak.get()
                + waterAmountBetweenRightPeakAndEnd.get();
    }


    private long calculateWaterFromStartToLeftPeak(final int[] landscapeSection, int startIndexInclusive, int stopIndexExclusive, final Border leftBorder) throws Exception {
        return calculateWater(landscapeSection, startIndexInclusive, stopIndexExclusive, leftBorder, Border.WALL);
    }

    private long calculateWaterFromRightPeakToEnd(final int[] landscapeSection, int startIndexInclusive, int stopIndexExclusive, final Border rightBorder) throws Exception {
        return calculateWater(landscapeSection, startIndexInclusive, stopIndexExclusive, Border.WALL, rightBorder);
    }



    private MaxHeightEntry findMaxHeight(final int[] landscapeSection, int startIndexInclusive, int endIndexExclusive) throws Exception {
        List<Callable<MaxHeightEntry>> tasks = new ArrayList<>();

        for (int i = startIndexInclusive; i < endIndexExclusive;) {
            int currTaskEnd = Math.min(i + LENGTH_PER_TASK, endIndexExclusive);
            tasks.add(findMaxHeightTask(landscapeSection, i, currTaskEnd));
            i = currTaskEnd;
        }

        List<Future<MaxHeightEntry>> results = executor.invokeAll(tasks);
        return mergeMaxHeightEntryFromTasks(results);
    }

    private Callable<MaxHeightEntry> findMaxHeightTask(final int[] landscapeSection, int startIndexInclusive, int endIndexExclusive) {
        return () -> {
            int maxHeight = -1;
            int mostLeftPosition = Integer.MAX_VALUE;
            int mostRightPosition = -1;

            for (int i = startIndexInclusive; i < endIndexExclusive; i++) {
                if (landscapeSection[i] > maxHeight) {
                    maxHeight = landscapeSection[i];
                    mostLeftPosition = mostRightPosition = i;
                    continue;
                }
                if (landscapeSection[i] == maxHeight) {
                    mostRightPosition = i;
                    continue;
                }
            }

            return new MaxHeightEntry(maxHeight, mostLeftPosition, mostRightPosition);
        };
    }

    private MaxHeightEntry mergeMaxHeightEntryFromTasks(List<Future<MaxHeightEntry>> results) {
        int finalMaxHeight = -1;
        int finalMostLeftPosition = Integer.MAX_VALUE;
        int finalMostRightPosition = -1;

        for (MaxHeightEntry result : results.stream().map(Future::resultNow).toList()) {
            if (result.getHeight() > finalMaxHeight) {
                finalMaxHeight = result.getHeight();
                finalMostLeftPosition = result.getMostLeftPosition();
                finalMostRightPosition = result.getMostRightPosition();
                continue;
            }
            if (result.getHeight() == finalMaxHeight) {
                if (result.getMostLeftPosition() < finalMostLeftPosition) {
                    finalMostRightPosition = result.getMostLeftPosition();
                }
                if (result.getMostRightPosition() > finalMostRightPosition) {
                    finalMostRightPosition = result.getMostRightPosition();
                }
            }
        }

        return new MaxHeightEntry(finalMaxHeight, finalMostLeftPosition, finalMostRightPosition);
    }

    private static int calculateWaterSingleSectionBetweenWalls(final int[] landscapeSection,
                                                               int startIndexInclusive,
                                                               int stopIndexExclusive) {
        int leftWallHeight  = landscapeSection[startIndexInclusive - 1];
        int rightWallHeight = landscapeSection[stopIndexExclusive];

        return Math.min(leftWallHeight, rightWallHeight) - landscapeSection[startIndexInclusive];
    }

    private static int calculateWaterSingleSectionWhenAnySideIsEmpty() {
        return 0;
    }

    private Future<Long> calculateWaterBetweenPositions(final int[] landscapeSection,
                                                        int startPositionInclusive,
                                                        int endPositionExclusive,
                                                        int targetHeight) {
        Callable<Long> result = () -> {
            long sum = 0;
            for (int i = startPositionInclusive; i < endPositionExclusive; i++) {
                sum += targetHeight - landscapeSection[i];
            }
            return sum;
        };

        return executor.submit(result);
    }





    private static class MaxHeightEntry {
        private final int mHeight;
        private final int mMostLeftPosition;
        private final int mMostRightPosition;

        MaxHeightEntry(int height, int mostLeftPosition, int mostRightPosition) {
            mHeight = height;
            mMostLeftPosition = mostLeftPosition;
            mMostRightPosition = mostRightPosition;
        }

        public int getHeight() {
            return mHeight;
        }

        public int getMostLeftPosition() {
            return mMostLeftPosition;
        }

        public int getMostRightPosition() {
            return mMostRightPosition;
        }
    }

    private enum Border {
        EMPTY,
        WALL
    }
}
