package com.featureflow.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.featureflow.domain.valueobject.ClassOfService;
import org.hibernate.annotations.JdbcTypeCode;
import jakarta.persistence.*;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "features")
public class FeatureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "business_value", nullable = false)
    private Double businessValue;

    @Column(name = "requestor_id")
    private UUID requestorId;

    @Column
    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(name = "class_of_service", nullable = false)
    private ClassOfService classOfService = ClassOfService.STANDARD;

    @ManyToMany
    @JoinTable(
        name = "feature_products",
        joinColumns = @JoinColumn(name = "feature_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    @JsonIgnore
    private Set<ProductEntity> products = new HashSet<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "effort_estimate", columnDefinition = "jsonb")
    private Map<String, Double> effortEstimate = new LinkedHashMap<>(Map.of("backendHours", 0.0, "frontendHours", 0.0, "qaHours", 0.0, "devopsHours", 0.0));

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "stochastic_estimate", columnDefinition = "jsonb")
    private Map<String, Double> stochasticEstimate;

    @ManyToMany
    @JoinTable(
        name = "feature_dependencies",
        joinColumns = @JoinColumn(name = "feature_id"),
        inverseJoinColumns = @JoinColumn(name = "depends_on_id")
    )
    @JsonIgnore
    private Set<FeatureEntity> dependencies = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "feature_required_expertise", joinColumns = @JoinColumn(name = "feature_id"))
    @Column(name = "expertise_tag")
    private Set<String> requiredExpertise = new HashSet<>();

    @Column(name = "can_split", nullable = false)
    private Boolean canSplit = false;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "external_source")
    private String externalSource;

    @Version
    private Integer version;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getBusinessValue() { return businessValue; }
    public void setBusinessValue(Double businessValue) { this.businessValue = businessValue; }
    public UUID getRequestorId() { return requestorId; }
    public void setRequestorId(UUID requestorId) { this.requestorId = requestorId; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public ClassOfService getClassOfService() { return classOfService; }
    public void setClassOfService(ClassOfService classOfService) { this.classOfService = classOfService; }
    public Set<ProductEntity> getProducts() { return products; }
    public void setProducts(Set<ProductEntity> products) { this.products = products; }
    public Map<String, Double> getEffortEstimate() { return effortEstimate; }
    public void setEffortEstimate(Map<String, Double> effortEstimate) { this.effortEstimate = effortEstimate; }
    public Map<String, Double> getStochasticEstimate() { return stochasticEstimate; }
    public void setStochasticEstimate(Map<String, Double> stochasticEstimate) { this.stochasticEstimate = stochasticEstimate; }
    public Set<FeatureEntity> getDependencies() { return dependencies; }
    public void setDependencies(Set<FeatureEntity> dependencies) { this.dependencies = dependencies; }
    public Set<String> getRequiredExpertise() { return requiredExpertise; }
    public void setRequiredExpertise(Set<String> requiredExpertise) { this.requiredExpertise = requiredExpertise; }
    public Boolean getCanSplit() { return canSplit; }
    public void setCanSplit(Boolean canSplit) { this.canSplit = canSplit; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public String getExternalSource() { return externalSource; }
    public void setExternalSource(String externalSource) { this.externalSource = externalSource; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
