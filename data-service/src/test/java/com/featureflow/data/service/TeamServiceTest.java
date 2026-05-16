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

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private EntityMapper mapper;

    @InjectMocks
    private TeamService teamService;

    private UUID teamId;
    private TeamEntity teamEntity;
    private TeamDto teamDto;

    @BeforeEach
    void setUp() {
        teamId = UUID.randomUUID();

        teamEntity = new TeamEntity();
        teamEntity.setId(teamId);
        teamEntity.setName("Alpha Team");
        teamEntity.setFocusFactor(0.7);
        teamEntity.setBugReservePercent(0.20);
        teamEntity.setTechDebtReservePercent(0.10);
        teamEntity.setVelocity(50.0);
        teamEntity.setExpertiseTags(new HashSet<>(List.of("java", "react")));
        teamEntity.setVersion(1);

        teamDto = new TeamDto(
            teamId,
            "Alpha Team",
            0.7,
            0.20,
            0.10,
            50.0,
            new HashSet<>(List.of("java", "react")),
            null,
            1,
            null, null, null, null
        );
    }

    @Test
    void listAll_shouldReturnTeams() {
        when(teamRepository.findAll()).thenReturn(List.of(teamEntity));
        when(mapper.toTeamDto(teamEntity)).thenReturn(teamDto);

        List<TeamDto> result = teamService.listAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Alpha Team");
    }

    @Test
    void getById_shouldReturnTeam() {
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(teamEntity));
        when(mapper.toTeamDto(teamEntity)).thenReturn(teamDto);

        TeamDto result = teamService.getById(teamId);

        assertThat(result.id()).isEqualTo(teamId);
    }

    @Test
    void getById_notFound_shouldThrow() {
        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.getById(teamId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining(teamId.toString());
    }

    @Test
    void create_shouldSaveAndReturnDto() {
        CreateTeamRequest request = new CreateTeamRequest("New Team", 0.8, null, null, null, null);

        when(mapper.toTeamEntity(request)).thenReturn(teamEntity);
        when(teamRepository.save(teamEntity)).thenReturn(teamEntity);
        when(mapper.toTeamDto(teamEntity)).thenReturn(teamDto);

        TeamDto result = teamService.create(request);

        assertThat(result).isEqualTo(teamDto);
        verify(teamRepository).save(teamEntity);
    }

    @Test
    void update_shouldModifyAndReturnDto() {
        UpdateTeamRequest request = new UpdateTeamRequest("Updated Team", 0.85, null, null, null, null);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(teamEntity));
        when(teamRepository.save(teamEntity)).thenReturn(teamEntity);
        when(mapper.toTeamDto(teamEntity)).thenReturn(teamDto);

        TeamDto result = teamService.update(teamId, request);

        assertThat(teamEntity.getName()).isEqualTo("Updated Team");
        assertThat(teamEntity.getFocusFactor()).isEqualTo(0.85);
        assertThat(result).isEqualTo(teamDto);
    }

    @Test
    void update_notFound_shouldThrow() {
        UpdateTeamRequest request = new UpdateTeamRequest(null, null, null, null, null, null);
        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.update(teamId, request))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void delete_shouldRemoveTeam() {
        when(teamRepository.existsById(teamId)).thenReturn(true);

        teamService.delete(teamId);

        verify(teamRepository).deleteById(teamId);
    }

    @Test
    void delete_notFound_shouldThrow() {
        when(teamRepository.existsById(teamId)).thenReturn(false);

        assertThatThrownBy(() -> teamService.delete(teamId))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getCapacityForSprint_shouldReturnCapacity() {
        UUID sprintId = UUID.randomUUID();
        when(teamRepository.existsById(teamId)).thenReturn(true);
        when(assignmentRepository.findByTeamAndSprint(teamId, sprintId)).thenReturn(List.of());

        Map<String, Double> result = teamService.getCapacityForSprint(teamId, sprintId);

        assertThat(result).containsKeys("backendHours", "frontendHours", "qaHours", "devopsHours");
        assertThat(result.values()).allMatch(v -> v == 0.0);
    }

    @Test
    void getCapacityForSprint_teamNotFound_shouldThrow() {
        UUID sprintId = UUID.randomUUID();
        when(teamRepository.existsById(teamId)).thenReturn(false);

        assertThatThrownBy(() -> teamService.getCapacityForSprint(teamId, sprintId))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getTeamLoad_shouldReturnLoad() {
        UUID sprintId = UUID.randomUUID();
        when(teamRepository.existsById(teamId)).thenReturn(true);
        when(assignmentRepository.findByTeamAndSprint(teamId, sprintId)).thenReturn(List.of());

        double result = teamService.getTeamLoad(teamId, sprintId);

        assertThat(result).isEqualTo(0.0);
    }

    @Test
    void getTeamLoad_teamNotFound_shouldThrow() {
        UUID sprintId = UUID.randomUUID();
        when(teamRepository.existsById(teamId)).thenReturn(false);

        assertThatThrownBy(() -> teamService.getTeamLoad(teamId, sprintId))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
