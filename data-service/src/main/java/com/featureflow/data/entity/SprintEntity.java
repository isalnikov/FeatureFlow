package com.featureflow.data.entity;

import org.hibernate.annotations.JdbcTypeCode;
import jakarta.persistence.*;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "sprints")
public class SprintEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planning_window_id", nullable = false)
    private PlanningWindowEntity planningWindow;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "capacity_overrides", columnDefinition = "jsonb")
    private Map<String, Object> capacityOverrides;

    @Column(name = "external_id")
    private String externalId;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public PlanningWindowEntity getPlanningWindow() { return planningWindow; }
    public void setPlanningWindow(PlanningWindowEntity planningWindow) { this.planningWindow = planningWindow; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Map<String, Object> getCapacityOverrides() { return capacityOverrides; }
    public void setCapacityOverrides(Map<String, Object> capacityOverrides) { this.capacityOverrides = capacityOverrides; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
}
