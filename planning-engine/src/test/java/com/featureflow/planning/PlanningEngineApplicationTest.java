package com.featureflow.planning;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlanningEngineApplicationTest {

    @Test
    void applicationClassExists() {
        PlanningEngineApplication app = new PlanningEngineApplication();
        assertThat(app).isNotNull();
    }
}
