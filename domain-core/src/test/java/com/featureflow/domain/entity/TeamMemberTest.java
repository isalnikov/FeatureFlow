package com.featureflow.domain.entity;

import com.featureflow.domain.valueobject.Role;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeamMemberTest {

    @Test
    void create_shouldSetFields() {
        UUID id = UUID.randomUUID();
        var member = new TeamMember(id, "John", Role.BACKEND, 1.0);

        assertThat(member.personId()).isEqualTo(id);
        assertThat(member.name()).isEqualTo("John");
        assertThat(member.role()).isEqualTo(Role.BACKEND);
        assertThat(member.availabilityPercent()).isEqualTo(1.0);
    }

    @Test
    void create_withDefaultAvailability() {
        var member = new TeamMember(UUID.randomUUID(), "John", Role.BACKEND);
        assertThat(member.availabilityPercent()).isEqualTo(1.0);
    }

    @Test
    void invalidAvailability_shouldThrow() {
        assertThatThrownBy(() -> new TeamMember(UUID.randomUUID(), "John", Role.BACKEND, -0.1))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TeamMember(UUID.randomUUID(), "John", Role.BACKEND, 1.5))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equality_shouldCompareAllFields() {
        UUID id = UUID.randomUUID();
        var m1 = new TeamMember(id, "John", Role.BACKEND, 1.0);
        var m2 = new TeamMember(id, "John", Role.BACKEND, 1.0);

        assertThat(m1).isEqualTo(m2);

        var m3 = new TeamMember(id, "John", Role.FRONTEND, 1.0);
        assertThat(m1).isNotEqualTo(m3);
    }
}
