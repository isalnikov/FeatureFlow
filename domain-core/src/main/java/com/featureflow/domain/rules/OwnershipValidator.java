package com.featureflow.domain.rules;

import com.featureflow.domain.entity.FeatureRequest;
import com.featureflow.domain.entity.Product;
import com.featureflow.domain.entity.Team;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class OwnershipValidator {

    private OwnershipValidator() {}

    public static boolean teamOwnsProducts(
        Team team,
        FeatureRequest feature,
        Map<UUID, Product> products
    ) {
        return feature.getProductIds().stream()
            .map(products::get)
            .allMatch(p -> p != null && p.getTeamIds().contains(team.getId()));
    }

    public static List<Team> findCandidateTeams(
        FeatureRequest feature,
        List<Team> teams,
        Map<UUID, Product> products
    ) {
        return teams.stream()
            .filter(t -> teamOwnsProducts(t, feature, products))
            .toList();
    }

    public static List<Team> findCandidateTeamsWithExpertise(
        FeatureRequest feature,
        List<Team> teams,
        Map<UUID, Product> products
    ) {
        return findCandidateTeams(feature, teams, products).stream()
            .filter(t -> feature.getRequiredExpertise().stream()
                .allMatch(t::hasExpertise))
            .toList();
    }
}
