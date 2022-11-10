package ru.anmo.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.random.RandomGenerator;

@Configuration
@ComponentScan(value="ru.anmo")
public class AppConfiguration {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    RandomGenerator getRandomizer() {
        return new Random();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    ExecutorService getExecutorService() {
        return Executors.newWorkStealingPool();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    @Qualifier("parallel")
    Integer getParallelTaskCount() {
        return Runtime.getRuntime().availableProcessors();
    }
}
