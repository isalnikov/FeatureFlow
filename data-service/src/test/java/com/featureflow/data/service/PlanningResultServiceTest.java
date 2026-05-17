package com.featureflow.data.service;

import com.featureflow.data.dto.PlanningResultSummary;
import com.featureflow.data.dto.SavePlanningResultRequest;
import com.featureflow.data.entity.PlanningResultEntity;
import com.featureflow.data.repository.PlanningResultRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanningResultServiceTest {

    @Mock
    private PlanningResultRepository repository;

    @InjectMocks
    private PlanningResultService service;

    @Test
    void save_createsEntityAndSaves() {
        SavePlanningResultRequest request = new SavePlanningResultRequest(
            UUID.randomUUID(), "GREEDY", 100.0, 500, "{\"key\":\"value\"}"
        );

        service.save(request);

        ArgumentCaptor<PlanningResultEntity> captor = ArgumentCaptor.forClass(PlanningResultEntity.class);
        verify(repository).save(captor.capture());

        PlanningResultEntity entity = captor.getValue();
        assertThat(entity.getPlanningWindowId()).isEqualTo(request.planningWindowId());
        assertThat(entity.getAlgorithm()).isEqualTo("GREEDY");
        assertThat(entity.getTotalCost()).isEqualTo(100.0);
        assertThat(entity.getComputationTimeMs()).isEqualTo(500);
        assertThat(entity.getResultData()).isEqualTo("{\"key\":\"value\"}");
        assertThat(entity.getCreatedAt()).isNotNull();
    }

    @Test
    void listRecent_returnsSummaries() {
        UUID id = UUID.randomUUID();
        PlanningResultEntity entity = new PlanningResultEntity();
        entity.setId(id);
        entity.setAlgorithm("GREEDY");
        entity.setTotalCost(100.0);
        entity.setComputationTimeMs(500);
        entity.setCreatedAt(Instant.now());

        Page<PlanningResultEntity> page = new PageImpl<>(List.of(entity));
        when(repository.findRecent(eq("GREEDY"), any(Pageable.class))).thenReturn(page);

        List<PlanningResultSummary> result = service.listRecent("GREEDY", 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(id);
        assertThat(result.get(0).algorithm()).isEqualTo("GREEDY");
    }

    @Test
    void findById_existing_returnsEntity() {
        UUID id = UUID.randomUUID();
        PlanningResultEntity entity = new PlanningResultEntity();
        entity.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        Optional<PlanningResultEntity> result = service.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
    }

    @Test
    void findById_nonExisting_returnsEmpty() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<PlanningResultEntity> result = service.findById(id);

        assertThat(result).isEmpty();
    }

    @Test
    void delete_delegatesToRepository() {
        UUID id = UUID.randomUUID();

        service.delete(id);

        verify(repository).deleteById(id);
    }
}
