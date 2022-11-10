package ru.anmo.world_generator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

@Component
public class WorldGenerator implements IWorldGenerator{
    public static final int MIN_LENGTH = 0;
    public static final int MAX_LENGTH = 32000;
    public static final int MIN_HEIGHT = 0;
    public static final int MAX_HEIGHT = 32000;

    @Autowired
    private RandomGenerator randomizer;

    @Override
    public int[] generateWorld(int... manualHeights) {
        int[] generatedWorld = new int[manualHeights.length];
        System.arraycopy(manualHeights, 0, generatedWorld, 0, manualHeights.length);
        return generatedWorld;
    }

    @Override
    public int[] generateWorld(int length, int maxHeight) throws IncorrectWorldParametersException {
        if (length < 0 || length > MAX_LENGTH) {
            throw new IncorrectWorldParametersException("Incorrect length = " + length + ", should be between " + MIN_LENGTH + " and " + MAX_LENGTH);
        }
        if (maxHeight < 0 || maxHeight > MAX_HEIGHT) {
            throw new IncorrectWorldParametersException("Incorrect maxHeight = " + maxHeight + ", should be between " + MIN_HEIGHT + " and " + MAX_HEIGHT);
        }

        int[] generatedWorld = new int[length];
        IntStream.range(0, length).forEach(i -> {
            generatedWorld[i] = randomizer.nextInt(MIN_HEIGHT, maxHeight + 1);
        });

        debugPrintWorld(generatedWorld, length, maxHeight);

        return generatedWorld;
    }

    @Override
    public final int[] generateRandomWorld() {
        int[] generatedWorld = null;
        try {
            generatedWorld = generateWorld(randomizer.nextInt(MIN_LENGTH, MAX_LENGTH),
                                           randomizer.nextInt(MIN_HEIGHT, MAX_HEIGHT));
        } catch (IncorrectWorldParametersException e) {
            System.out.println("Shouldn't get there, developer was drunk lol");
        }
        return generatedWorld;
    }




    private void debugPrintWorld(int[] generatedWorld, int length, int maxHeight) {
        System.out.println();
        System.out.println("==== Generated World (length = " + length + ", maxHeight = " + maxHeight + ") ====");
        for (int i = 0; i < Math.min(30, generatedWorld.length); i++) {
            System.out.print(generatedWorld[i] + ", ");
        }
        System.out.println();
        System.out.println("======================================================================");
        System.out.println();
    }
}
