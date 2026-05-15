package com.featureflow.domain.entity;

import com.featureflow.domain.valueobject.Capacity;
import com.featureflow.domain.valueobject.DateRange;
import com.featureflow.domain.valueobject.Role;

import java.util.*;

public final class Team {
    private final UUID id;
    private String name;
    private List<TeamMember> members;
    private Capacity defaultCapacity;
    private Map<UUID, DateRange> calendar;
    private Set<String> expertiseTags;
    private Double velocity;

    public Team(UUID id, String name) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.members = new ArrayList<>();
        this.calendar = new HashMap<>();
        this.expertiseTags = new HashSet<>();
        this.velocity = null;
        this.defaultCapacity = Capacity.empty();
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = Objects.requireNonNull(name); }

    public List<TeamMember> getMembers() { return Collections.unmodifiableList(members); }

    public void addMember(TeamMember member) { members.add(member); }
    public void removeMember(UUID personId) {
        members.removeIf(m -> m.personId().equals(personId));
    }
    public void setMembers(List<TeamMember> members) { this.members = new ArrayList<>(members); }

    public Map<Role, Long> memberCountByRole() {
        return members.stream()
            .collect(java.util.stream.Collectors.groupingBy(TeamMember::role, java.util.stream.Collectors.counting()));
    }

    public Capacity getDefaultCapacity() { return defaultCapacity; }
    public void setDefaultCapacity(Capacity capacity) { this.defaultCapacity = capacity; }

    public Capacity effectiveCapacityForSprint(Sprint sprint) {
        DateRange override = calendar.get(id);
        if (override == null) {
            return defaultCapacity;
        }
        if (sprint.dateRange().overlaps(override)) {
            return defaultCapacity.withFocusFactor(defaultCapacity.focusFactor() * 0.5);
        }
        return defaultCapacity;
    }

    public Map<UUID, DateRange> getCalendar() { return Collections.unmodifiableMap(calendar); }
    public void addCalendarOverride(UUID teamId, DateRange range) { calendar.put(teamId, range); }
    public void removeCalendarOverride(UUID teamId) { calendar.remove(teamId); }

    public Set<String> getExpertiseTags() { return Collections.unmodifiableSet(expertiseTags); }
    public void addExpertise(String tag) { expertiseTags.add(tag); }
    public void removeExpertise(String tag) { expertiseTags.remove(tag); }
    public void setExpertiseTags(Set<String> tags) { this.expertiseTags = new HashSet<>(tags); }

    public boolean hasExpertise(String tag) { return expertiseTags.contains(tag); }

    public Double getVelocity() { return velocity; }
    public void setVelocity(Double velocity) { this.velocity = velocity; }

    public boolean hasCapacityForRole(Role role) {
        return members.stream().anyMatch(m -> m.role() == role);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return Objects.equals(id, team.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Team{id=%s, name='%s'}".formatted(id, name);
    }
}
