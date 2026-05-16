package com.featureflow.data.mapper;

import com.featureflow.data.dto.*;
import com.featureflow.data.entity.*;
import com.featureflow.domain.valueobject.AssignmentStatus;
import com.featureflow.domain.valueobject.ClassOfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class EntityMapperTest {

    private EntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new EntityMapper();
    }

    @Test
    void toProductDto_shouldMapEntityToDto() {
        ProductEntity entity = new ProductEntity();
        entity.setId(UUID.randomUUID());
        entity.setName("Test Product");
        entity.setDescription("Description");
        entity.setTechnologyStack(Set.of("java", "spring"));
        entity.setVersion(1);

        ProductDto dto = mapper.toProductDto(entity);

        assertThat(dto.id()).isEqualTo(entity.getId());
        assertThat(dto.name()).isEqualTo("Test Product");
        assertThat(dto.description()).isEqualTo("Description");
        assertThat(dto.technologyStack()).containsExactlyInAnyOrder("java", "spring");
        assertThat(dto.version()).isEqualTo(1);
    }

    @Test
    void toProductEntity_shouldMapRequestToEntity() {
        CreateProductRequest request = new CreateProductRequest("New Product", "Description", Set.of("java"));

        ProductEntity entity = mapper.toProductEntity(request);

        assertThat(entity.getName()).isEqualTo("New Product");
        assertThat(entity.getDescription()).isEqualTo("Description");
        assertThat(entity.getTechnologyStack()).containsExactly("java");
    }

    @Test
    void toProductEntity_withNullTechStack_shouldNotSet() {
        CreateProductRequest request = new CreateProductRequest("New Product", "Description", null);

        ProductEntity entity = mapper.toProductEntity(request);

        assertThat(entity.getTechnologyStack()).isEmpty();
    }

    @Test
    void toTeamDto_shouldMapEntityToDto() {
        TeamEntity entity = new TeamEntity();
        entity.setId(UUID.randomUUID());
        entity.setName("Team Alpha");
        entity.setFocusFactor(0.7);
        entity.setBugReservePercent(0.2);
        entity.setTechDebtReservePercent(0.1);
        entity.setVelocity(50.0);
        entity.setExpertiseTags(Set.of("java"));
        entity.setExternalId("ext-1");
        entity.setVersion(1);

        TeamDto dto = mapper.toTeamDto(entity);

        assertThat(dto.id()).isEqualTo(entity.getId());
        assertThat(dto.name()).isEqualTo("Team Alpha");
        assertThat(dto.focusFactor()).isEqualTo(0.7);
        assertThat(dto.velocity()).isEqualTo(50.0);
    }

    @Test
    void toTeamEntity_shouldMapRequestToEntity() {
        CreateTeamRequest request = new CreateTeamRequest("New Team", 0.8, 0.15, 0.1, Set.of("java"), "ext-1");

        TeamEntity entity = mapper.toTeamEntity(request);

        assertThat(entity.getName()).isEqualTo("New Team");
        assertThat(entity.getFocusFactor()).isEqualTo(0.8);
        assertThat(entity.getExternalId()).isEqualTo("ext-1");
    }

    @Test
    void toFeatureDto_shouldMapEntityToDto() {
        FeatureEntity entity = new FeatureEntity();
        entity.setId(UUID.randomUUID());
        entity.setTitle("Test Feature");
        entity.setDescription("Description");
        entity.setBusinessValue(50.0);
        entity.setRequestorId(UUID.randomUUID());
        entity.setDeadline(LocalDate.of(2025, 6, 1));
        entity.setClassOfService(ClassOfService.STANDARD);
        entity.setCanSplit(false);
        entity.setExternalId("ext-1");
        entity.setVersion(1);

        FeatureDto dto = mapper.toFeatureDto(entity);

        assertThat(dto.id()).isEqualTo(entity.getId());
        assertThat(dto.title()).isEqualTo("Test Feature");
        assertThat(dto.businessValue()).isEqualTo(50.0);
        assertThat(dto.classOfService()).isEqualTo(ClassOfService.STANDARD);
    }

    @Test
    void toFeatureEntity_shouldMapRequestToEntity() {
        CreateFeatureRequest request = new CreateFeatureRequest(
            "New Feature", "Description", 50.0, UUID.randomUUID(),
            LocalDate.of(2025, 6, 1), ClassOfService.STANDARD,
            null, Map.of("backendHours", 40.0), null, Set.of("java"),
            false, "ext-1", "jira"
        );

        FeatureEntity entity = mapper.toFeatureEntity(request);

        assertThat(entity.getTitle()).isEqualTo("New Feature");
        assertThat(entity.getBusinessValue()).isEqualTo(50.0);
        assertThat(entity.getClassOfService()).isEqualTo(ClassOfService.STANDARD);
    }

    @Test
    void toAssignmentDto_shouldMapEntityToDto() {
        FeatureEntity feature = new FeatureEntity();
        feature.setId(UUID.randomUUID());
        TeamEntity team = new TeamEntity();
        team.setId(UUID.randomUUID());
        SprintEntity sprint = new SprintEntity();
        sprint.setId(UUID.randomUUID());

        AssignmentEntity entity = new AssignmentEntity();
        entity.setId(UUID.randomUUID());
        entity.setFeature(feature);
        entity.setTeam(team);
        entity.setSprint(sprint);
        entity.setAllocatedEffort(Map.of("backendHours", 40.0));
        entity.setStatus(AssignmentStatus.PLANNED);
        entity.setVersion(1);

        AssignmentDto dto = mapper.toAssignmentDto(entity);

        assertThat(dto.id()).isEqualTo(entity.getId());
        assertThat(dto.featureId()).isEqualTo(feature.getId());
        assertThat(dto.teamId()).isEqualTo(team.getId());
        assertThat(dto.sprintId()).isEqualTo(sprint.getId());
        assertThat(dto.status()).isEqualTo(AssignmentStatus.PLANNED);
    }

    @Test
    void toAssignmentEntity_shouldMapRequestToEntity() {
        FeatureEntity feature = new FeatureEntity();
        feature.setId(UUID.randomUUID());
        TeamEntity team = new TeamEntity();
        team.setId(UUID.randomUUID());
        SprintEntity sprint = new SprintEntity();
        sprint.setId(UUID.randomUUID());

        CreateAssignmentRequest request = new CreateAssignmentRequest(
            feature.getId(), team.getId(), sprint.getId(), Map.of("backendHours", 40.0)
        );

        AssignmentEntity entity = mapper.toAssignmentEntity(request, feature, team, sprint);

        assertThat(entity.getFeature()).isEqualTo(feature);
        assertThat(entity.getTeam()).isEqualTo(team);
        assertThat(entity.getSprint()).isEqualTo(sprint);
        assertThat(entity.getAllocatedEffort()).containsEntry("backendHours", 40.0);
    }

    @Test
    void toPlanningWindowDto_shouldMapEntityToDto() {
        PlanningWindowEntity entity = new PlanningWindowEntity();
        entity.setId(UUID.randomUUID());
        entity.setStartDate(LocalDate.of(2025, 1, 1));
        entity.setEndDate(LocalDate.of(2025, 6, 30));
        entity.setVersion(1);

        PlanningWindowDto dto = mapper.toPlanningWindowDto(entity);

        assertThat(dto.id()).isEqualTo(entity.getId());
        assertThat(dto.startDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(dto.endDate()).isEqualTo(LocalDate.of(2025, 6, 30));
    }

    @Test
    void toPlanningWindowEntity_shouldMapRequestToEntity() {
        CreatePlanningWindowRequest request = new CreatePlanningWindowRequest(
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30)
        );

        PlanningWindowEntity entity = mapper.toPlanningWindowEntity(request);

        assertThat(entity.getStartDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(entity.getEndDate()).isEqualTo(LocalDate.of(2025, 6, 30));
    }

    @Test
    void toSprintDto_shouldMapEntityToDto() {
        PlanningWindowEntity window = new PlanningWindowEntity();
        window.setId(UUID.randomUUID());

        SprintEntity entity = new SprintEntity();
        entity.setId(UUID.randomUUID());
        entity.setPlanningWindow(window);
        entity.setStartDate(LocalDate.of(2025, 1, 1));
        entity.setEndDate(LocalDate.of(2025, 1, 14));
        entity.setCapacityOverrides(Map.of());
        entity.setExternalId("SPR-1");

        SprintDto dto = mapper.toSprintDto(entity);

        assertThat(dto.id()).isEqualTo(entity.getId());
        assertThat(dto.planningWindowId()).isEqualTo(window.getId());
        assertThat(dto.startDate()).isEqualTo(LocalDate.of(2025, 1, 1));
    }

    @Test
    void toSprintEntity_shouldMapRequestToEntity() {
        PlanningWindowEntity window = new PlanningWindowEntity();
        window.setId(UUID.randomUUID());

        CreateSprintRequest request = new CreateSprintRequest(
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 14), Map.of(), "SPR-1"
        );

        SprintEntity entity = mapper.toSprintEntity(request, window);

        assertThat(entity.getPlanningWindow()).isEqualTo(window);
        assertThat(entity.getStartDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(entity.getExternalId()).isEqualTo("SPR-1");
    }
}
