package com.featureflow.data.service;

import com.featureflow.data.dto.*;
import com.featureflow.data.entity.*;
import com.featureflow.data.mapper.EntityMapper;
import com.featureflow.data.repository.*;
import com.featureflow.domain.valueobject.AssignmentStatus;
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
class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private FeatureRepository featureRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private EntityMapper mapper;

    @InjectMocks
    private AssignmentService assignmentService;

    private UUID assignmentId;
    private AssignmentEntity assignmentEntity;
    private AssignmentDto assignmentDto;
    private FeatureEntity featureEntity;
    private TeamEntity teamEntity;
    private SprintEntity sprintEntity;

    @BeforeEach
    void setUp() {
        assignmentId = UUID.randomUUID();

        featureEntity = new FeatureEntity();
        featureEntity.setId(UUID.randomUUID());
        featureEntity.setTitle("Feature");

        teamEntity = new TeamEntity();
        teamEntity.setId(UUID.randomUUID());
        teamEntity.setName("Team");

        sprintEntity = new SprintEntity();
        sprintEntity.setId(UUID.randomUUID());

        assignmentEntity = new AssignmentEntity();
        assignmentEntity.setId(assignmentId);
        assignmentEntity.setFeature(featureEntity);
        assignmentEntity.setTeam(teamEntity);
        assignmentEntity.setSprint(sprintEntity);
        assignmentEntity.setStatus(AssignmentStatus.PLANNED);

        assignmentDto = new AssignmentDto();
        assignmentDto.setId(assignmentId);
        assignmentDto.setFeatureId(featureEntity.getId());
        assignmentDto.setTeamId(teamEntity.getId());
        assignmentDto.setSprintId(sprintEntity.getId());
        assignmentDto.setStatus(AssignmentStatus.PLANNED);
    }

    @Test
    void listAll_shouldReturnAssignments() {
        when(assignmentRepository.findAll()).thenReturn(List.of(assignmentEntity));
        when(mapper.toAssignmentDto(assignmentEntity)).thenReturn(assignmentDto);

        List<AssignmentDto> result = assignmentService.listAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(assignmentId);
    }

    @Test
    void findByTeamId_shouldReturnAssignments() {
        UUID teamId = teamEntity.getId();
        when(assignmentRepository.findByTeamId(teamId)).thenReturn(List.of(assignmentEntity));
        when(mapper.toAssignmentDto(assignmentEntity)).thenReturn(assignmentDto);

        List<AssignmentDto> result = assignmentService.findByTeamId(teamId);

        assertThat(result).hasSize(1);
    }

    @Test
    void findBySprintId_shouldReturnAssignments() {
        UUID sprintId = sprintEntity.getId();
        when(assignmentRepository.findBySprintId(sprintId)).thenReturn(List.of(assignmentEntity));
        when(mapper.toAssignmentDto(assignmentEntity)).thenReturn(assignmentDto);

        List<AssignmentDto> result = assignmentService.findBySprintId(sprintId);

        assertThat(result).hasSize(1);
    }

    @Test
    void findByStatus_shouldReturnAssignments() {
        when(assignmentRepository.findByStatus(AssignmentStatus.PLANNED)).thenReturn(List.of(assignmentEntity));
        when(mapper.toAssignmentDto(assignmentEntity)).thenReturn(assignmentDto);

        List<AssignmentDto> result = assignmentService.findByStatus(AssignmentStatus.PLANNED);

        assertThat(result).hasSize(1);
    }

    @Test
    void getById_shouldReturnAssignment() {
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignmentEntity));
        when(mapper.toAssignmentDto(assignmentEntity)).thenReturn(assignmentDto);

        AssignmentDto result = assignmentService.getById(assignmentId);

        assertThat(result.getId()).isEqualTo(assignmentId);
    }

    @Test
    void getById_notFound_shouldThrow() {
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assignmentService.getById(assignmentId))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void create_shouldSaveAndReturnDto() {
        CreateAssignmentRequest request = new CreateAssignmentRequest();
        request.setFeatureId(featureEntity.getId());
        request.setTeamId(teamEntity.getId());
        request.setSprintId(sprintEntity.getId());

        when(featureRepository.findById(request.getFeatureId())).thenReturn(Optional.of(featureEntity));
        when(teamRepository.findById(request.getTeamId())).thenReturn(Optional.of(teamEntity));
        when(sprintRepository.findById(request.getSprintId())).thenReturn(Optional.of(sprintEntity));
        when(mapper.toAssignmentEntity(request, featureEntity, teamEntity, sprintEntity)).thenReturn(assignmentEntity);
        when(assignmentRepository.save(assignmentEntity)).thenReturn(assignmentEntity);
        when(mapper.toAssignmentDto(assignmentEntity)).thenReturn(assignmentDto);

        AssignmentDto result = assignmentService.create(request);

        assertThat(result).isEqualTo(assignmentDto);
        verify(assignmentRepository).save(assignmentEntity);
    }

    @Test
    void create_featureNotFound_shouldThrow() {
        CreateAssignmentRequest request = new CreateAssignmentRequest();
        request.setFeatureId(UUID.randomUUID());
        when(featureRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assignmentService.create(request))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void update_shouldModifyAndReturnDto() {
        UpdateAssignmentRequest request = new UpdateAssignmentRequest();
        request.setAllocatedEffort(Map.of("backendHours", 40.0));

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignmentEntity));
        when(assignmentRepository.save(assignmentEntity)).thenReturn(assignmentEntity);
        when(mapper.toAssignmentDto(assignmentEntity)).thenReturn(assignmentDto);

        AssignmentDto result = assignmentService.update(assignmentId, request);

        assertThat(assignmentEntity.getAllocatedEffort()).containsEntry("backendHours", 40.0);
        assertThat(result).isEqualTo(assignmentDto);
    }

    @Test
    void delete_shouldRemoveAssignment() {
        when(assignmentRepository.existsById(assignmentId)).thenReturn(true);

        assignmentService.delete(assignmentId);

        verify(assignmentRepository).deleteById(assignmentId);
    }

    @Test
    void delete_notFound_shouldThrow() {
        when(assignmentRepository.existsById(assignmentId)).thenReturn(false);

        assertThatThrownBy(() -> assignmentService.delete(assignmentId))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void lock_shouldSetLockedStatus() {
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignmentEntity));
        when(assignmentRepository.save(assignmentEntity)).thenReturn(assignmentEntity);
        when(mapper.toAssignmentDto(assignmentEntity)).thenReturn(assignmentDto);

        AssignmentDto result = assignmentService.lock(assignmentId);

        assertThat(assignmentEntity.getStatus()).isEqualTo(AssignmentStatus.LOCKED);
    }

    @Test
    void unlock_shouldSetPlannedStatus() {
        assignmentEntity.setStatus(AssignmentStatus.LOCKED);
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignmentEntity));
        when(assignmentRepository.save(assignmentEntity)).thenReturn(assignmentEntity);
        when(mapper.toAssignmentDto(assignmentEntity)).thenReturn(assignmentDto);

        AssignmentDto result = assignmentService.unlock(assignmentId);

        assertThat(assignmentEntity.getStatus()).isEqualTo(AssignmentStatus.PLANNED);
    }

    @Test
    void bulkCreate_shouldSaveAllAndReturnDtos() {
        CreateAssignmentRequest req = new CreateAssignmentRequest();
        req.setFeatureId(featureEntity.getId());
        req.setTeamId(teamEntity.getId());
        req.setSprintId(sprintEntity.getId());

        when(featureRepository.findById(req.getFeatureId())).thenReturn(Optional.of(featureEntity));
        when(teamRepository.findById(req.getTeamId())).thenReturn(Optional.of(teamEntity));
        when(sprintRepository.findById(req.getSprintId())).thenReturn(Optional.of(sprintEntity));
        when(mapper.toAssignmentEntity(req, featureEntity, teamEntity, sprintEntity)).thenReturn(assignmentEntity);
        when(assignmentRepository.saveAll(any())).thenReturn(List.of(assignmentEntity));
        when(mapper.toAssignmentDto(assignmentEntity)).thenReturn(assignmentDto);

        List<AssignmentDto> result = assignmentService.bulkCreate(List.of(req));

        assertThat(result).hasSize(1);
        verify(assignmentRepository).saveAll(any());
    }
}
