package com.featureflow.data.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public class TeamDto {
    private UUID id;
    private String name;
    private Double focusFactor;
    private Double bugReservePercent;
    private Double techDebtReservePercent;
    private Double velocity;
    private Set<String> expertiseTags;
    private String externalId;
    private Integer version;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
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
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
