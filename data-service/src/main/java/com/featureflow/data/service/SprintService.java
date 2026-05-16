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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SprintService {

    private final SprintRepository sprintRepository;
    private final PlanningWindowRepository planningWindowRepository;
    private final EntityMapper mapper;

    public SprintService(SprintRepository sprintRepository,
                         PlanningWindowRepository planningWindowRepository,
                         EntityMapper mapper) {
        this.sprintRepository = sprintRepository;
        this.planningWindowRepository = planningWindowRepository;
        this.mapper = mapper;
    }

    public List<SprintDto> listAll() {
        return sprintRepository.findAll().stream()
            .map(mapper::toSprintDto)
            .collect(Collectors.toList());
    }

    public List<SprintDto> listByPlanningWindow(UUID planningWindowId) {
        return sprintRepository.findByPlanningWindowIdOrderByStartDate(planningWindowId).stream()
            .map(mapper::toSprintDto)
            .collect(Collectors.toList());
    }

    public SprintDto getById(UUID id) {
        return sprintRepository.findById(id)
            .map(mapper::toSprintDto)
            .orElseThrow(() -> new EntityNotFoundException("Sprint not found with id: " + id));
    }

    @Transactional
    public SprintDto create(UUID planningWindowId, CreateSprintRequest request) {
        PlanningWindowEntity planningWindow = planningWindowRepository.findById(planningWindowId)
            .orElseThrow(() -> new EntityNotFoundException(
                "PlanningWindow not found with id: " + planningWindowId));

        SprintEntity entity = mapper.toSprintEntity(request, planningWindow);
        entity = sprintRepository.save(entity);
        return mapper.toSprintDto(entity);
    }

    @Transactional
    public SprintDto update(UUID id, UpdateSprintRequest request) {
        SprintEntity entity = sprintRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Sprint not found with id: " + id));

        if (request.startDate() != null) {
            entity.setStartDate(request.startDate());
        }
        if (request.endDate() != null) {
            entity.setEndDate(request.endDate());
        }
        if (request.capacityOverrides() != null) {
            entity.setCapacityOverrides(request.capacityOverrides());
        }
        if (request.externalId() != null) {
            entity.setExternalId(request.externalId());
        }

        entity = sprintRepository.save(entity);
        return mapper.toSprintDto(entity);
    }

    @Transactional
    public void delete(UUID id) {
        if (!sprintRepository.existsById(id)) {
            throw new EntityNotFoundException("Sprint not found with id: " + id);
        }
        sprintRepository.deleteById(id);
    }
}
