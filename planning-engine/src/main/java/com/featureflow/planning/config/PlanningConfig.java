package com.featureflow.planning.config;

import com.featureflow.planning.annealing.Mutator;
import com.featureflow.planning.annealing.SimulatedAnnealing;
import com.featureflow.planning.greedy.GreedyPlanner;
import com.featureflow.planning.montecarlo.MonteCarloSimulator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlanningConfig {

    @Bean
    public GreedyPlanner greedyPlanner() {
        return new GreedyPlanner();
    }

    @Bean
    public Mutator mutator() {
        return new Mutator();
    }

    @Bean
    public SimulatedAnnealing simulatedAnnealing(Mutator mutator) {
        return new SimulatedAnnealing(mutator);
    }

    @Bean
    public MonteCarloSimulator monteCarloSimulator() {
        return new MonteCarloSimulator();
    }
}
