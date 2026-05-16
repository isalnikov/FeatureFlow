package com.featureflow.data.service;

import com.featureflow.data.dto.AssignmentDto;
import com.featureflow.data.dto.CreateAssignmentRequest;
import com.featureflow.data.dto.UpdateAssignmentRequest;
import com.featureflow.data.entity.AssignmentEntity;
import com.featureflow.data.entity.FeatureEntity;
import com.featureflow.data.entity.SprintEntity;
import com.featureflow.data.entity.TeamEntity;
import com.featureflow.data.mapper.EntityMapper;
import com.featureflow.data.repository.AssignmentRepository;
import com.featureflow.data.repository.FeatureRepository;
import com.featureflow.data.repository.SprintRepository;
import com.featureflow.data.repository.TeamRepository;
import com.featureflow.domain.valueobject.AssignmentStatus;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final FeatureRepository featureRepository;
    private final TeamRepository teamRepository;
    private final SprintRepository sprintRepository;
    private final EntityMapper mapper;

    public AssignmentService(AssignmentRepository assignmentRepository,
                             FeatureRepository featureRepository,
                             TeamRepository teamRepository,
                             SprintRepository sprintRepository,
                             EntityMapper mapper) {
        this.assignmentRepository = assignmentRepository;
        this.featureRepository = featureRepository;
        this.teamRepository = teamRepository;
        this.sprintRepository = sprintRepository;
        this.mapper = mapper;
    }

    public List<AssignmentDto> listAll() {
        return assignmentRepository.findAll().stream()
                .map(mapper::toAssignmentDto)
                .collect(Collectors.toList());
    }

    public List<AssignmentDto> findByTeamId(UUID teamId) {
        return assignmentRepository.findByTeamId(teamId).stream()
                .map(mapper::toAssignmentDto)
                .collect(Collectors.toList());
    }

    public List<AssignmentDto> findBySprintId(UUID sprintId) {
        return assignmentRepository.findBySprintId(sprintId).stream()
                .map(mapper::toAssignmentDto)
                .collect(Collectors.toList());
    }

    public List<AssignmentDto> findByStatus(AssignmentStatus status) {
        return assignmentRepository.findByStatus(status).stream()
                .map(mapper::toAssignmentDto)
                .collect(Collectors.toList());
    }

    public AssignmentDto getById(UUID id) {
        AssignmentEntity entity = assignmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found with id: " + id));
        return mapper.toAssignmentDto(entity);
    }

    @Transactional
    public AssignmentDto create(CreateAssignmentRequest request) {
        FeatureEntity feature = featureRepository.findById(request.featureId())
                .orElseThrow(() -> new EntityNotFoundException("Feature not found with id: " + request.featureId()));
        TeamEntity team = teamRepository.findById(request.teamId())
                .orElseThrow(() -> new EntityNotFoundException("Team not found with id: " + request.teamId()));
        SprintEntity sprint = sprintRepository.findById(request.sprintId())
                .orElseThrow(() -> new EntityNotFoundException("Sprint not found with id: " + request.sprintId()));
        AssignmentEntity entity = mapper.toAssignmentEntity(request, feature, team, sprint);
        entity = assignmentRepository.save(entity);
        return mapper.toAssignmentDto(entity);
    }

    @Transactional
    public AssignmentDto update(UUID id, UpdateAssignmentRequest request) {
        AssignmentEntity entity = assignmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found with id: " + id));
        if (request.allocatedEffort() != null) {
            entity.setAllocatedEffort(request.allocatedEffort());
        }
        entity = assignmentRepository.save(entity);
        return mapper.toAssignmentDto(entity);
    }

    @Transactional
    public void delete(UUID id) {
        if (!assignmentRepository.existsById(id)) {
            throw new EntityNotFoundException("Assignment not found with id: " + id);
        }
        assignmentRepository.deleteById(id);
    }

    @Transactional
    public AssignmentDto lock(UUID id) {
        AssignmentEntity entity = assignmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found with id: " + id));
        entity.setStatus(AssignmentStatus.LOCKED);
        entity = assignmentRepository.save(entity);
        return mapper.toAssignmentDto(entity);
    }

    @Transactional
    public AssignmentDto unlock(UUID id) {
        AssignmentEntity entity = assignmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found with id: " + id));
        entity.setStatus(AssignmentStatus.PLANNED);
        entity = assignmentRepository.save(entity);
        return mapper.toAssignmentDto(entity);
    }

    @Transactional
    public List<AssignmentDto> bulkCreate(List<CreateAssignmentRequest> requests) {
        List<AssignmentEntity> entities = requests.stream()
                .map(request -> {
                    FeatureEntity feature = featureRepository.findById(request.featureId())
                            .orElseThrow(() -> new EntityNotFoundException("Feature not found with id: " + request.featureId()));
                    TeamEntity team = teamRepository.findById(request.teamId())
                            .orElseThrow(() -> new EntityNotFoundException("Team not found with id: " + request.teamId()));
                    SprintEntity sprint = sprintRepository.findById(request.sprintId())
                            .orElseThrow(() -> new EntityNotFoundException("Sprint not found with id: " + request.sprintId()));
                    return mapper.toAssignmentEntity(request, feature, team, sprint);
                })
                .collect(Collectors.toList());
        entities = assignmentRepository.saveAll(entities);
        return entities.stream()
                .map(mapper::toAssignmentDto)
                .collect(Collectors.toList());
    }
}
