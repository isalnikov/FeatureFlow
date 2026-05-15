package com.featureflow.domain.planning;

import com.featureflow.domain.entity.FeatureRequest;
import com.featureflow.domain.entity.PlanningWindow;
import com.featureflow.domain.entity.Product;
import com.featureflow.domain.entity.Team;
import com.featureflow.domain.valueobject.AnnealingParameters;
import com.featureflow.domain.valueobject.MonteCarloParameters;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlanningRequestTest {

    @Test
    void create_shouldSetFields() {
        var feature = new FeatureRequest(UUID.randomUUID(), "Feature", "Desc", 10.0);
        var product = new Product(UUID.randomUUID(), "Product", "Desc");
        var team = new Team(UUID.randomUUID(), "Team");
        var window = new PlanningWindow(UUID.randomUUID(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31));
        var params = new PlanningParameters(1.0, 0.5, 2.0, 3,
            AnnealingParameters.defaults(), MonteCarloParameters.defaults());

        var request = new PlanningRequest(
            List.of(feature),
            List.of(team),
            List.of(product),
            window,
            params,
            Set.of()
        );

        assertThat(request.features()).hasSize(1);
        assertThat(request.teams()).hasSize(1);
        assertThat(request.products()).hasSize(1);
        assertThat(request.planningWindow()).isEqualTo(window);
        assertThat(request.lockedAssignments()).isEmpty();
    }

    @Test
    void emptyFeatures_shouldThrow() {
        var product = new Product(UUID.randomUUID(), "Product", "Desc");
        var team = new Team(UUID.randomUUID(), "Team");
        var window = new PlanningWindow(UUID.randomUUID(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31));
        var params = new PlanningParameters(1.0, 0.5, 2.0, 3,
            AnnealingParameters.defaults(), MonteCarloParameters.defaults());

        assertThatThrownBy(() -> new PlanningRequest(
            List.of(), List.of(team), List.of(product), window, params, Set.of()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void emptyTeams_shouldThrow() {
        var feature = new FeatureRequest(UUID.randomUUID(), "Feature", "Desc", 10.0);
        var product = new Product(UUID.randomUUID(), "Product", "Desc");
        var window = new PlanningWindow(UUID.randomUUID(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31));
        var params = new PlanningParameters(1.0, 0.5, 2.0, 3,
            AnnealingParameters.defaults(), MonteCarloParameters.defaults());

        assertThatThrownBy(() -> new PlanningRequest(
            List.of(feature), List.of(), List.of(product), window, params, Set.of()
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
