package com.featureflow.data.mapper;

import com.featureflow.data.dto.*;
import com.featureflow.data.entity.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class EntityMapper {

    public ProductDto toProductDto(ProductEntity entity) {
        ProductDto dto = new ProductDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setTechnologyStack(entity.getTechnologyStack());
        dto.setVersion(entity.getVersion());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        return dto;
    }

    public ProductEntity toProductEntity(CreateProductRequest request) {
        ProductEntity entity = new ProductEntity();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        if (request.getTechnologyStack() != null) {
            entity.setTechnologyStack(new HashSet<>(request.getTechnologyStack()));
        }
        return entity;
    }

    public TeamDto toTeamDto(TeamEntity entity) {
        TeamDto dto = new TeamDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setFocusFactor(entity.getFocusFactor());
        dto.setBugReservePercent(entity.getBugReservePercent());
        dto.setTechDebtReservePercent(entity.getTechDebtReservePercent());
        dto.setVelocity(entity.getVelocity());
        dto.setExpertiseTags(entity.getExpertiseTags());
        dto.setExternalId(entity.getExternalId());
        dto.setVersion(entity.getVersion());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        return dto;
    }

    public TeamEntity toTeamEntity(CreateTeamRequest request) {
        TeamEntity entity = new TeamEntity();
        entity.setName(request.getName());
        entity.setFocusFactor(request.getFocusFactor());
        entity.setBugReservePercent(request.getBugReservePercent());
        entity.setTechDebtReservePercent(request.getTechDebtReservePercent());
        if (request.getExpertiseTags() != null) {
            entity.setExpertiseTags(new HashSet<>(request.getExpertiseTags()));
        }
        entity.setExternalId(request.getExternalId());
        return entity;
    }

    public FeatureDto toFeatureDto(FeatureEntity entity) {
        FeatureDto dto = new FeatureDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setBusinessValue(entity.getBusinessValue());
        dto.setRequestorId(entity.getRequestorId());
        dto.setDeadline(entity.getDeadline());
        dto.setClassOfService(entity.getClassOfService());
        dto.setEffortEstimate(entity.getEffortEstimate());
        dto.setStochasticEstimate(entity.getStochasticEstimate());
        dto.setRequiredExpertise(entity.getRequiredExpertise());
        dto.setCanSplit(entity.getCanSplit());
        dto.setExternalId(entity.getExternalId());
        dto.setExternalSource(entity.getExternalSource());
        dto.setVersion(entity.getVersion());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        return dto;
    }

    public FeatureEntity toFeatureEntity(CreateFeatureRequest request) {
        FeatureEntity entity = new FeatureEntity();
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setBusinessValue(request.getBusinessValue());
        entity.setRequestorId(request.getRequestorId());
        entity.setDeadline(request.getDeadline());
        entity.setClassOfService(request.getClassOfService());
        entity.setEffortEstimate(request.getEffortEstimate());
        entity.setStochasticEstimate(request.getStochasticEstimate());
        entity.setRequiredExpertise(request.getRequiredExpertise());
        entity.setCanSplit(request.getCanSplit());
        entity.setExternalId(request.getExternalId());
        entity.setExternalSource(request.getExternalSource());
        return entity;
    }

    public AssignmentDto toAssignmentDto(AssignmentEntity entity) {
        AssignmentDto dto = new AssignmentDto();
        dto.setId(entity.getId());
        dto.setFeatureId(entity.getFeature().getId());
        dto.setTeamId(entity.getTeam().getId());
        dto.setSprintId(entity.getSprint().getId());
        dto.setAllocatedEffort(entity.getAllocatedEffort());
        dto.setStatus(entity.getStatus());
        dto.setVersion(entity.getVersion());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public AssignmentEntity toAssignmentEntity(CreateAssignmentRequest request,
                                                FeatureEntity feature,
                                                TeamEntity team,
                                                SprintEntity sprint) {
        AssignmentEntity entity = new AssignmentEntity();
        entity.setFeature(feature);
        entity.setTeam(team);
        entity.setSprint(sprint);
        entity.setAllocatedEffort(request.getAllocatedEffort());
        return entity;
    }

    public PlanningWindowDto toPlanningWindowDto(PlanningWindowEntity entity) {
        PlanningWindowDto dto = new PlanningWindowDto();
        dto.setId(entity.getId());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setVersion(entity.getVersion());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    public PlanningWindowEntity toPlanningWindowEntity(CreatePlanningWindowRequest request) {
        PlanningWindowEntity entity = new PlanningWindowEntity();
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        return entity;
    }

    public SprintDto toSprintDto(SprintEntity entity) {
        SprintDto dto = new SprintDto();
        dto.setId(entity.getId());
        dto.setPlanningWindowId(entity.getPlanningWindow().getId());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setCapacityOverrides(entity.getCapacityOverrides());
        dto.setExternalId(entity.getExternalId());
        return dto;
    }

    public SprintEntity toSprintEntity(CreateSprintRequest request, PlanningWindowEntity planningWindow) {
        SprintEntity entity = new SprintEntity();
        entity.setPlanningWindow(planningWindow);
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setCapacityOverrides(request.getCapacityOverrides());
        entity.setExternalId(request.getExternalId());
        return entity;
    }
}
