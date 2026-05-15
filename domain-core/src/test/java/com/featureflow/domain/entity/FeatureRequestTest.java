package com.featureflow.domain.entity;

import com.featureflow.domain.valueobject.ClassOfService;
import com.featureflow.domain.valueobject.EffortEstimate;
import com.featureflow.domain.valueobject.ThreePointEstimate;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FeatureRequestTest {

    @Test
    void create_shouldInitializeDefaults() {
        var feature = new FeatureRequest(UUID.randomUUID(), "Test Feature", "Description", 10.0);

        assertThat(feature.getClassOfService()).isEqualTo(ClassOfService.STANDARD);
        assertThat(feature.getProductIds()).isEmpty();
        assertThat(feature.getDependencies()).isEmpty();
        assertThat(feature.getRequiredExpertise()).isEmpty();
        assertThat(feature.isCanSplit()).isFalse();
        assertThat(feature.getVersion()).isZero();
    }

    @Test
    void addProductId_shouldAddToSet() {
        var feature = new FeatureRequest(UUID.randomUUID(), "Test", "Desc", 5.0);
        UUID productId = UUID.randomUUID();

        feature.addProductId(productId);
        assertThat(feature.getProductIds()).contains(productId);
    }

    @Test
    void removeProductId_shouldRemoveFromSet() {
        var feature = new FeatureRequest(UUID.randomUUID(), "Test", "Desc", 5.0);
        UUID productId = UUID.randomUUID();
        feature.addProductId(productId);

        feature.removeProductId(productId);
        assertThat(feature.getProductIds()).doesNotContain(productId);
    }

    @Test
    void addDependency_shouldTrackDependency() {
        var feature = new FeatureRequest(UUID.randomUUID(), "Test", "Desc", 5.0);
        UUID depId = UUID.randomUUID();

        feature.addDependency(depId);
        assertThat(feature.dependsOn(depId)).isTrue();
    }

    @Test
    void hasDeadline_shouldCheckCorrectly() {
        var feature = new FeatureRequest(UUID.randomUUID(), "Test", "Desc", 5.0);
        assertThat(feature.hasDeadline()).isFalse();

        feature.setDeadline(LocalDate.of(2025, 6, 1));
        assertThat(feature.hasDeadline()).isTrue();
    }

    @Test
    void setStochasticEstimate_shouldAcceptNull() {
        var feature = new FeatureRequest(UUID.randomUUID(), "Test", "Desc", 5.0);
        feature.setStochasticEstimate(null);
        assertThat(feature.getStochasticEstimate()).isNull();

        feature.setStochasticEstimate(new ThreePointEstimate(10, 20, 30));
        assertThat(feature.getStochasticEstimate().expected()).isEqualTo(20.0);
    }

    @Test
    void equality_shouldBeBasedOnId() {
        UUID id = UUID.randomUUID();
        var f1 = new FeatureRequest(id, "Feature 1", "Desc", 10.0);
        var f2 = new FeatureRequest(id, "Feature 2", "Other", 20.0);

        assertThat(f1).isEqualTo(f2);
        assertThat(f1.hashCode()).isEqualTo(f2.hashCode());
    }

    @Test
    void setEffortEstimate_shouldUpdate() {
        var feature = new FeatureRequest(UUID.randomUUID(), "Test", "Desc", 5.0);
        var effort = new EffortEstimate(40, 24, 16, 8);
        feature.setEffortEstimate(effort);

        assertThat(feature.getEffortEstimate().totalHours()).isEqualTo(88);
    }
}
