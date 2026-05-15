package com.featureflow.domain.planning;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConstraintViolationTest {

    @Test
    void violationType_enumValues() {
        assertThat(ConstraintViolation.ViolationType.values()).hasSize(6);
    }

    @Test
    void create_shouldSetFields() {
        var violation = new ConstraintViolation(
            ConstraintViolation.ViolationType.CAPACITY_EXCEEDED,
            java.util.UUID.randomUUID(),
            java.util.UUID.randomUUID(),
            "Capacity exceeded"
        );
        assertThat(violation.type()).isEqualTo(ConstraintViolation.ViolationType.CAPACITY_EXCEEDED);
        assertThat(violation.message()).isEqualTo("Capacity exceeded");
    }
}
