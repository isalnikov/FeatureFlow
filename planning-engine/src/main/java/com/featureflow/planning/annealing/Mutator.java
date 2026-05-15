package com.featureflow.planning.annealing;

import java.util.*;

public class Mutator {

    public Solution apply(Solution current, List<UUID> featureIds, Random random) {
        int mutationType = random.nextInt(3);

        return switch (mutationType) {
            case 0 -> shiftFeatureSprint(current, featureIds, random);
            case 1 -> swapFeatures(current, featureIds, random);
            default -> current;
        };
    }

    public Solution apply(Solution current, List<UUID> featureIds, List<UUID> candidateTeams, Random random) {
        return reassignTeam(current, featureIds, candidateTeams, random);
    }

    Solution shiftFeatureSprint(Solution current, List<UUID> featureIds, int direction) {
        UUID featureId = featureIds.get(0);
        int currentSprint = current.featureSprintIndex().getOrDefault(featureId, 0);
        int newSprint = Math.max(0, currentSprint + direction);

        var newSprintIndex = new HashMap<>(current.featureSprintIndex());
        newSprintIndex.put(featureId, newSprint);

        return new Solution(
            current.assignments(),
            newSprintIndex,
            current.featureTeamMapping(),
            Double.MAX_VALUE
        );
    }

    Solution swapFeatures(Solution current, UUID f1, UUID f2) {
        var newSprintIndex = new HashMap<>(current.featureSprintIndex());
        Integer s1 = newSprintIndex.get(f1);
        Integer s2 = newSprintIndex.get(f2);

        if (s1 != null && s2 != null) {
            newSprintIndex.put(f1, s2);
            newSprintIndex.put(f2, s1);
        }

        return new Solution(
            current.assignments(),
            newSprintIndex,
            current.featureTeamMapping(),
            Double.MAX_VALUE
        );
    }

    Solution reassignTeam(Solution current, UUID featureId, UUID newTeam) {
        UUID currentTeam = current.featureTeamMapping().get(featureId);
        if (currentTeam != null && currentTeam.equals(newTeam)) {
            return current;
        }

        var newTeamMapping = new HashMap<>(current.featureTeamMapping());
        newTeamMapping.put(featureId, newTeam);

        return new Solution(
            current.assignments(),
            current.featureSprintIndex(),
            newTeamMapping,
            Double.MAX_VALUE
        );
    }

    private Solution shiftFeatureSprint(Solution current, List<UUID> featureIds, Random random) {
        UUID featureId = featureIds.get(random.nextInt(featureIds.size()));
        int currentSprint = current.featureSprintIndex().getOrDefault(featureId, 0);
        int direction = random.nextBoolean() ? 1 : -1;
        int newSprint = Math.max(0, currentSprint + direction);

        var newSprintIndex = new HashMap<>(current.featureSprintIndex());
        newSprintIndex.put(featureId, newSprint);

        return new Solution(
            current.assignments(),
            newSprintIndex,
            current.featureTeamMapping(),
            Double.MAX_VALUE
        );
    }

    private Solution swapFeatures(Solution current, List<UUID> featureIds, Random random) {
        if (featureIds.size() < 2) return current;

        int i1 = random.nextInt(featureIds.size());
        int i2 = random.nextInt(featureIds.size());
        while (i2 == i1) {
            i2 = random.nextInt(featureIds.size());
        }

        UUID f1 = featureIds.get(i1);
        UUID f2 = featureIds.get(i2);

        var newSprintIndex = new HashMap<>(current.featureSprintIndex());
        Integer s1 = newSprintIndex.get(f1);
        Integer s2 = newSprintIndex.get(f2);

        if (s1 != null && s2 != null) {
            newSprintIndex.put(f1, s2);
            newSprintIndex.put(f2, s1);
        }

        return new Solution(
            current.assignments(),
            newSprintIndex,
            current.featureTeamMapping(),
            Double.MAX_VALUE
        );
    }

    private Solution reassignTeam(Solution current, List<UUID> featureIds, List<UUID> candidateTeams, Random random) {
        UUID featureId = featureIds.get(random.nextInt(featureIds.size()));
        UUID currentTeam = current.featureTeamMapping().get(featureId);

        List<UUID> otherTeams = candidateTeams.stream()
            .filter(t -> !t.equals(currentTeam))
            .toList();

        if (otherTeams.isEmpty()) return current;

        UUID newTeam = otherTeams.get(random.nextInt(otherTeams.size()));

        var newTeamMapping = new HashMap<>(current.featureTeamMapping());
        newTeamMapping.put(featureId, newTeam);

        return new Solution(
            current.assignments(),
            current.featureSprintIndex(),
            newTeamMapping,
            Double.MAX_VALUE
        );
    }
}
