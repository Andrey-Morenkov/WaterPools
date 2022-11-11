package ru.anmo;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.anmo.configuration.AppConfiguration;
import ru.anmo.waterpool_solver.IWaterPoolSolver;
import ru.anmo.waterpool_solver.WaterPoolSolver;
import ru.anmo.world_generator.IWorldGenerator;
import ru.anmo.world_generator.WorldGenerator;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {

        System.out.println("See WaterPoolSolverTest.checkManualWorldSolving()");

        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfiguration.class);

        IWorldGenerator  worldGenerator  = context.getBean(WorldGenerator.class);
        IWaterPoolSolver waterPoolSolver = context.getBean(WaterPoolSolver.class);

        int[] generatedWorld = worldGenerator.generateRandomWorld();
        long startTime = System.nanoTime ();
        long waterAmount = waterPoolSolver.calculateWaterAmount(generatedWorld);
        long stopTime = System.nanoTime ();

        System.out.println("millis = " + TimeUnit.NANOSECONDS.toMillis(stopTime - startTime) +  ", Water amount = " +  waterAmount);
    }
}