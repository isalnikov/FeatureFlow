package com.featureflow.planning.montecarlo;

import com.featureflow.domain.entity.FeatureRequest;
import com.featureflow.domain.valueobject.EffortEstimate;
import com.featureflow.domain.valueobject.MonteCarloParameters;
import com.featureflow.domain.valueobject.ThreePointEstimate;

import java.time.LocalDate;
import java.util.*;

public class MonteCarloSimulator {

    public Map<UUID, FeatureDeadlineProbability> simulate(
        List<FeatureRequest> features,
        Map<UUID, LocalDate> baseCompletionDates,
        MonteCarloParameters params,
        Random random
    ) {
        Map<UUID, List<LocalDate>> completionDates = new HashMap<>();

        for (FeatureRequest feature : features) {
            completionDates.put(feature.getId(), new ArrayList<>());
        }

        for (int i = 0; i < params.iterations(); i++) {
            for (FeatureRequest feature : features) {
                LocalDate baseDate = baseCompletionDates.get(feature.getId());
                if (baseDate == null) continue;

                LocalDate perturbedDate = perturbCompletionDate(feature, baseDate, random);
                completionDates.get(feature.getId()).add(perturbedDate);
            }
        }

        Map<UUID, FeatureDeadlineProbability> results = new LinkedHashMap<>();

        for (FeatureRequest feature : features) {
            List<LocalDate> dates = completionDates.get(feature.getId());
            results.put(feature.getId(), computeProbability(feature, dates));
        }

        return results;
    }

    private LocalDate perturbCompletionDate(FeatureRequest feature, LocalDate baseDate, Random random) {
        ThreePointEstimate estimate = feature.getStochasticEstimate();
        if (estimate == null) {
            return baseDate;
        }

        double sampled = sampleTriangular(
            estimate.optimistic(),
            estimate.mostLikely(),
            estimate.pessimistic(),
            random
        );

        double expected = estimate.expected();
        double ratio = sampled / expected;

        // Derive duration from effort estimate (8h work day)
        EffortEstimate effort = feature.getEffortEstimate();
        double durationDays = (effort != null && effort.totalHours() > 0)
            ? effort.totalHours() / 8.0
            : 5.0; // default 5-day duration if no effort estimate

        // Perturb the duration, not the absolute date
        long perturbationDays = Math.round(durationDays * (ratio - 1));
        return baseDate.plusDays(perturbationDays);
    }

    private double sampleTriangular(double min, double mode, double max, Random random) {
        double u = random.nextDouble();
        double c = (mode - min) / (max - min);

        if (u < c) {
            return min + Math.sqrt(u * (max - min) * (mode - min));
        } else {
            return max - Math.sqrt((1 - u) * (max - min) * (max - mode));
        }
    }

    private FeatureDeadlineProbability computeProbability(FeatureRequest feature, List<LocalDate> dates) {
        if (!feature.hasDeadline()) {
            return new FeatureDeadlineProbability(
                feature.getId(), null, 0.0, null, null
            );
        }

        long metDeadline = dates.stream()
            .filter(d -> !d.isAfter(feature.getDeadline()))
            .count();

        double probability = (double) metDeadline / dates.size();

        List<LocalDate> sorted = dates.stream().sorted().toList();
        LocalDate p50 = sorted.get((int) (sorted.size() * 0.5));
        LocalDate p90 = sorted.get((int) (sorted.size() * 0.9));

        return new FeatureDeadlineProbability(
            feature.getId(),
            feature.getDeadline(),
            probability,
            p50,
            p90
        );
    }
}
