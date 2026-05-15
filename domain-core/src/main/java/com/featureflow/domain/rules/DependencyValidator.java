package com.featureflow.domain.rules;

import com.featureflow.domain.exception.DependencyCycleException;

import java.util.*;

public final class DependencyValidator {

    private DependencyValidator() {}

    public static void checkNoCycles(Map<UUID, Set<UUID>> dependencies) {
        Map<UUID, Integer> inDegree = new HashMap<>();
        for (UUID key : dependencies.keySet()) {
            inDegree.putIfAbsent(key, 0);
        }
        for (var entry : dependencies.entrySet()) {
            for (UUID dep : entry.getValue()) {
                inDegree.merge(dep, 1, Integer::sum);
            }
        }

        Queue<UUID> queue = inDegree.entrySet().stream()
            .filter(e -> e.getValue() == 0)
            .map(Map.Entry::getKey)
            .collect(java.util.stream.Collectors.toCollection(ArrayDeque::new));

        int processed = 0;
        while (!queue.isEmpty()) {
            UUID node = queue.poll();
            processed++;
            for (UUID dep : dependencies.getOrDefault(node, Set.of())) {
                int newDegree = inDegree.merge(dep, -1, Integer::sum);
                if (newDegree == 0) queue.add(dep);
            }
        }

        if (processed < dependencies.size()) {
            throw new DependencyCycleException("Cyclic dependency detected among features");
        }
    }

    public static List<UUID> topologicalSort(Map<UUID, Set<UUID>> dependencies) {
        Map<UUID, Integer> inDegree = new HashMap<>();
        for (UUID key : dependencies.keySet()) {
            inDegree.putIfAbsent(key, 0);
        }
        for (var entry : dependencies.entrySet()) {
            for (UUID dep : entry.getValue()) {
                inDegree.merge(dep, 1, Integer::sum);
            }
        }

        Queue<UUID> queue = inDegree.entrySet().stream()
            .filter(e -> e.getValue() == 0)
            .map(Map.Entry::getKey)
            .collect(java.util.stream.Collectors.toCollection(ArrayDeque::new));

        List<UUID> sorted = new ArrayList<>();
        while (!queue.isEmpty()) {
            UUID node = queue.poll();
            sorted.add(node);
            for (UUID dep : dependencies.getOrDefault(node, Set.of())) {
                int newDegree = inDegree.merge(dep, -1, Integer::sum);
                if (newDegree == 0) queue.add(dep);
            }
        }

        if (sorted.size() < dependencies.size()) {
            throw new DependencyCycleException("Cyclic dependency detected");
        }

        return sorted;
    }
}
