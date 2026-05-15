package com.featureflow.planning.annealing;

import com.featureflow.domain.valueobject.AnnealingParameters;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class SimulatedAnnealing {

    private final Mutator mutator;

    public SimulatedAnnealing(Mutator mutator) {
        this.mutator = mutator;
    }

    public Solution optimize(
        Solution initialSolution,
        AnnealingParameters params,
        List<UUID> featureIds,
        List<UUID> candidateTeams,
        List<UUID> lockedFeatures
    ) {
        Solution current = initialSolution;
        Solution best = current;
        double temperature = params.initialTemperature();
        Random random = new Random(42);

        int iteration = 0;
        while (temperature > params.minTemperature() && iteration < params.maxIterations()) {
            Solution neighbor = generateNeighbor(current, featureIds, candidateTeams, lockedFeatures, random);

            double deltaCost = neighbor.cost() - current.cost();

            if (deltaCost < 0 || random.nextDouble() < Math.exp(-deltaCost / temperature)) {
                current = neighbor;

                if (current.cost() < best.cost()) {
                    best = current;
                }
            }

            temperature *= params.coolingRate();
            iteration++;
        }

        return best;
    }

    private Solution generateNeighbor(
        Solution current,
        List<UUID> featureIds,
        List<UUID> candidateTeams,
        List<UUID> lockedFeatures,
        Random random
    ) {
        List<UUID> mutableFeatures = featureIds.stream()
            .filter(f -> !lockedFeatures.contains(f))
            .toList();

        if (mutableFeatures.isEmpty()) return current;

        int mutationType = random.nextInt(3);

        return switch (mutationType) {
            case 0 -> mutator.shiftFeatureSprint(current, mutableFeatures, random.nextBoolean() ? 1 : -1);
            case 1 -> {
                if (mutableFeatures.size() >= 2) {
                    UUID f1 = mutableFeatures.get(random.nextInt(mutableFeatures.size()));
                    UUID f2;
                    do {
                        f2 = mutableFeatures.get(random.nextInt(mutableFeatures.size()));
                    } while (f2.equals(f1));
                    yield mutator.swapFeatures(current, f1, f2);
                } else {
                    yield mutator.shiftFeatureSprint(current, mutableFeatures, random.nextBoolean() ? 1 : -1);
                }
            }
            default -> {
                UUID featureId = mutableFeatures.get(random.nextInt(mutableFeatures.size()));
                UUID currentTeam = current.featureTeamMapping().get(featureId);
                List<UUID> otherTeams = candidateTeams.stream()
                    .filter(t -> !t.equals(currentTeam))
                    .toList();
                if (otherTeams.isEmpty()) {
                    yield current;
                } else {
                    UUID newTeam = otherTeams.get(random.nextInt(otherTeams.size()));
                    yield mutator.reassignTeam(current, featureId, newTeam);
                }
            }
        };
    }
}
