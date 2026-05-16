package com.featureflow.data.service;

import com.featureflow.data.dto.CreateSprintRequest;
import com.featureflow.data.dto.SprintDto;
import com.featureflow.data.dto.UpdateSprintRequest;
import com.featureflow.data.entity.PlanningWindowEntity;
import com.featureflow.data.entity.SprintEntity;
import com.featureflow.data.mapper.EntityMapper;
import com.featureflow.data.repository.PlanningWindowRepository;
import com.featureflow.data.repository.SprintRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SprintServiceTest {

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private PlanningWindowRepository planningWindowRepository;

    @Mock
    private EntityMapper mapper;

    @InjectMocks
    private SprintService sprintService;

    private UUID sprintId;
    private UUID planningWindowId;
    private PlanningWindowEntity planningWindow;
    private SprintEntity sprintEntity;
    private SprintDto sprintDto;

    @BeforeEach
    void setUp() {
        sprintId = UUID.randomUUID();
        planningWindowId = UUID.randomUUID();

        planningWindow = new PlanningWindowEntity();
        planningWindow.setId(planningWindowId);
        planningWindow.setStartDate(LocalDate.of(2025, 1, 1));
        planningWindow.setEndDate(LocalDate.of(2025, 6, 30));

        sprintEntity = new SprintEntity();
        sprintEntity.setId(sprintId);
        sprintEntity.setPlanningWindow(planningWindow);
        sprintEntity.setStartDate(LocalDate.of(2025, 1, 1));
        sprintEntity.setEndDate(LocalDate.of(2025, 1, 14));
        sprintEntity.setCapacityOverrides(Map.of());
        sprintEntity.setExternalId("SPR-1");

        sprintDto = new SprintDto(
            sprintId,
            planningWindowId,
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 14),
            Map.of(),
            "SPR-1"
        );
    }

    @Test
    void listByPlanningWindow_shouldReturnSprints() {
        when(sprintRepository.findByPlanningWindowIdOrderByStartDate(planningWindowId))
            .thenReturn(List.of(sprintEntity));
        when(mapper.toSprintDto(sprintEntity)).thenReturn(sprintDto);

        List<SprintDto> result = sprintService.listByPlanningWindow(planningWindowId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(sprintId);
    }

    @Test
    void getById_shouldReturnSprint() {
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprintEntity));
        when(mapper.toSprintDto(sprintEntity)).thenReturn(sprintDto);

        SprintDto result = sprintService.getById(sprintId);

        assertThat(result.id()).isEqualTo(sprintId);
        assertThat(result.startDate()).isEqualTo(LocalDate.of(2025, 1, 1));
    }

    @Test
    void getById_notFound_shouldThrow() {
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sprintService.getById(sprintId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining(sprintId.toString());
    }

    @Test
    void create_shouldSaveAndReturnDto() {
        CreateSprintRequest request = new CreateSprintRequest(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 2, 14), Map.of(), "SPR-2");

        when(planningWindowRepository.findById(planningWindowId))
            .thenReturn(Optional.of(planningWindow));
        when(mapper.toSprintEntity(request, planningWindow)).thenReturn(sprintEntity);
        when(sprintRepository.save(sprintEntity)).thenReturn(sprintEntity);
        when(mapper.toSprintDto(sprintEntity)).thenReturn(sprintDto);

        SprintDto result = sprintService.create(planningWindowId, request);

        assertThat(result).isEqualTo(sprintDto);
        verify(sprintRepository).save(sprintEntity);
    }

    @Test
    void create_planningWindowNotFound_shouldThrow() {
        CreateSprintRequest request = new CreateSprintRequest(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 2, 14), Map.of(), "SPR-2");
        when(planningWindowRepository.findById(planningWindowId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sprintService.create(planningWindowId, request))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("PlanningWindow");
    }

    @Test
    void update_shouldModifyAndReturnDto() {
        UpdateSprintRequest request = new UpdateSprintRequest(null, LocalDate.of(2025, 1, 21), null, "SPR-1-UPDATED");

        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprintEntity));
        when(sprintRepository.save(sprintEntity)).thenReturn(sprintEntity);
        when(mapper.toSprintDto(sprintEntity)).thenReturn(sprintDto);

        SprintDto result = sprintService.update(sprintId, request);

        assertThat(result).isEqualTo(sprintDto);
        assertThat(sprintEntity.getEndDate()).isEqualTo(LocalDate.of(2025, 1, 21));
        assertThat(sprintEntity.getExternalId()).isEqualTo("SPR-1-UPDATED");
    }

    @Test
    void update_notFound_shouldThrow() {
        UpdateSprintRequest request = new UpdateSprintRequest(null, null, null, null);
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sprintService.update(sprintId, request))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining(sprintId.toString());
    }

    @Test
    void delete_shouldRemoveSprint() {
        when(sprintRepository.existsById(sprintId)).thenReturn(true);

        sprintService.delete(sprintId);

        verify(sprintRepository).deleteById(sprintId);
    }

    @Test
    void delete_notFound_shouldThrow() {
        when(sprintRepository.existsById(sprintId)).thenReturn(false);

        assertThatThrownBy(() -> sprintService.delete(sprintId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining(sprintId.toString());
    }
}
