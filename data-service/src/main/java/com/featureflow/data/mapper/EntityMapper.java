package com.featureflow.data.mapper;

import com.featureflow.data.dto.*;
import com.featureflow.data.entity.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class EntityMapper {

    public ProductDto toProductDto(ProductEntity entity) {
        return new ProductDto(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getTechnologyStack(),
            entity.getVersion(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getCreatedBy(),
            entity.getUpdatedBy()
        );
    }

    public ProductEntity toProductEntity(CreateProductRequest request) {
        ProductEntity entity = new ProductEntity();
        entity.setName(request.name());
        entity.setDescription(request.description());
        if (request.technologyStack() != null) {
            entity.setTechnologyStack(new HashSet<>(request.technologyStack()));
        }
        return entity;
    }

    public TeamDto toTeamDto(TeamEntity entity) {
        return new TeamDto(
            entity.getId(),
            entity.getName(),
            entity.getFocusFactor(),
            entity.getBugReservePercent(),
            entity.getTechDebtReservePercent(),
            entity.getVelocity(),
            entity.getExpertiseTags(),
            entity.getExternalId(),
            entity.getVersion(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getCreatedBy(),
            entity.getUpdatedBy()
        );
    }

    public TeamEntity toTeamEntity(CreateTeamRequest request) {
        TeamEntity entity = new TeamEntity();
        entity.setName(request.name());
        entity.setFocusFactor(request.focusFactor());
        entity.setBugReservePercent(request.bugReservePercent());
        entity.setTechDebtReservePercent(request.techDebtReservePercent());
        if (request.expertiseTags() != null) {
            entity.setExpertiseTags(new HashSet<>(request.expertiseTags()));
        }
        entity.setExternalId(request.externalId());
        return entity;
    }

    public FeatureDto toFeatureDto(FeatureEntity entity) {
        return new FeatureDto(
            entity.getId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getBusinessValue(),
            entity.getRequestorId(),
            entity.getDeadline(),
            entity.getClassOfService(),
            entity.getEffortEstimate(),
            entity.getStochasticEstimate(),
            entity.getRequiredExpertise(),
            entity.getCanSplit(),
            entity.getExternalId(),
            entity.getExternalSource(),
            entity.getVersion(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getCreatedBy(),
            entity.getUpdatedBy()
        );
    }

    public FeatureEntity toFeatureEntity(CreateFeatureRequest request) {
        FeatureEntity entity = new FeatureEntity();
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setBusinessValue(request.businessValue());
        entity.setRequestorId(request.requestorId());
        entity.setDeadline(request.deadline());
        entity.setClassOfService(request.classOfService());
        entity.setEffortEstimate(request.effortEstimate());
        entity.setStochasticEstimate(request.stochasticEstimate());
        entity.setRequiredExpertise(request.requiredExpertise());
        entity.setCanSplit(request.canSplit());
        entity.setExternalId(request.externalId());
        entity.setExternalSource(request.externalSource());
        return entity;
    }

    public AssignmentDto toAssignmentDto(AssignmentEntity entity) {
        return new AssignmentDto(
            entity.getId(),
            entity.getFeature().getId(),
            entity.getTeam().getId(),
            entity.getSprint().getId(),
            entity.getAllocatedEffort(),
            entity.getStatus(),
            entity.getVersion(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public AssignmentEntity toAssignmentEntity(CreateAssignmentRequest request,
                                                FeatureEntity feature,
                                                TeamEntity team,
                                                SprintEntity sprint) {
        AssignmentEntity entity = new AssignmentEntity();
        entity.setFeature(feature);
        entity.setTeam(team);
        entity.setSprint(sprint);
        entity.setAllocatedEffort(request.allocatedEffort());
        return entity;
    }

    public PlanningWindowDto toPlanningWindowDto(PlanningWindowEntity entity) {
        return new PlanningWindowDto(
            entity.getId(),
            entity.getStartDate(),
            entity.getEndDate(),
            entity.getVersion(),
            entity.getCreatedAt()
        );
    }

    public PlanningWindowEntity toPlanningWindowEntity(CreatePlanningWindowRequest request) {
        PlanningWindowEntity entity = new PlanningWindowEntity();
        entity.setStartDate(request.startDate());
        entity.setEndDate(request.endDate());
        return entity;
    }

    public SprintDto toSprintDto(SprintEntity entity) {
        return new SprintDto(
            entity.getId(),
            entity.getPlanningWindow().getId(),
            entity.getStartDate(),
            entity.getEndDate(),
            entity.getCapacityOverrides(),
            entity.getExternalId()
        );
    }

    public SprintEntity toSprintEntity(CreateSprintRequest request, PlanningWindowEntity planningWindow) {
        SprintEntity entity = new SprintEntity();
        entity.setPlanningWindow(planningWindow);
        entity.setStartDate(request.startDate());
        entity.setEndDate(request.endDate());
        entity.setCapacityOverrides(request.capacityOverrides());
        entity.setExternalId(request.externalId());
        return entity;
    }
}
