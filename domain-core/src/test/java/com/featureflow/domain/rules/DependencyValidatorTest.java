package com.featureflow.domain.rules;

import com.featureflow.domain.exception.DependencyCycleException;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DependencyValidatorTest {

    @Test
    void checkNoCycles_shouldPassForAcyclicGraph() {
        var deps = Map.of(
            uuid("a"), Set.of(uuid("b"), uuid("c")),
            uuid("b"), Set.of(uuid("c")),
            uuid("c"), Set.<UUID>of()
        );
        assertThatCode(() -> DependencyValidator.checkNoCycles(deps))
            .doesNotThrowAnyException();
    }

    @Test
    void checkNoCycles_shouldDetectCycle() {
        var deps = Map.of(
            uuid("a"), Set.of(uuid("b")),
            uuid("b"), Set.of(uuid("c")),
            uuid("c"), Set.of(uuid("a"))
        );
        assertThatThrownBy(() -> DependencyValidator.checkNoCycles(deps))
            .isInstanceOf(DependencyCycleException.class);
    }

    @Test
    void checkNoCycles_shouldPassForEmptyGraph() {
        var deps = Map.<UUID, Set<UUID>>of();
        assertThatCode(() -> DependencyValidator.checkNoCycles(deps))
            .doesNotThrowAnyException();
    }

    @Test
    void checkNoCycles_shouldPassForSingleNode() {
        var deps = Map.of(uuid("a"), Set.<UUID>of());
        assertThatCode(() -> DependencyValidator.checkNoCycles(deps))
            .doesNotThrowAnyException();
    }

    @Test
    void topologicalSort_shouldReturnCorrectOrder() {
        var deps = Map.of(
            uuid("a"), Set.of(uuid("b")),
            uuid("b"), Set.of(uuid("c")),
            uuid("c"), Set.<UUID>of()
        );
        var sorted = DependencyValidator.topologicalSort(deps);

        assertThat(sorted).hasSize(3);
        assertThat(sorted.indexOf(uuid("a"))).isLessThan(sorted.indexOf(uuid("b")));
        assertThat(sorted.indexOf(uuid("b"))).isLessThan(sorted.indexOf(uuid("c")));
    }

    @Test
    void topologicalSort_shouldThrowForCycle() {
        var deps = Map.of(
            uuid("a"), Set.of(uuid("b")),
            uuid("b"), Set.of(uuid("a"))
        );
        assertThatThrownBy(() -> DependencyValidator.topologicalSort(deps))
            .isInstanceOf(DependencyCycleException.class);
    }

    private UUID uuid(String s) {
        return UUID.nameUUIDFromBytes(s.getBytes());
    }
}
