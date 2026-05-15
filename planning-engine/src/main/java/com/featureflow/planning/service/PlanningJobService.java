package com.featureflow.planning.service;

import com.featureflow.domain.planning.PlanningRequest;
import com.featureflow.domain.planning.PlanningResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PlanningJobService {

    private final ConcurrentHashMap<UUID, PlanningJob> jobs = new ConcurrentHashMap<>();

    @Async
    public UUID submitJob(PlanningRequest request) {
        UUID jobId = UUID.randomUUID();
        jobs.put(jobId, new PlanningJob(
            jobId,
            PlanningJobStatus.RUNNING,
            LocalDateTime.now(),
            null,
            null
        ));

        try {
            PlanningResult result = executePlanning(request);
            PlanningJob previous = jobs.get(jobId);
            jobs.put(jobId, new PlanningJob(
                jobId,
                PlanningJobStatus.COMPLETED,
                previous.submittedAt(),
                LocalDateTime.now(),
                null
            ));
        } catch (Exception e) {
            PlanningJob previous = jobs.get(jobId);
            jobs.put(jobId, new PlanningJob(
                jobId,
                PlanningJobStatus.FAILED,
                previous.submittedAt(),
                LocalDateTime.now(),
                e.getMessage()
            ));
        }

        return jobId;
    }

    public PlanningJob getJobStatus(UUID jobId) {
        PlanningJob job = jobs.get(jobId);
        if (job == null) {
            throw new IllegalArgumentException("Job not found: " + jobId);
        }
        return job;
    }

    private PlanningResult executePlanning(PlanningRequest request) {
        return new PlanningResult(
            java.util.List.of(),
            java.util.List.of(),
            java.util.Map.of(),
            java.util.Map.of(),
            0.0,
            0L,
            PlanningResult.PlanningAlgorithm.GREEDY
        );
    }

    public record PlanningJob(
        UUID id,
        PlanningJobStatus status,
        LocalDateTime submittedAt,
        LocalDateTime completedAt,
        String errorMessage
    ) {}

    public enum PlanningJobStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED
    }
}
