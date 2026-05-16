package com.featureflow.data.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "planning_results")
public class PlanningResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "planning_window_id")
    private UUID planningWindowId;

    @Column(nullable = false)
    private String algorithm;

    @Column(name = "total_cost", nullable = false)
    private double totalCost;

    @Column(name = "computation_time_ms", nullable = false)
    private long computationTimeMs;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result_data", nullable = false, columnDefinition = "jsonb")
    private String resultData;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public PlanningResultEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPlanningWindowId() { return planningWindowId; }
    public void setPlanningWindowId(UUID planningWindowId) { this.planningWindowId = planningWindowId; }

    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public long getComputationTimeMs() { return computationTimeMs; }
    public void setComputationTimeMs(long computationTimeMs) { this.computationTimeMs = computationTimeMs; }

    public String getResultData() { return resultData; }
    public void setResultData(String resultData) { this.resultData = resultData; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
