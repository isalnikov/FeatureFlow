package com.featureflow.data.service;

import com.featureflow.data.dto.*;
import com.featureflow.data.entity.*;
import com.featureflow.data.mapper.EntityMapper;
import com.featureflow.data.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanningWindowServiceTest {

    @Mock
    private PlanningWindowRepository planningWindowRepository;

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private EntityMapper mapper;

    @InjectMocks
    private PlanningWindowService planningWindowService;

    private UUID windowId;
    private PlanningWindowEntity windowEntity;
    private PlanningWindowDto windowDto;

    @BeforeEach
    void setUp() {
        windowId = UUID.randomUUID();

        windowEntity = new PlanningWindowEntity();
        windowEntity.setId(windowId);
        windowEntity.setStartDate(LocalDate.of(2025, 1, 1));
        windowEntity.setEndDate(LocalDate.of(2025, 6, 30));
        windowEntity.setVersion(1);

        windowDto = new PlanningWindowDto(
            windowId,
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 6, 30),
            1,
            null
        );
    }

    @Test
    void listAll_shouldReturnWindows() {
        when(planningWindowRepository.findAll()).thenReturn(List.of(windowEntity));
        when(mapper.toPlanningWindowDto(windowEntity)).thenReturn(windowDto);

        List<PlanningWindowDto> result = planningWindowService.listAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(windowId);
    }

    @Test
    void getById_shouldReturnWindow() {
        when(planningWindowRepository.findById(windowId)).thenReturn(Optional.of(windowEntity));
        when(mapper.toPlanningWindowDto(windowEntity)).thenReturn(windowDto);

        PlanningWindowDto result = planningWindowService.getById(windowId);

        assertThat(result.id()).isEqualTo(windowId);
    }

    @Test
    void getById_notFound_shouldThrow() {
        when(planningWindowRepository.findById(windowId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> planningWindowService.getById(windowId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining(windowId.toString());
    }

    @Test
    void create_shouldSaveAndReturnDto() {
        CreatePlanningWindowRequest request = new CreatePlanningWindowRequest(LocalDate.of(2025, 7, 1), LocalDate.of(2025, 12, 31));

        when(mapper.toPlanningWindowEntity(request)).thenReturn(windowEntity);
        when(planningWindowRepository.save(windowEntity)).thenReturn(windowEntity);
        when(mapper.toPlanningWindowDto(windowEntity)).thenReturn(windowDto);

        PlanningWindowDto result = planningWindowService.create(request);

        assertThat(result).isEqualTo(windowDto);
        verify(planningWindowRepository).save(windowEntity);
    }

    @Test
    void delete_shouldRemoveWindow() {
        when(planningWindowRepository.existsById(windowId)).thenReturn(true);

        planningWindowService.delete(windowId);

        verify(planningWindowRepository).deleteById(windowId);
    }

    @Test
    void delete_notFound_shouldThrow() {
        when(planningWindowRepository.existsById(windowId)).thenReturn(false);

        assertThatThrownBy(() -> planningWindowService.delete(windowId))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void addSprint_shouldSaveAndReturnSprintDto() {
        CreateSprintRequest request = new CreateSprintRequest(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 14), Map.of(), null);

        SprintEntity sprintEntity = new SprintEntity();
        sprintEntity.setId(UUID.randomUUID());
        sprintEntity.setStartDate(LocalDate.of(2025, 1, 1));
        sprintEntity.setEndDate(LocalDate.of(2025, 1, 14));

        SprintDto sprintDto = new SprintDto(
            sprintEntity.getId(),
            null,
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 14),
            null,
            null
        );

        when(planningWindowRepository.findById(windowId)).thenReturn(Optional.of(windowEntity));
        when(mapper.toSprintEntity(request, windowEntity)).thenReturn(sprintEntity);
        when(sprintRepository.save(sprintEntity)).thenReturn(sprintEntity);
        when(mapper.toSprintDto(sprintEntity)).thenReturn(sprintDto);

        SprintDto result = planningWindowService.addSprint(windowId, request);

        assertThat(result.id()).isEqualTo(sprintEntity.getId());
        verify(sprintRepository).save(sprintEntity);
    }

    @Test
    void addSprint_windowNotFound_shouldThrow() {
        CreateSprintRequest request = new CreateSprintRequest(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 14), Map.of(), null);
        when(planningWindowRepository.findById(windowId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> planningWindowService.addSprint(windowId, request))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void removeSprint_shouldDeleteSprint() {
        UUID sprintId = UUID.randomUUID();
        SprintEntity sprintEntity = new SprintEntity();
        sprintEntity.setId(sprintId);

        when(planningWindowRepository.existsById(windowId)).thenReturn(true);
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprintEntity));

        planningWindowService.removeSprint(windowId, sprintId);

        verify(sprintRepository).delete(sprintEntity);
    }

    @Test
    void removeSprint_windowNotFound_shouldThrow() {
        UUID sprintId = UUID.randomUUID();
        when(planningWindowRepository.existsById(windowId)).thenReturn(false);

        assertThatThrownBy(() -> planningWindowService.removeSprint(windowId, sprintId))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void removeSprint_sprintNotFound_shouldThrow() {
        UUID sprintId = UUID.randomUUID();
        when(planningWindowRepository.existsById(windowId)).thenReturn(true);
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> planningWindowService.removeSprint(windowId, sprintId))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getSprints_shouldReturnSprints() {
        SprintEntity sprintEntity = new SprintEntity();
        sprintEntity.setId(UUID.randomUUID());
        sprintEntity.setStartDate(LocalDate.of(2025, 1, 1));
        sprintEntity.setEndDate(LocalDate.of(2025, 1, 14));

        SprintDto sprintDto = new SprintDto(
            sprintEntity.getId(),
            null,
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 14),
            null,
            null
        );

        when(planningWindowRepository.existsById(windowId)).thenReturn(true);
        when(sprintRepository.findByPlanningWindowId(windowId)).thenReturn(List.of(sprintEntity));
        when(mapper.toSprintDto(sprintEntity)).thenReturn(sprintDto);

        List<SprintDto> result = planningWindowService.getSprints(windowId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(sprintEntity.getId());
    }

    @Test
    void getSprints_windowNotFound_shouldThrow() {
        when(planningWindowRepository.existsById(windowId)).thenReturn(false);

        assertThatThrownBy(() -> planningWindowService.getSprints(windowId))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
