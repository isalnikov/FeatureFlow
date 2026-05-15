package com.featureflow.planning.annealing;

import com.featureflow.domain.entity.Assignment;
import com.featureflow.domain.entity.FeatureRequest;
import com.featureflow.domain.planning.FeatureTimeline;
import com.featureflow.domain.planning.PlanningResult;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public record Solution(
    Map<UUID, Assignment> assignments,
    Map<UUID, Integer> featureSprintIndex,
    Map<UUID, UUID> featureTeamMapping,
    double cost
) {
    public static Solution from(PlanningResult result) {
        Map<UUID, Assignment> assignmentMap = new HashMap<>();
        Map<UUID, Integer> sprintIndex = new HashMap<>();
        Map<UUID, UUID> teamMapping = new HashMap<>();

        for (Assignment a : result.assignments()) {
            UUID featureId = a.getFeatureId();
            assignmentMap.put(featureId, a);
            teamMapping.put(featureId, a.getTeamId());
        }

        LocalDate baseDate = null;
        for (FeatureTimeline tl : result.featureTimelines().values()) {
            if (baseDate == null || tl.startDate().isBefore(baseDate)) {
                baseDate = tl.startDate();
            }
        }
        if (baseDate == null) {
            baseDate = LocalDate.now();
        }
        LocalDate referenceDate = baseDate;

        for (Map.Entry<UUID, FeatureTimeline> entry : result.featureTimelines().entrySet()) {
            UUID featureId = entry.getKey();
            FeatureTimeline tl = entry.getValue();
            long daysFromStart = ChronoUnit.DAYS.between(referenceDate, tl.startDate());
            sprintIndex.put(featureId, (int) (daysFromStart / 14));
        }

        return new Solution(assignmentMap, sprintIndex, teamMapping, result.totalCost());
    }

    public static double computeWeightedTtm(
        List<FeatureRequest> features,
        Map<UUID, FeatureTimeline> timelines,
        double weight
    ) {
        double total = 0;
        for (FeatureRequest feature : features) {
            FeatureTimeline tl = timelines.get(feature.getId());
            if (tl != null) {
                long daysFromStart = ChronoUnit.DAYS.between(tl.startDate(), tl.endDate());
                total += feature.getBusinessValue() * daysFromStart;
            }
        }
        return total * weight;
    }
}
