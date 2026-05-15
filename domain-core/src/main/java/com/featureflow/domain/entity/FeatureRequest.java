package com.featureflow.domain.entity;

import com.featureflow.domain.valueobject.ClassOfService;
import com.featureflow.domain.valueobject.EffortEstimate;
import com.featureflow.domain.valueobject.ThreePointEstimate;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class FeatureRequest {
    private final UUID id;
    private String title;
    private String description;
    private double businessValue;
    private UUID requestorId;
    private LocalDate deadline;
    private ClassOfService classOfService;
    private Set<UUID> productIds;
    private EffortEstimate effortEstimate;
    private ThreePointEstimate stochasticEstimate;
    private Set<UUID> dependencies;
    private Set<String> requiredExpertise;
    private boolean canSplit;
    private int version;

    public FeatureRequest(UUID id, String title, String description, double businessValue) {
        this.id = Objects.requireNonNull(id);
        this.title = Objects.requireNonNull(title);
        this.description = description;
        this.businessValue = businessValue;
        this.classOfService = ClassOfService.STANDARD;
        this.productIds = new HashSet<>();
        this.effortEstimate = new EffortEstimate();
        this.stochasticEstimate = null;
        this.dependencies = new HashSet<>();
        this.requiredExpertise = new HashSet<>();
        this.canSplit = false;
        this.version = 0;
    }

    public UUID getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = Objects.requireNonNull(title); }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getBusinessValue() { return businessValue; }
    public void setBusinessValue(double businessValue) { this.businessValue = businessValue; }

    public UUID getRequestorId() { return requestorId; }
    public void setRequestorId(UUID requestorId) { this.requestorId = requestorId; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public boolean hasDeadline() { return deadline != null; }

    public ClassOfService getClassOfService() { return classOfService; }
    public void setClassOfService(ClassOfService classOfService) {
        this.classOfService = Objects.requireNonNull(classOfService);
    }

    public Set<UUID> getProductIds() { return Collections.unmodifiableSet(productIds); }
    public void addProductId(UUID productId) { productIds.add(productId); }
    public void removeProductId(UUID productId) { productIds.remove(productId); }
    public void setProductIds(Set<UUID> productIds) { this.productIds = new HashSet<>(productIds); }

    public EffortEstimate getEffortEstimate() { return effortEstimate; }
    public void setEffortEstimate(EffortEstimate effortEstimate) {
        this.effortEstimate = Objects.requireNonNull(effortEstimate);
    }

    public ThreePointEstimate getStochasticEstimate() { return stochasticEstimate; }
    public void setStochasticEstimate(ThreePointEstimate stochasticEstimate) {
        this.stochasticEstimate = stochasticEstimate;
    }

    public Set<UUID> getDependencies() { return Collections.unmodifiableSet(dependencies); }
    public void addDependency(UUID featureId) { dependencies.add(featureId); }
    public void removeDependency(UUID featureId) { dependencies.remove(featureId); }
    public void setDependencies(Set<UUID> dependencies) { this.dependencies = new HashSet<>(dependencies); }

    public boolean dependsOn(UUID featureId) { return dependencies.contains(featureId); }

    public Set<String> getRequiredExpertise() { return Collections.unmodifiableSet(requiredExpertise); }
    public void addRequiredExpertise(String tag) { requiredExpertise.add(tag); }
    public void removeRequiredExpertise(String tag) { requiredExpertise.remove(tag); }
    public void setRequiredExpertise(Set<String> requiredExpertise) {
        this.requiredExpertise = new HashSet<>(requiredExpertise);
    }

    public boolean isCanSplit() { return canSplit; }
    public void setCanSplit(boolean canSplit) { this.canSplit = canSplit; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeatureRequest that = (FeatureRequest) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "FeatureRequest{id=%s, title='%s', value=%.1f}".formatted(id, title, businessValue);
    }
}
