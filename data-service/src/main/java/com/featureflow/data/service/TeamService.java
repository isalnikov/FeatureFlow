package com.featureflow.data.service;

import com.featureflow.data.dto.CreateTeamRequest;
import com.featureflow.data.dto.TeamDto;
import com.featureflow.data.dto.UpdateTeamRequest;
import com.featureflow.data.entity.AssignmentEntity;
import com.featureflow.data.entity.TeamEntity;
import com.featureflow.data.mapper.EntityMapper;
import com.featureflow.data.repository.AssignmentRepository;
import com.featureflow.data.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository teamRepository;
    private final AssignmentRepository assignmentRepository;
    private final EntityMapper mapper;

    public TeamService(TeamRepository teamRepository,
                       AssignmentRepository assignmentRepository,
                       EntityMapper mapper) {
        this.teamRepository = teamRepository;
        this.assignmentRepository = assignmentRepository;
        this.mapper = mapper;
    }

    public List<TeamDto> listAll() {
        return teamRepository.findAll().stream()
                .map(mapper::toTeamDto)
                .collect(Collectors.toList());
    }

    public TeamDto getById(UUID id) {
        TeamEntity entity = teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Team not found with id: " + id));
        return mapper.toTeamDto(entity);
    }

    @Transactional
    public TeamDto create(CreateTeamRequest request) {
        TeamEntity entity = mapper.toTeamEntity(request);
        entity = teamRepository.save(entity);
        return mapper.toTeamDto(entity);
    }

    @Transactional
    public TeamDto update(UUID id, UpdateTeamRequest request) {
        TeamEntity entity = teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Team not found with id: " + id));
        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getFocusFactor() != null) {
            entity.setFocusFactor(request.getFocusFactor());
        }
        if (request.getBugReservePercent() != null) {
            entity.setBugReservePercent(request.getBugReservePercent());
        }
        if (request.getTechDebtReservePercent() != null) {
            entity.setTechDebtReservePercent(request.getTechDebtReservePercent());
        }
        if (request.getVelocity() != null) {
            entity.setVelocity(request.getVelocity());
        }
        if (request.getExpertiseTags() != null) {
            entity.setExpertiseTags(request.getExpertiseTags());
        }
        entity = teamRepository.save(entity);
        return mapper.toTeamDto(entity);
    }

    @Transactional
    public void delete(UUID id) {
        if (!teamRepository.existsById(id)) {
            throw new EntityNotFoundException("Team not found with id: " + id);
        }
        teamRepository.deleteById(id);
    }

    public Map<String, Double> getCapacityForSprint(UUID teamId, UUID sprintId) {
        if (!teamRepository.existsById(teamId)) {
            throw new EntityNotFoundException("Team not found with id: " + teamId);
        }
        List<AssignmentEntity> assignments = assignmentRepository.findByTeamAndSprint(teamId, sprintId);
        Map<String, Double> capacity = new HashMap<>();
        capacity.put("backendHours", 0.0);
        capacity.put("frontendHours", 0.0);
        capacity.put("qaHours", 0.0);
        capacity.put("devopsHours", 0.0);
        for (AssignmentEntity assignment : assignments) {
            Map<String, Double> effort = assignment.getAllocatedEffort();
            if (effort != null) {
                for (Map.Entry<String, Double> entry : effort.entrySet()) {
                    capacity.merge(entry.getKey(), entry.getValue(), Double::sum);
                }
            }
        }
        return capacity;
    }

    public double getTeamLoad(UUID teamId, UUID sprintId) {
        if (!teamRepository.existsById(teamId)) {
            throw new EntityNotFoundException("Team not found with id: " + teamId);
        }
        List<AssignmentEntity> assignments = assignmentRepository.findByTeamAndSprint(teamId, sprintId);
        double totalLoad = 0.0;
        for (AssignmentEntity assignment : assignments) {
            Map<String, Double> effort = assignment.getAllocatedEffort();
            if (effort != null) {
                for (Double value : effort.values()) {
                    totalLoad += value;
                }
            }
        }
        return totalLoad;
    }
}
