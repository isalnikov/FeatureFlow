package com.featureflow.planning.service;

import com.featureflow.domain.planning.FeatureTimeline;
import com.featureflow.domain.planning.PlanningResult;
import com.featureflow.domain.planning.TeamLoadReport;
import com.featureflow.planning.model.ComparisonResult;
import com.featureflow.planning.model.TeamLoadDiff;
import com.featureflow.planning.model.TimelineDiff;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ComparisonService {

    public static ComparisonResult compare(PlanningResult baseline, PlanningResult simulation) {
        return new ComparisonResult(
            baseline.totalCost(),
            simulation.totalCost(),
            simulation.totalCost() - baseline.totalCost(),
            baseline.conflicts().size(),
            simulation.conflicts().size(),
            compareTimelines(baseline.featureTimelines(), simulation.featureTimelines()),
            compareTeamLoads(baseline.teamLoadReports(), simulation.teamLoadReports())
        );
    }

    private static List<TimelineDiff> compareTimelines(
        Map<UUID, FeatureTimeline> baseline,
        Map<UUID, FeatureTimeline> simulation
    ) {
        List<TimelineDiff> diffs = new ArrayList<>();
        for (Map.Entry<UUID, FeatureTimeline> entry : baseline.entrySet()) {
            UUID featureId = entry.getKey();
            FeatureTimeline baseTimeline = entry.getValue();
            FeatureTimeline simTimeline = simulation.get(featureId);

            if (simTimeline != null) {
                LocalDate baseStart = baseTimeline.startDate();
                LocalDate baseEnd = baseTimeline.endDate();
                LocalDate simStart = simTimeline.startDate();
                LocalDate simEnd = simTimeline.endDate();
                long dayShift = ChronoUnit.DAYS.between(baseStart, simStart);

                diffs.add(new TimelineDiff(
                    featureId,
                    "Feature-" + featureId.toString().substring(0, 8),
                    baseStart, baseEnd, simStart, simEnd, dayShift
                ));
            }
        }
        return diffs;
    }

    private static List<TeamLoadDiff> compareTeamLoads(
        Map<UUID, TeamLoadReport> baseline,
        Map<UUID, TeamLoadReport> simulation
    ) {
        List<TeamLoadDiff> diffs = new ArrayList<>();
        for (Map.Entry<UUID, TeamLoadReport> entry : baseline.entrySet()) {
            UUID teamId = entry.getKey();
            TeamLoadReport baseReport = entry.getValue();
            TeamLoadReport simReport = simulation.get(teamId);

            if (simReport != null) {
                double delta = simReport.utilizationPercent() - baseReport.utilizationPercent();
                List<String> newBottlenecks = new ArrayList<>(simReport.bottleneckRoles());
                newBottlenecks.removeAll(baseReport.bottleneckRoles());

                diffs.add(new TeamLoadDiff(
                    teamId,
                    "Team-" + teamId.toString().substring(0, 8),
                    baseReport.utilizationPercent(),
                    simReport.utilizationPercent(),
                    delta,
                    newBottlenecks
                ));
            }
        }
        return diffs;
    }
}
