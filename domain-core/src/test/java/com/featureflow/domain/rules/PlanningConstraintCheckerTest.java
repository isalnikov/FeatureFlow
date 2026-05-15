package com.featureflow.domain.rules;

import com.featureflow.domain.entity.Assignment;
import com.featureflow.domain.entity.FeatureRequest;
import com.featureflow.domain.entity.Product;
import com.featureflow.domain.entity.Team;
import com.featureflow.domain.planning.ConstraintViolation;
import com.featureflow.domain.planning.PlanningParameters;
import com.featureflow.domain.planning.PlanningRequest;
import com.featureflow.domain.valueobject.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PlanningConstraintCheckerTest {

    @Test
    void checkAll_shouldDetectOwnershipViolation() {
        UUID productId = UUID.randomUUID();
        var feature = new FeatureRequest(UUID.randomUUID(), "Feature", "Desc", 10.0);
        feature.addProductId(productId);
        var product = new Product(productId, "Product", "Desc");
        var team = new Team(UUID.randomUUID(), "Team");
        var window = new com.featureflow.domain.entity.PlanningWindow(
            UUID.randomUUID(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31));
        var params = PlanningParameters.defaults();

        var request = new PlanningRequest(
            List.of(feature), List.of(team), List.of(product), window, params, Set.of()
        );

        var assignment = new Assignment(
            UUID.randomUUID(), feature.getId(), team.getId(), UUID.randomUUID()
        );

        var checker = new PlanningConstraintChecker();
        var violations = checker.checkAll(request, List.of(assignment));

        assertThat(violations).isNotEmpty();
    }

    @Test
    void checkAll_shouldPassWhenNoViolations() {
        UUID productId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();

        var product = new Product(productId, "Product", "Desc");
        product.addTeamId(teamId);

        var feature = new FeatureRequest(UUID.randomUUID(), "Feature", "Desc", 10.0);
        feature.addProductId(productId);

        var team = new Team(teamId, "Team");
        var window = new com.featureflow.domain.entity.PlanningWindow(
            UUID.randomUUID(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31));
        var params = PlanningParameters.defaults();

        var request = new PlanningRequest(
            List.of(feature), List.of(team), List.of(product), window, params, Set.of()
        );

        var assignment = new Assignment(
            UUID.randomUUID(), feature.getId(), team.getId(), UUID.randomUUID()
        );

        var checker = new PlanningConstraintChecker();
        var violations = checker.checkAll(request, List.of(assignment));

        assertThat(violations).isEmpty();
    }
}
