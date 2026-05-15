package com.featureflow.data.dto;

import java.util.Set;

public class UpdateTeamRequest {
    private String name;
    private Double focusFactor;
    private Double bugReservePercent;
    private Double techDebtReservePercent;
    private Double velocity;
    private Set<String> expertiseTags;

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
}
