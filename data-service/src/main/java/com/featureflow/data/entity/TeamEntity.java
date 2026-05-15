package com.featureflow.data.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "teams")
public class TeamEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamMemberEntity> members = new ArrayList<>();

    @Column(name = "focus_factor")
    private Double focusFactor = 0.7;

    @Column(name = "bug_reserve_percent")
    private Double bugReservePercent = 0.20;

    @Column(name = "techdebt_reserve_percent")
    private Double techDebtReservePercent = 0.10;

    @Column
    private Double velocity;

    @ElementCollection
    @CollectionTable(name = "team_expertise", joinColumns = @JoinColumn(name = "team_id"))
    @Column(name = "expertise_tag")
    private Set<String> expertiseTags = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "product_teams",
        joinColumns = @JoinColumn(name = "team_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Set<ProductEntity> products = new HashSet<>();

    @Column(name = "external_id")
    private String externalId;

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
    public List<TeamMemberEntity> getMembers() { return members; }
    public void setMembers(List<TeamMemberEntity> members) { this.members = members; }
    public Double getFocusFactor() { return focusFactor; }
    public void setFocusFactor(Double focusFactor) { this.focusFactor = focusFactor; }
    public Double getBugReservePercent() { return bugReservePercent; }
    public void setBugReservePercent(Double bugReservePercent) { this.bugReservePercent = bugReservePercent; }
    public Double getTechDebtReservePercent() { return techDebtReservePercent; }
    public void setTechDebtReservePercent(Double techDebtReservePercent) { this.techDebtReservePercent = techDebtReservePercent; }
    public Double getVelocity() { return velocity; }
    public void setVelocity(Double velocity) { this.velocity = velocity; }
    public Set<String> getExpertiseTags() { return expertiseTags; }
    public void setExpertiseTags(Set<String> expertiseTags) { this.expertiseTags = expertiseTags; }
    public Set<ProductEntity> getProducts() { return products; }
    public void setProducts(Set<ProductEntity> products) { this.products = products; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
