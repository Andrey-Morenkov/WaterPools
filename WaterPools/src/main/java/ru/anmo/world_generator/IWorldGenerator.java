package ru.anmo.world_generator;

public interface IWorldGenerator {

    int[] generateWorld(int... manualHeights);
    int[] generateWorld(int length, int maxHeight) throws IncorrectWorldParametersException;
    int[] generateRandomWorld();

    class IncorrectWorldParametersException extends Exception {
        IncorrectWorldParametersException(String err) {
            super(err);
        }
    }
}
