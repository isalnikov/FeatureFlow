package com.featureflow.domain.rules;

import com.featureflow.domain.entity.FeatureRequest;
import com.featureflow.domain.entity.Product;
import com.featureflow.domain.entity.Team;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OwnershipValidatorTest {

    @Test
    void teamOwnsProducts_shouldReturnTrueWhenTeamOwnsAllProducts() {
        UUID productId = uuid("p1");
        UUID teamId = uuid("t1");

        var product = new Product(productId, "Product", "Desc");
        product.addTeamId(teamId);

        var feature = new FeatureRequest(uuid("f1"), "Feature", "Desc", 10.0);
        feature.addProductId(productId);

        var team = new Team(teamId, "Team");

        assertThat(OwnershipValidator.teamOwnsProducts(team, feature, Map.of(productId, product)))
            .isTrue();
    }

    @Test
    void teamOwnsProducts_shouldReturnFalseWhenTeamDoesNotOwnProduct() {
        UUID productId = uuid("p1");
        UUID teamId = uuid("t1");

        var product = new Product(productId, "Product", "Desc");

        var feature = new FeatureRequest(uuid("f1"), "Feature", "Desc", 10.0);
        feature.addProductId(productId);

        var team = new Team(teamId, "Team");

        assertThat(OwnershipValidator.teamOwnsProducts(team, feature, Map.of(productId, product)))
            .isFalse();
    }

    @Test
    void findCandidateTeams_shouldFilterByProductOwnership() {
        var product = new Product(uuid("p1"), "Product", "Desc");
        product.addTeamId(uuid("t1"));
        product.addTeamId(uuid("t2"));

        var feature = new FeatureRequest(uuid("f1"), "Feature", "Desc", 10.0);
        feature.addProductId(uuid("p1"));

        var team1 = new Team(uuid("t1"), "Team 1");
        var team2 = new Team(uuid("t2"), "Team 2");
        var team3 = new Team(uuid("t3"), "Team 3");

        var candidates = OwnershipValidator.findCandidateTeams(
            feature, List.of(team1, team2, team3), Map.of(uuid("p1"), product)
        );

        assertThat(candidates).hasSize(2);
        assertThat(candidates).extracting(Team::getId)
            .containsExactlyInAnyOrder(uuid("t1"), uuid("t2"));
    }

    @Test
    void findCandidateTeamsWithExpertise_shouldFilterByExpertise() {
        var product = new Product(uuid("p1"), "Product", "Desc");
        product.addTeamId(uuid("t1"));
        product.addTeamId(uuid("t2"));

        var feature = new FeatureRequest(uuid("f1"), "Feature", "Desc", 10.0);
        feature.addProductId(uuid("p1"));
        feature.addRequiredExpertise("legacy");

        var team1 = new Team(uuid("t1"), "Team 1");
        team1.addExpertise("legacy");
        var team2 = new Team(uuid("t2"), "Team 2");

        var candidates = OwnershipValidator.findCandidateTeamsWithExpertise(
            feature, List.of(team1, team2), Map.of(uuid("p1"), product)
        );

        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).getId()).isEqualTo(uuid("t1"));
    }

    private UUID uuid(String s) {
        return UUID.nameUUIDFromBytes(s.getBytes());
    }
}
