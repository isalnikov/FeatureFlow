package com.featureflow.planning.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.featureflow.domain.planning.PlanningRequest;
import com.featureflow.domain.planning.PlanningResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PlanningJobService {

    private static final Logger log = LoggerFactory.getLogger(PlanningJobService.class);

    private final PlanningOrchestrator orchestrator;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final String dataServiceUrl;
    private final ConcurrentHashMap<UUID, PlanningJob> jobs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();

    public PlanningJobService(PlanningOrchestrator orchestrator,
                              WebClient.Builder webClientBuilder,
                              ObjectMapper objectMapper,
                              @Value("${featureflow.data-service.url:http://localhost:8080}") String dataServiceUrl) {
        this.orchestrator = orchestrator;
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = objectMapper;
        this.dataServiceUrl = dataServiceUrl;
    }

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

        SseEmitter emitter = new SseEmitter(60_000L);
        emitters.put(jobId, emitter);

        try {
            emitter.send(SseEmitter.event().name("status").data("RUNNING"));

            PlanningResult result = orchestrator.runFullPipeline(request);

            PlanningJob previous = jobs.get(jobId);
            jobs.put(jobId, new PlanningJob(
                jobId,
                PlanningJobStatus.COMPLETED,
                previous.submittedAt(),
                LocalDateTime.now(),
                null,
                result
            ));

            storeResult(jobId, result);

            emitter.send(SseEmitter.event().name("status").data("COMPLETED"));
            emitter.send(SseEmitter.event().name("result").data(result));
            emitter.complete();
        } catch (Exception e) {
            PlanningJob previous = jobs.get(jobId);
            jobs.put(jobId, new PlanningJob(
                jobId,
                PlanningJobStatus.FAILED,
                previous.submittedAt(),
                LocalDateTime.now(),
                e.getMessage()
            ));

            try {
                emitter.send(SseEmitter.event().name("status").data("FAILED"));
                emitter.send(SseEmitter.event().name("error").data(e.getMessage()));
            } catch (IOException ex) {
                // emitter already completed
            }
            emitter.completeWithError(e);
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

    public PlanningResult getJobResult(UUID jobId) {
        PlanningJob job = jobs.get(jobId);
        if (job == null || job.status() != PlanningJobStatus.COMPLETED) {
            return null;
        }
        return job.result();
    }

    public SseEmitter streamStatus(UUID jobId) {
        if (!jobs.containsKey(jobId)) {
            throw new IllegalArgumentException("Job not found: " + jobId);
        }

        SseEmitter existing = emitters.get(jobId);
        if (existing != null) {
            return existing;
        }

        SseEmitter emitter = new SseEmitter(60_000L);
        PlanningJob job = jobs.get(jobId);
        try {
            emitter.send(SseEmitter.event().name("status").data(job.status().name()));
            if (job.status() == PlanningJobStatus.COMPLETED && job.result() != null) {
                emitter.send(SseEmitter.event().name("result").data(job.result()));
            }
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }

    private void storeResult(UUID jobId, PlanningResult result) {
        try {
            String resultJson = objectMapper.writeValueAsString(result);
            var payload = Map.of(
                "algorithm", result.algorithm().name(),
                "totalCost", result.totalCost(),
                "computationTimeMs", result.computationTimeMs(),
                "resultData", resultJson
            );
            webClientBuilder.build()
                .post()
                .uri(dataServiceUrl + "/api/v1/planning-results")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(10))
                .block();
        } catch (Exception e) {
            log.warn("Failed to store planning result: {}", e.getMessage());
        }
    }

    public record PlanningJob(
        UUID id,
        PlanningJobStatus status,
        LocalDateTime submittedAt,
        LocalDateTime completedAt,
        String errorMessage,
        PlanningResult result
    ) {
        public PlanningJob(UUID id, PlanningJobStatus status, LocalDateTime submittedAt,
                          LocalDateTime completedAt, String errorMessage) {
            this(id, status, submittedAt, completedAt, errorMessage, null);
        }
    }

    public enum PlanningJobStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED
    }
}
