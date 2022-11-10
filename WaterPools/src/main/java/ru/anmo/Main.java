package ru.anmo;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.anmo.configuration.AppConfiguration;
import ru.anmo.waterpool_solver.IWaterPoolSolver;
import ru.anmo.waterpool_solver.WaterPoolSolver;
import ru.anmo.world_generator.IWorldGenerator;
import ru.anmo.world_generator.WorldGenerator;

public class Main {
    public static void main(String[] args) {

        System.out.println("See WaterPoolSolverTest.checkManualWorldSolving()");

        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfiguration.class);

        IWorldGenerator  worldGenerator  = context.getBean(WorldGenerator.class);
        IWaterPoolSolver waterPoolSolver = context.getBean(WaterPoolSolver.class);

        int[] generatedWorld = worldGenerator.generateRandomWorld();
        long waterAmount = waterPoolSolver.calculateWaterAmount(generatedWorld);
        System.out.println("Water amount = " +  waterAmount);
    }
}