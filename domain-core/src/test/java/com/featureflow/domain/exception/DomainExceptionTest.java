package com.featureflow.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainExceptionTest {

    @Test
    void capacityExceededException_shouldBeDomainException() {
        var ex = new CapacityExceededException("Capacity exceeded");
        assertThat(ex).isInstanceOf(DomainException.class);
        assertThat(ex.getMessage()).isEqualTo("Capacity exceeded");
    }

    @Test
    void dependencyCycleException_shouldBeDomainException() {
        var ex = new DependencyCycleException("Cycle detected");
        assertThat(ex).isInstanceOf(DomainException.class);
        assertThat(ex.getMessage()).isEqualTo("Cycle detected");
    }

    @Test
    void ownershipViolationException_shouldBeDomainException() {
        var ex = new OwnershipViolationException("Ownership violated");
        assertThat(ex).isInstanceOf(DomainException.class);
        assertThat(ex.getMessage()).isEqualTo("Ownership violated");
    }

    @Test
    void domainException_shouldSupportCause() {
        var cause = new RuntimeException("Root cause");
        var ex = new DependencyCycleException("Cycle detected", cause);
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    void sealedHierarchy_shouldOnlyPermitSubclasses() {
        assertThat(DomainException.class.getPermittedSubclasses())
            .containsExactlyInAnyOrder(
                CapacityExceededException.class,
                DependencyCycleException.class,
                OwnershipViolationException.class
            );
    }
}
