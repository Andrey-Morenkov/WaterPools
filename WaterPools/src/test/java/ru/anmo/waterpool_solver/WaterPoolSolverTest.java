package ru.anmo.waterpool_solver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.anmo.configuration.AppConfiguration;
import ru.anmo.world_generator.IWorldGenerator;
import ru.anmo.world_generator.WorldGenerator;

import static org.junit.jupiter.api.Assertions.*;

class WaterPoolSolverTest {

    private AnnotationConfigApplicationContext context = null;
    private WaterPoolSolver waterPoolSolver;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext(AppConfiguration.class);
        waterPoolSolver = context.getBean(WaterPoolSolver.class);
    }

    @Test
    void checkManualWorldSolving1() {

        // Check "v4" visualization https://docs.google.com/spreadsheets/d/1nqam-aRyXAa0rM-xaKNeF0LoPea6bWuu01QCQK1i8VM/edit?usp=sharing
        int[] customWorld = { 2, 3, 3, 1, 2, 1, 0, 5, 1, 3, 2, 0, 2, 0, 1, 5, 2, 1, 1, 2 };
        int manualSolution = 36;

        IWorldGenerator worldGenerator = context.getBean(WorldGenerator.class);
        int[] generatedWorld = worldGenerator.generateWorld(customWorld);
        assertEquals(manualSolution, waterPoolSolver.calculateWaterAmount(generatedWorld));
    }

    @Test
    void checkManualWorldSolving2() {

        // Check "v1" visualization https://docs.google.com/spreadsheets/d/1nqam-aRyXAa0rM-xaKNeF0LoPea6bWuu01QCQK1i8VM/edit?usp=sharing
        int[] customWorld = { 2, 4, 3, 5, 1, 1, 0, 3, 2, 4, 1, 0, 0, 0, 1, 3, 2, 2, 2, 6 };
        int manualSolution = 54;

        IWorldGenerator worldGenerator = context.getBean(WorldGenerator.class);
        int[] generatedWorld = worldGenerator.generateWorld(customWorld);
        assertEquals(manualSolution, waterPoolSolver.calculateWaterAmount(generatedWorld));
    }

    @Test
    void checkManualWorldSolving3() {

        int[] customWorld = { 2, 3 ,6 ,2 ,4 ,1 ,0 ,2 ,1 ,3 ,2 ,0 ,2 ,0 ,1 ,3 ,2 ,1 ,1 ,2 };
        int manualSolution = 22;

        IWorldGenerator worldGenerator = context.getBean(WorldGenerator.class);
        int[] generatedWorld = worldGenerator.generateWorld(customWorld);
        assertEquals(manualSolution, waterPoolSolver.calculateWaterAmount(generatedWorld));
    }

    @Test
    void checkManualWorldSolving4() {

        int[] customWorld = { 2, 3, 3 ,2 ,4 ,1 ,0 ,5 ,1 ,3 ,2 ,0 ,2 ,0 ,1 ,4 ,2 ,1 ,1 ,2 };
        int manualSolution = 29;

        IWorldGenerator worldGenerator = context.getBean(WorldGenerator.class);
        int[] generatedWorld = worldGenerator.generateWorld(customWorld);
        assertEquals(manualSolution, waterPoolSolver.calculateWaterAmount(generatedWorld));
    }
}