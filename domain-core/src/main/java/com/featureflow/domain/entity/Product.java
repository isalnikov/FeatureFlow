package com.featureflow.domain.entity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class Product {
    private final UUID id;
    private String name;
    private String description;
    private Set<String> technologyStack;
    private Set<UUID> teamIds;

    public Product(UUID id, String name, String description) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.description = description;
        this.technologyStack = new HashSet<>();
        this.teamIds = new HashSet<>();
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = Objects.requireNonNull(name); }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Set<String> getTechnologyStack() { return Collections.unmodifiableSet(technologyStack); }

    public void addTechnology(String tech) { technologyStack.add(tech); }
    public void removeTechnology(String tech) { technologyStack.remove(tech); }
    public void setTechnologyStack(Set<String> techStack) {
        this.technologyStack = new HashSet<>(techStack);
    }

    public Set<UUID> getTeamIds() { return Collections.unmodifiableSet(teamIds); }

    public void addTeamId(UUID teamId) { teamIds.add(teamId); }
    public void removeTeamId(UUID teamId) { teamIds.remove(teamId); }
    public void setTeamIds(Set<UUID> teamIds) { this.teamIds = new HashSet<>(teamIds); }

    public boolean isOwnedBy(UUID teamId) { return teamIds.contains(teamId); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Product{id=%s, name='%s'}".formatted(id, name);
    }
}
