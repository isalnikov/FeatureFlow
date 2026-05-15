package com.featureflow.domain.entity;

import com.featureflow.domain.valueobject.DateRange;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class PlanningWindow {
    private final UUID id;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Sprint> sprints;

    public PlanningWindow(UUID id, LocalDate startDate, LocalDate endDate) {
        this.id = Objects.requireNonNull(id);
        this.startDate = Objects.requireNonNull(startDate);
        this.endDate = Objects.requireNonNull(endDate);
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        this.sprints = new ArrayList<>();
    }

    public UUID getId() { return id; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public DateRange dateRange() { return new DateRange(startDate, endDate); }

    public List<Sprint> getSprints() { return Collections.unmodifiableList(sprints); }

    public void addSprint(Sprint sprint) { sprints.add(sprint); }
    public void setSprints(List<Sprint> sprints) { this.sprints = new ArrayList<>(sprints); }

    public int sprintIndexForDate(LocalDate date) {
        for (int i = 0; i < sprints.size(); i++) {
            if (sprints.get(i).dateRange().contains(date)) {
                return i;
            }
        }
        return -1;
    }

    public Sprint getSprint(int index) {
        if (index < 0 || index >= sprints.size()) return null;
        return sprints.get(index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlanningWindow that = (PlanningWindow) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PlanningWindow{id=%s, %s to %s, sprints=%d}".formatted(id, startDate, endDate, sprints.size());
    }
}
