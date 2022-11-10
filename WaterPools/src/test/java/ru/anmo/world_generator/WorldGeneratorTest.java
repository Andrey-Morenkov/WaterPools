package ru.anmo.world_generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.anmo.configuration.AppConfiguration;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class WorldGeneratorTest {

    private AnnotationConfigApplicationContext context = null;
    private WorldGenerator worldGenerator;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext(AppConfiguration.class);
        worldGenerator = context.getBean(WorldGenerator.class);
    }

    @Test
    void generateCustomWorldWithCorrectLengthAndHeight() {
        int worldLength = 100;
        int worldHeight = 100;

        int[] generatedWorld = assertDoesNotThrow(() -> worldGenerator.generateWorld(worldLength,worldHeight));
        checkWorld(generatedWorld, worldLength, worldHeight);
    }

    @Test
    void generateRandomWorld() {
        checkWorld(worldGenerator.generateRandomWorld());
    }

    @Test
    void generateManualWorld() {
        int[] worldHeights = {2, 3, 3, 1, 2, 1, 0, 5, 1, 3, 2, 0, 2, 0, 1, 5, 2, 1, 1, 2};

        int[] generatedWorld = worldGenerator.generateWorld(worldHeights);
        checkWorld(generatedWorld);
    }

    @Nested
    class WorldGeneratorLengthLogic {
        @Test
        void generateWorldNegativeLength() {
            int worldLength = -1;
            int worldHeight = 100;

            assertThrows(IWorldGenerator.IncorrectWorldParametersException.class, () -> worldGenerator.generateWorld(worldLength, worldHeight));
        }

        @Test
        void generateWorldZeroLength() {
            int worldLength = 0;
            int worldHeight = 100;

            int[] generatedWorld = assertDoesNotThrow(() -> worldGenerator.generateWorld(worldLength, worldHeight));
            checkWorld(generatedWorld, worldLength, worldHeight);
        }

        @Test
        void generateWorldLessThanMinLength() {
            int worldLength = WorldGenerator.MIN_LENGTH - 1;
            int worldHeight = 100;

            assertThrows(IWorldGenerator.IncorrectWorldParametersException.class, () -> worldGenerator.generateWorld(worldLength, worldHeight));
        }

        @Test
        void generateWorldMaxLength() {
            int worldLength = WorldGenerator.MAX_LENGTH;
            int worldHeight = 100;

            int[] generatedWorld = assertDoesNotThrow(() -> worldGenerator.generateWorld(worldLength,worldHeight));
            checkWorld(generatedWorld);
        }

        @Test
        void generateWorldTooBigLength() {
            int worldLength = WorldGenerator.MAX_LENGTH + 1;
            int worldHeight = 100;

            assertThrows(IWorldGenerator.IncorrectWorldParametersException.class, () -> worldGenerator.generateWorld(worldLength, worldHeight));
        }
    }



    @Nested
    class WorldGeneratorHeightLogic {
        @Test
        void generateWorldNegativeHeight() {
            int worldLength = 100;
            int worldHeight = -1;

            assertThrows(IWorldGenerator.IncorrectWorldParametersException.class, () -> worldGenerator.generateWorld(worldLength, worldHeight));
        }

        @Test
        void generateWorldZeroHeight() {
            int worldLength = 100;
            int worldHeight = 0;

            int[] generatedWorld = assertDoesNotThrow(() -> worldGenerator.generateWorld(worldLength, worldHeight));
            checkWorld(generatedWorld, worldLength, worldHeight);
        }

        @Test
        void generateWorldLessThanMinHeight() {
            int worldLength = 100;
            int worldHeight = WorldGenerator.MIN_HEIGHT - 1;

            assertThrows(IWorldGenerator.IncorrectWorldParametersException.class, () -> worldGenerator.generateWorld(worldLength, worldHeight));
        }

        @Test
        void generateWorldMaxHeight() {
            int worldLength = 100;
            int worldHeight = WorldGenerator.MAX_HEIGHT;

            int[] generatedWorld = assertDoesNotThrow(() -> worldGenerator.generateWorld(worldLength,worldHeight));
            checkWorld(generatedWorld);
        }

        @Test
        void generateWorldTooBigHeight() {
            int worldLength = 100;
            int worldHeight = WorldGenerator.MAX_HEIGHT + 1;

            assertThrows(IWorldGenerator.IncorrectWorldParametersException.class, () -> worldGenerator.generateWorld(worldLength, worldHeight));
        }
    }





    void checkWorld(int[] generatedWorld) {
        checkWorld(generatedWorld, WorldGenerator.MAX_LENGTH, WorldGenerator.MAX_HEIGHT);
    }

    void checkWorld(int[] generatedWorld, int initialLength, int initialMaxHeight) {
        assertNotNull(generatedWorld);
        assertTrue(isWorldHeightIsCorrect(generatedWorld, initialMaxHeight));
        assertTrue(isWorldLengthIsCorrect(generatedWorld, initialLength));
    }

    boolean isWorldHeightIsCorrect(int[] generatedWorld, int initialMaxHeight) {
        return Arrays
                .stream(generatedWorld)
                .noneMatch(height -> (height > initialMaxHeight) || (height < WorldGenerator.MIN_HEIGHT));
    }

    boolean isWorldLengthIsCorrect(int[] generatedWorld, int initialLength) {
        return generatedWorld.length <= initialLength;
    }
}