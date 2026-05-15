package com.featureflow.domain.planning;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConflictTest {

    @Test
    void type_enumValues() {
        assertThat(Conflict.Type.values()).hasSize(6);
        assertThat(Conflict.Type.valueOf("CAPACITY_EXCEEDED")).isNotNull();
        assertThat(Conflict.Type.valueOf("DEPENDENCY_VIOLATION")).isNotNull();
        assertThat(Conflict.Type.valueOf("OWNERSHIP_VIOLATION")).isNotNull();
        assertThat(Conflict.Type.valueOf("PARALLELISM_EXCEEDED")).isNotNull();
        assertThat(Conflict.Type.valueOf("DEADLINE_MISSED")).isNotNull();
        assertThat(Conflict.Type.valueOf("DEPENDENCY_CYCLE")).isNotNull();
    }

    @Test
    void create_shouldSetFields() {
        var conflict = new Conflict(
            Conflict.Type.CAPACITY_EXCEEDED,
            java.util.UUID.randomUUID(),
            java.util.UUID.randomUUID(),
            "Capacity exceeded"
        );
        assertThat(conflict.type()).isEqualTo(Conflict.Type.CAPACITY_EXCEEDED);
        assertThat(conflict.message()).isEqualTo("Capacity exceeded");
    }
}
