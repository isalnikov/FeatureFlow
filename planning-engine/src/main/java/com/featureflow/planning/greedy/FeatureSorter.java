package com.featureflow.planning.greedy;

import com.featureflow.domain.entity.FeatureRequest;
import com.featureflow.domain.entity.Product;
import com.featureflow.domain.exception.DependencyCycleException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class FeatureSorter {

    private FeatureSorter() {
    }

    public static List<FeatureRequest> sortByPriority(
        List<FeatureRequest> features,
        List<Product> products
    ) {
        Map<UUID, FeatureRequest> featureMap = features.stream()
            .collect(Collectors.toMap(FeatureRequest::getId, Function.identity()));

        Map<UUID, Integer> inDegree = new HashMap<>();
        Map<UUID, List<UUID>> dependents = new HashMap<>();

        for (FeatureRequest f : features) {
            inDegree.putIfAbsent(f.getId(), 0);
            dependents.putIfAbsent(f.getId(), new ArrayList<>());
        }

        for (FeatureRequest f : features) {
            for (UUID depId : f.getDependencies()) {
                inDegree.merge(f.getId(), 1, Integer::sum);
                dependents.computeIfAbsent(depId, k -> new ArrayList<>()).add(f.getId());
            }
        }

        PriorityQueue<FeatureRequest> queue = new PriorityQueue<>(
            Comparator.<FeatureRequest>comparingDouble(FeatureRequest::getBusinessValue)
                .reversed()
                .thenComparing(f -> featureMap.keySet().stream()
                    .toList()
                    .indexOf(f.getId()))
        );

        for (FeatureRequest f : features) {
            if (inDegree.get(f.getId()) == 0) {
                queue.add(f);
            }
        }

        List<FeatureRequest> sorted = new ArrayList<>();
        while (!queue.isEmpty()) {
            FeatureRequest current = queue.poll();
            sorted.add(current);

            for (UUID dependentId : dependents.getOrDefault(current.getId(), List.of())) {
                int newDegree = inDegree.merge(dependentId, -1, Integer::sum);
                if (newDegree == 0) {
                    queue.add(featureMap.get(dependentId));
                }
            }
        }

        if (sorted.size() < features.size()) {
            throw new DependencyCycleException("Cyclic dependency detected in features");
        }

        return sorted;
    }
}
