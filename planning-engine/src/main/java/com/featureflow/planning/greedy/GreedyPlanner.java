package com.featureflow.planning.greedy;

import com.featureflow.domain.entity.*;
import com.featureflow.domain.exception.DependencyCycleException;
import com.featureflow.domain.planning.*;
import com.featureflow.domain.rules.OwnershipValidator;
import com.featureflow.domain.valueobject.Capacity;
import com.featureflow.domain.valueobject.EffortEstimate;
import com.featureflow.domain.valueobject.Role;

import java.time.LocalDate;
import java.util.*;

public class GreedyPlanner {

    public PlanningResult plan(PlanningRequest request) {
        long start = System.currentTimeMillis();

        List<FeatureRequest> sortedFeatures = FeatureSorter.sortByPriority(
            request.features(),
            request.products()
        );

        Map<UUID, Map<Role, Double>> remainingCapacity = initRemainingCapacity(request);

        List<Assignment> assignments = new ArrayList<>();
        Map<UUID, FeatureTimeline> timelines = new LinkedHashMap<>();
        List<Conflict> conflicts = new ArrayList<>();

        Map<UUID, LocalDate> featureEndDates = new HashMap<>();

        for (FeatureRequest feature : sortedFeatures) {
            LocalDate earliestStart = computeEarliestStart(feature, featureEndDates, request);

            List<Team> candidates = OwnershipValidator.findCandidateTeams(
                feature,
                request.teams(),
                indexById(request.products(), Product::getId)
            );

            if (candidates.isEmpty()) {
                conflicts.add(new Conflict(
                    Conflict.Type.OWNERSHIP_VIOLATION,
                    feature.getId(),
                    null,
                    "No team owns products for feature: " + feature.getTitle()
                ));
                continue;
            }

            AssignmentResult result = findBestAssignment(
                feature, candidates, earliestStart, remainingCapacity, request
            );

            if (result != null) {
                assignments.addAll(result.assignments);
                timelines.put(feature.getId(), result.timeline);
                featureEndDates.put(feature.getId(), result.endDate);
                deductCapacity(remainingCapacity, result);
            } else {
                conflicts.add(new Conflict(
                    Conflict.Type.CAPACITY_EXCEEDED,
                    feature.getId(),
                    null,
                    "No team has sufficient capacity for feature: " + feature.getTitle()
                ));
            }
        }

        Map<UUID, TeamLoadReport> teamLoadReports = computeTeamLoadReports(assignments, request);
        double totalCost = computeCost(assignments, timelines, request.parameters());

        long elapsed = System.currentTimeMillis() - start;

        return new PlanningResult(
            assignments,
            conflicts,
            timelines,
            teamLoadReports,
            totalCost,
            elapsed,
            PlanningResult.PlanningAlgorithm.GREEDY
        );
    }

    private Map<UUID, Map<Role, Double>> initRemainingCapacity(PlanningRequest request) {
        Map<UUID, Map<Role, Double>> capacity = new HashMap<>();

        for (Team team : request.teams()) {
            for (Sprint sprint : request.planningWindow().getSprints()) {
                UUID key = capacityKey(team.getId(), sprint.id());
                Capacity effective = team.effectiveCapacityForSprint(sprint);
                Map<Role, Double> roleCapacity = new EnumMap<>(Role.class);
                for (Role role : Role.values()) {
                    roleCapacity.put(role, effective.effectiveHours(role));
                }
                capacity.put(key, roleCapacity);
            }
        }

        return capacity;
    }

    private LocalDate computeEarliestStart(
        FeatureRequest feature,
        Map<UUID, LocalDate> featureEndDates,
        PlanningRequest request
    ) {
        LocalDate earliest = request.planningWindow().getStartDate();

        for (UUID depId : feature.getDependencies()) {
            LocalDate depEnd = featureEndDates.get(depId);
            if (depEnd != null && depEnd.isAfter(earliest)) {
                earliest = depEnd.plusDays(1);
            }
        }

        return earliest;
    }

    private AssignmentResult findBestAssignment(
        FeatureRequest feature,
        List<Team> candidates,
        LocalDate earliestStart,
        Map<UUID, Map<Role, Double>> remainingCapacity,
        PlanningRequest request
    ) {
        AssignmentResult best = null;

        for (Team team : candidates) {
            AssignmentResult result = tryAssign(feature, team, earliestStart, remainingCapacity, request);
            if (result != null) {
                if (best == null || result.startDate.isBefore(best.startDate)) {
                    best = result;
                }
            }
        }

        return best;
    }

    private AssignmentResult tryAssign(
        FeatureRequest feature,
        Team team,
        LocalDate earliestStart,
        Map<UUID, Map<Role, Double>> remainingCapacity,
        PlanningRequest request
    ) {
        List<Sprint> sprints = request.planningWindow().getSprints();
        EffortEstimate effort = feature.getEffortEstimate();

        int startIdx = findSprintIndexAtOrAfter(sprints, earliestStart);
        if (startIdx < 0) return null;

        Map<Role, Double> remaining = new EnumMap<>(Role.class);
        for (Role role : Role.values()) {
            remaining.put(role, effort.forRole(role));
        }

        List<Assignment> assignments = new ArrayList<>();
        int currentIdx = startIdx;

        while (hasRemainingEffort(remaining) && currentIdx < sprints.size()) {
            Sprint sprint = sprints.get(currentIdx);
            UUID key = capacityKey(team.getId(), sprint.id());
            Map<Role, Double> sprintCapacity = remainingCapacity.get(key);

            if (sprintCapacity == null) {
                currentIdx++;
                continue;
            }

            Map<Role, Double> allocated = new EnumMap<>(Role.class);
            for (Role role : Role.values()) {
                double needed = remaining.getOrDefault(role, 0.0);
                double available = sprintCapacity.getOrDefault(role, 0.0);
                double use = Math.min(needed, available);
                allocated.put(role, use);
                remaining.put(role, needed - use);
                sprintCapacity.put(role, available - use);
            }

            double totalAllocated = allocated.values().stream().mapToDouble(Double::doubleValue).sum();
            if (totalAllocated > 0) {
                UUID assignmentId = UUID.randomUUID();
                assignments.add(new Assignment(
                    assignmentId,
                    feature.getId(),
                    team.getId(),
                    sprint.id(),
                    new EffortEstimate(
                        allocated.getOrDefault(Role.BACKEND, 0.0),
                        allocated.getOrDefault(Role.FRONTEND, 0.0),
                        allocated.getOrDefault(Role.QA, 0.0),
                        allocated.getOrDefault(Role.DEVOPS, 0.0)
                    )
                ));
            }

            currentIdx++;
        }

        if (hasRemainingEffort(remaining)) {
            return null;
        }

        if (assignments.isEmpty()) return null;

        Sprint firstSprint = sprints.get(startIdx);
        Sprint lastSprint = sprints.get(currentIdx - 1);

        FeatureTimeline timeline = new FeatureTimeline(
            feature.getId(),
            firstSprint.startDate(),
            lastSprint.endDate(),
            1.0
        );

        return new AssignmentResult(assignments, timeline, lastSprint.endDate(), firstSprint.startDate());
    }

    private boolean hasRemainingEffort(Map<Role, Double> remaining) {
        return remaining.values().stream().anyMatch(v -> v > 0.001);
    }

    private int findSprintIndexAtOrAfter(List<Sprint> sprints, LocalDate date) {
        for (int i = 0; i < sprints.size(); i++) {
            if (!sprints.get(i).endDate().isBefore(date)) {
                return i;
            }
        }
        return -1;
    }

    private void deductCapacity(Map<UUID, Map<Role, Double>> remainingCapacity, AssignmentResult result) {
        for (Assignment assignment : result.assignments) {
            UUID key = capacityKey(assignment.getTeamId(), assignment.getSprintId());
            Map<Role, Double> sprintCapacity = remainingCapacity.get(key);
            if (sprintCapacity != null) {
                EffortEstimate effort = assignment.getAllocatedEffort();
                for (Role role : Role.values()) {
                    double current = sprintCapacity.getOrDefault(role, 0.0);
                    sprintCapacity.put(role, Math.max(0, current - effort.forRole(role)));
                }
            }
        }
    }

    private Map<UUID, TeamLoadReport> computeTeamLoadReports(
        List<Assignment> assignments,
        PlanningRequest request
    ) {
        Map<UUID, TeamLoadReport> reports = new LinkedHashMap<>();
        for (Team team : request.teams()) {
            reports.put(team.getId(), new TeamLoadReport(
                team.getId(),
                Map.of(),
                0.0,
                List.of()
            ));
        }
        return reports;
    }

    private double computeCost(
        List<Assignment> assignments,
        Map<UUID, FeatureTimeline> timelines,
        PlanningParameters params
    ) {
        double cost = 0;
        for (var entry : timelines.entrySet()) {
            FeatureTimeline tl = entry.getValue();
            long daysFromStart = java.time.temporal.ChronoUnit.DAYS.between(
                tl.startDate(), tl.endDate()
            );
            cost += daysFromStart;
        }
        return cost;
    }

    private UUID capacityKey(UUID teamId, UUID sprintId) {
        return UUID.nameUUIDFromBytes((teamId.toString() + sprintId.toString()).getBytes());
    }

    private <T> Map<UUID, T> indexById(List<T> items, java.util.function.Function<T, UUID> idFn) {
        Map<UUID, T> map = new HashMap<>();
        for (T item : items) {
            map.put(idFn.apply(item), item);
        }
        return map;
    }

    private record AssignmentResult(
        List<Assignment> assignments,
        FeatureTimeline timeline,
        LocalDate endDate,
        LocalDate startDate
    ) {}
}
