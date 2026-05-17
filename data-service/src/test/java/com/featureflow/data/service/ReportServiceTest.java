package com.featureflow.data.service;

import com.featureflow.data.entity.FeatureEntity;
import com.featureflow.data.repository.FeatureRepository;
import com.featureflow.domain.valueobject.ClassOfService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private FeatureRepository featureRepo;

    @InjectMocks
    private ReportService reportService;

    @Test
    void exportFeaturesCsv_allFeatures() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        FeatureEntity f1 = createFeature(id1, "Feature A", 50.0, ClassOfService.STANDARD,
            Map.of("backendHours", 10.0, "frontendHours", 5.0, "qaHours", 3.0, "devopsHours", 1.0),
            LocalDate.of(2025, 3, 1));
        FeatureEntity f2 = createFeature(id2, "Feature B", 75.0, ClassOfService.FIXED_DATE,
            Map.of("backendHours", 20.0, "frontendHours", 10.0, "qaHours", 5.0, "devopsHours", 2.0),
            null);

        when(featureRepo.findAll()).thenReturn(List.of(f1, f2));

        byte[] csv = reportService.exportFeaturesCsv(null);
        String content = new String(csv);

        assertThat(content).contains("ID,Title,Business Value,Class of Service");
        assertThat(content).contains("Feature A");
        assertThat(content).contains("Feature B");
        assertThat(content).contains("STANDARD");
        assertThat(content).contains("FIXED_DATE");
        assertThat(content).contains("10.0");
    }

    @Test
    void exportFeaturesCsv_filteredByProduct() {
        UUID productId = UUID.randomUUID();
        FeatureEntity f1 = createFeature(UUID.randomUUID(), "Product Feature", 60.0, ClassOfService.STANDARD,
            Map.of("backendHours", 5.0, "frontendHours", 5.0, "qaHours", 2.0, "devopsHours", 1.0),
            null);

        when(featureRepo.findByProductId(productId)).thenReturn(List.of(f1));

        byte[] csv = reportService.exportFeaturesCsv(productId.toString());
        String content = new String(csv);

        assertThat(content).contains("Product Feature");
        assertThat(content).doesNotContain("Feature A");
    }

    @Test
    void exportFeaturesCsv_escapesCommasInTitle() {
        FeatureEntity f1 = createFeature(UUID.randomUUID(), "Feature, with comma", 50.0, ClassOfService.STANDARD,
            Map.of("backendHours", 10.0, "frontendHours", 5.0, "qaHours", 3.0, "devopsHours", 1.0),
            null);

        when(featureRepo.findAll()).thenReturn(List.of(f1));

        byte[] csv = reportService.exportFeaturesCsv(null);
        String content = new String(csv);

        assertThat(content).contains("\"Feature, with comma\"");
    }

    @Test
    void exportFeaturesCsv_emptyList() {
        when(featureRepo.findAll()).thenReturn(List.of());

        byte[] csv = reportService.exportFeaturesCsv(null);
        String content = new String(csv);

        assertThat(content).startsWith("ID,Title,Business Value,Class of Service");
        assertThat(content.split("\n")).hasSize(1); // header only
    }

    private FeatureEntity createFeature(UUID id, String title, Double businessValue,
                                         ClassOfService cos, Map<String, Double> effort, LocalDate deadline) {
        FeatureEntity entity = new FeatureEntity();
        entity.setId(id);
        entity.setTitle(title);
        entity.setBusinessValue(businessValue);
        entity.setClassOfService(cos);
        entity.setEffortEstimate(effort);
        entity.setDeadline(deadline);
        return entity;
    }
}
