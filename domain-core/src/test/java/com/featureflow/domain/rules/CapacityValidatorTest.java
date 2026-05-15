package com.featureflow.domain.rules;

import com.featureflow.domain.entity.Assignment;
import com.featureflow.domain.entity.Sprint;
import com.featureflow.domain.entity.Team;
import com.featureflow.domain.valueobject.Capacity;
import com.featureflow.domain.valueobject.EffortEstimate;
import com.featureflow.domain.valueobject.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CapacityValidatorTest {

    @Test
    void checkCapacity_shouldPassWhenCapacityAvailable() {
        var team = createTeamWithCapacity(Map.of(Role.BACKEND, 160.0));
        var sprint = new Sprint(UUID.randomUUID(), LocalDate.now(), LocalDate.now().plusWeeks(2));
        var newEffort = new EffortEstimate(40, 0, 0, 0);

        var result = CapacityValidator.checkCapacity(team, sprint, List.of(), newEffort);
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void checkCapacity_shouldFailWhenExceeded() {
        var team = createTeamWithCapacity(Map.of(Role.BACKEND, 40.0));
        var sprint = new Sprint(UUID.randomUUID(), LocalDate.now(), LocalDate.now().plusWeeks(2));
        var newEffort = new EffortEstimate(50, 0, 0, 0);

        var result = CapacityValidator.checkCapacity(team, sprint, List.of(), newEffort);
        assertThat(result.isValid()).isFalse();
        assertThat(result.errorMessage()).contains("exceeds");
    }

    @Test
    void checkCapacity_simple_shouldPass() {
        var result = CapacityValidator.checkCapacity(100.0, 30.0, 50.0, Role.BACKEND);
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void checkCapacity_simple_shouldFail() {
        var result = CapacityValidator.checkCapacity(100.0, 60.0, 50.0, Role.BACKEND);
        assertThat(result.isValid()).isFalse();
    }

    private Team createTeamWithCapacity(Map<Role, Double> capacity) {
        var team = new Team(UUID.randomUUID(), "Team");
        team.setDefaultCapacity(new Capacity(capacity, 0.7, 0.20, 0.10));
        return team;
    }
}
