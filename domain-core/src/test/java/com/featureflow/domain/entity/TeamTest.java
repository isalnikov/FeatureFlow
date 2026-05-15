package com.featureflow.domain.entity;

import com.featureflow.domain.valueobject.Capacity;
import com.featureflow.domain.valueobject.DateRange;
import com.featureflow.domain.valueobject.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TeamTest {

    @Test
    void create_shouldInitializeDefaults() {
        var team = new Team(UUID.randomUUID(), "Test Team");

        assertThat(team.getMembers()).isEmpty();
        assertThat(team.getExpertiseTags()).isEmpty();
        assertThat(team.getVelocity()).isNull();
    }

    @Test
    void addMember_shouldAddToList() {
        var team = new Team(UUID.randomUUID(), "Test Team");
        var member = new TeamMember(UUID.randomUUID(), "John", Role.BACKEND, 1.0);
        team.addMember(member);

        assertThat(team.getMembers()).hasSize(1);
    }

    @Test
    void removeMember_shouldRemoveFromList() {
        var team = new Team(UUID.randomUUID(), "Test Team");
        UUID personId = UUID.randomUUID();
        team.addMember(new TeamMember(personId, "John", Role.BACKEND, 1.0));

        team.removeMember(personId);
        assertThat(team.getMembers()).isEmpty();
    }

    @Test
    void memberCountByRole_shouldCountCorrectly() {
        var team = new Team(UUID.randomUUID(), "Test Team");
        team.addMember(new TeamMember(UUID.randomUUID(), "John", Role.BACKEND, 1.0));
        team.addMember(new TeamMember(UUID.randomUUID(), "Jane", Role.BACKEND, 1.0));
        team.addMember(new TeamMember(UUID.randomUUID(), "Bob", Role.FRONTEND, 1.0));

        var counts = team.memberCountByRole();
        assertThat(counts.get(Role.BACKEND)).isEqualTo(2);
        assertThat(counts.get(Role.FRONTEND)).isEqualTo(1);
    }

    @Test
    void addExpertise_shouldAddToSet() {
        var team = new Team(UUID.randomUUID(), "Test Team");
        team.addExpertise("legacy-system");
        assertThat(team.hasExpertise("legacy-system")).isTrue();
    }

    @Test
    void effectiveCapacityForSprint_shouldUseSprintOverride() {
        var team = new Team(UUID.randomUUID(), "Test Team");
        team.setDefaultCapacity(new Capacity(
            Map.of(Role.BACKEND, 160.0),
            0.7, 0.20, 0.10
        ));

        UUID teamId = team.getId();
        var override = new Capacity(
            Map.of(Role.BACKEND, 80.0),
            0.5, 0.10, 0.05
        );
        var sprint = new Sprint(
            UUID.randomUUID(),
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 14),
            Map.of(teamId, override)
        );

        var effective = team.effectiveCapacityForSprint(sprint);
        assertThat(effective).isEqualTo(override);
    }

    @Test
    void effectiveCapacityForSprint_shouldUseDefaultWhenNoOverride() {
        var team = new Team(UUID.randomUUID(), "Test Team");
        var defaultCap = new Capacity(
            Map.of(Role.BACKEND, 160.0),
            0.7, 0.20, 0.10
        );
        team.setDefaultCapacity(defaultCap);

        var sprint = new Sprint(
            UUID.randomUUID(),
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 14),
            Map.of()
        );

        var effective = team.effectiveCapacityForSprint(sprint);
        assertThat(effective).isEqualTo(defaultCap);
    }

    @Test
    void equality_shouldBeBasedOnId() {
        UUID id = UUID.randomUUID();
        var t1 = new Team(id, "Team 1");
        var t2 = new Team(id, "Team 2");

        assertThat(t1).isEqualTo(t2);
        assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
    }
}
