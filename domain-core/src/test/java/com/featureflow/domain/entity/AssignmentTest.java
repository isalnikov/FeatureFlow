package com.featureflow.domain.entity;

import com.featureflow.domain.valueobject.AssignmentStatus;
import com.featureflow.domain.valueobject.EffortEstimate;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AssignmentTest {

    @Test
    void create_shouldDefaultToPlannedStatus() {
        var assignment = new Assignment(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
        );
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.PLANNED);
    }

    @Test
    void create_shouldDefaultToEmptyEffort() {
        var assignment = new Assignment(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
        );
        assertThat(assignment.getAllocatedEffort().totalHours()).isZero();
    }

    @Test
    void lock_shouldSetStatusToLocked() {
        var assignment = new Assignment(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
        );
        assignment.lock();
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.LOCKED);
        assertThat(assignment.isLocked()).isTrue();
    }

    @Test
    void unlock_shouldSetStatusToPlanned() {
        var assignment = new Assignment(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
        );
        assignment.lock();
        assignment.unlock();
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.PLANNED);
        assertThat(assignment.isLocked()).isFalse();
    }

    @Test
    void setStatus_shouldChangeStatus() {
        var assignment = new Assignment(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
        );
        assignment.setStatus(AssignmentStatus.IN_PROGRESS);
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.IN_PROGRESS);
    }

    @Test
    void equality_shouldBeBasedOnId() {
        UUID id = UUID.randomUUID();
        var a1 = new Assignment(id, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        var a2 = new Assignment(id, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        assertThat(a1).isEqualTo(a2);
        assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
    }
}
