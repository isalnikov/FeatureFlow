package com.featureflow.data.service;

import com.featureflow.data.dto.CreatePlanningWindowRequest;
import com.featureflow.data.dto.CreateSprintRequest;
import com.featureflow.data.dto.PlanningWindowDto;
import com.featureflow.data.dto.SprintDto;
import com.featureflow.data.entity.PlanningWindowEntity;
import com.featureflow.data.entity.SprintEntity;
import com.featureflow.data.mapper.EntityMapper;
import com.featureflow.data.repository.PlanningWindowRepository;
import com.featureflow.data.repository.SprintRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PlanningWindowService {

    private final PlanningWindowRepository planningWindowRepository;
    private final SprintRepository sprintRepository;
    private final EntityMapper mapper;

    public PlanningWindowService(PlanningWindowRepository planningWindowRepository,
                                 SprintRepository sprintRepository,
                                 EntityMapper mapper) {
        this.planningWindowRepository = planningWindowRepository;
        this.sprintRepository = sprintRepository;
        this.mapper = mapper;
    }

    public List<PlanningWindowDto> listAll() {
        return planningWindowRepository.findAll().stream()
                .map(mapper::toPlanningWindowDto)
                .collect(Collectors.toList());
    }

    public PlanningWindowDto getById(UUID id) {
        PlanningWindowEntity entity = planningWindowRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Planning window not found with id: " + id));
        return mapper.toPlanningWindowDto(entity);
    }

    @Transactional
    public PlanningWindowDto create(CreatePlanningWindowRequest request) {
        PlanningWindowEntity entity = mapper.toPlanningWindowEntity(request);
        entity = planningWindowRepository.save(entity);
        return mapper.toPlanningWindowDto(entity);
    }

    @Transactional
    public void delete(UUID id) {
        if (!planningWindowRepository.existsById(id)) {
            throw new EntityNotFoundException("Planning window not found with id: " + id);
        }
        planningWindowRepository.deleteById(id);
    }

    @Transactional
    public SprintDto addSprint(UUID windowId, CreateSprintRequest request) {
        PlanningWindowEntity window = planningWindowRepository.findById(windowId)
                .orElseThrow(() -> new EntityNotFoundException("Planning window not found with id: " + windowId));
        SprintEntity entity = mapper.toSprintEntity(request, window);
        entity = sprintRepository.save(entity);
        return mapper.toSprintDto(entity);
    }

    @Transactional
    public void removeSprint(UUID windowId, UUID sprintId) {
        if (!planningWindowRepository.existsById(windowId)) {
            throw new EntityNotFoundException("Planning window not found with id: " + windowId);
        }
        SprintEntity sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new EntityNotFoundException("Sprint not found with id: " + sprintId));
        sprintRepository.delete(sprint);
    }

    public List<SprintDto> getSprints(UUID windowId) {
        if (!planningWindowRepository.existsById(windowId)) {
            throw new EntityNotFoundException("Planning window not found with id: " + windowId);
        }
        return sprintRepository.findByPlanningWindowId(windowId).stream()
                .map(mapper::toSprintDto)
                .collect(Collectors.toList());
    }
}
