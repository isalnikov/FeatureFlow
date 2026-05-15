package com.featureflow.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "products")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    @CollectionTable(name = "product_tech_stack", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "technology")
    private Set<String> technologyStack = new HashSet<>();

    @ManyToMany(mappedBy = "products")
    @JsonIgnore
    private Set<TeamEntity> teams = new HashSet<>();

    @ManyToMany(mappedBy = "products")
    @JsonIgnore
    private Set<FeatureEntity> features = new HashSet<>();

    @Version
    private Integer version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

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
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Set<String> getTechnologyStack() { return technologyStack; }
    public void setTechnologyStack(Set<String> technologyStack) { this.technologyStack = technologyStack; }
    public Set<TeamEntity> getTeams() { return teams; }
    public void setTeams(Set<TeamEntity> teams) { this.teams = teams; }
    public Set<FeatureEntity> getFeatures() { return features; }
    public void setFeatures(Set<FeatureEntity> features) { this.features = features; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
