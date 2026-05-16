package com.featureflow.planning.model;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ComparisonResult(
    double baselineCost,
    double simulationCost,
    double costDelta,
    int baselineConflicts,
    int simulationConflicts,
    List<TimelineDiff> timelineDiffs,
    List<TeamLoadDiff> teamLoadDiffs
) {}
