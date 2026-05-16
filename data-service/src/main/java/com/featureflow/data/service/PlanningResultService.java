package com.featureflow.data.service;

import com.featureflow.data.dto.PlanningResultSummary;
import com.featureflow.data.dto.SavePlanningResultRequest;
import com.featureflow.data.entity.PlanningResultEntity;
import com.featureflow.data.repository.PlanningResultRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PlanningResultService {

    private final PlanningResultRepository repository;

    public PlanningResultService(PlanningResultRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void save(SavePlanningResultRequest request) {
        PlanningResultEntity entity = new PlanningResultEntity();
        entity.setPlanningWindowId(request.planningWindowId());
        entity.setAlgorithm(request.algorithm());
        entity.setTotalCost(request.totalCost());
        entity.setComputationTimeMs(request.computationTimeMs());
        entity.setResultData(request.resultData());
        entity.setCreatedAt(Instant.now());
        repository.save(entity);
    }

    public List<PlanningResultSummary> listRecent(String algorithm, int limit) {
        return repository.findRecent(algorithm, PageRequest.of(0, limit))
            .stream()
            .map(this::toSummary)
            .toList();
    }

    public Optional<PlanningResultEntity> findById(UUID id) {
        return repository.findById(id);
    }

    @Transactional
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    private PlanningResultSummary toSummary(PlanningResultEntity entity) {
        return new PlanningResultSummary(
            entity.getId(),
            entity.getPlanningWindowId(),
            entity.getAlgorithm(),
            entity.getTotalCost(),
            entity.getComputationTimeMs(),
            entity.getCreatedAt()
        );
    }
}
