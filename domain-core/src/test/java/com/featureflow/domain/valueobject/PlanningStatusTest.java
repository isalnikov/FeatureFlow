package com.featureflow.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlanningStatusTest {

    @Test
    void enumValues_shouldExist() {
        assertThat(PlanningStatus.values()).isNotEmpty();
    }
}
