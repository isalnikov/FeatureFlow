package com.featureflow.data.dto;

import com.featureflow.domain.valueobject.ClassOfService;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

public class UpdateFeatureRequest {
    private String title;
    private String description;
    private Double businessValue;
    private LocalDate deadline;
    private ClassOfService classOfService;
    private Map<String, Double> effortEstimate;
    private Map<String, Double> stochasticEstimate;
    private Set<String> requiredExpertise;
    private Boolean canSplit;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getBusinessValue() { return businessValue; }
    public void setBusinessValue(Double businessValue) { this.businessValue = businessValue; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public ClassOfService getClassOfService() { return classOfService; }
    public void setClassOfService(ClassOfService classOfService) { this.classOfService = classOfService; }
    public Map<String, Double> getEffortEstimate() { return effortEstimate; }
    public void setEffortEstimate(Map<String, Double> effortEstimate) { this.effortEstimate = effortEstimate; }
    public Map<String, Double> getStochasticEstimate() { return stochasticEstimate; }
    public void setStochasticEstimate(Map<String, Double> stochasticEstimate) { this.stochasticEstimate = stochasticEstimate; }
    public Set<String> getRequiredExpertise() { return requiredExpertise; }
    public void setRequiredExpertise(Set<String> requiredExpertise) { this.requiredExpertise = requiredExpertise; }
    public Boolean getCanSplit() { return canSplit; }
    public void setCanSplit(Boolean canSplit) { this.canSplit = canSplit; }
}
