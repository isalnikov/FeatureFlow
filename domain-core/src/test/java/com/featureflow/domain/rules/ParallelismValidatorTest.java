package com.featureflow.domain.rules;

import com.featureflow.domain.entity.Assignment;
import com.featureflow.domain.entity.Sprint;
import com.featureflow.domain.entity.Team;
import com.featureflow.domain.valueobject.AssignmentStatus;
import com.featureflow.domain.valueobject.EffortEstimate;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ParallelismValidatorTest {

    @Test
    void withinParallelismLimit_shouldAllowUpTo3Features() {
        var team = new Team(uuid("t1"), "Team");
        var sprint = new Sprint(uuid("s1"), LocalDate.now(), LocalDate.now().plusWeeks(2));

        var assignments = List.of(
            createAssignment(uuid("t1"), uuid("s1"), uuid("f1")),
            createAssignment(uuid("t1"), uuid("s1"), uuid("f2")),
            createAssignment(uuid("t1"), uuid("s1"), uuid("f3"))
        );

        assertThat(ParallelismValidator.withinParallelismLimit(team, sprint, assignments)).isTrue();
    }

    @Test
    void withinParallelismLimit_shouldReject4thFeature() {
        var team = new Team(uuid("t1"), "Team");
        var sprint = new Sprint(uuid("s1"), LocalDate.now(), LocalDate.now().plusWeeks(2));

        var assignments = List.of(
            createAssignment(uuid("t1"), uuid("s1"), uuid("f1")),
            createAssignment(uuid("t1"), uuid("s1"), uuid("f2")),
            createAssignment(uuid("t1"), uuid("s1"), uuid("f3")),
            createAssignment(uuid("t1"), uuid("s1"), uuid("f4"))
        );

        assertThat(ParallelismValidator.withinParallelismLimit(team, sprint, assignments)).isFalse();
    }

    @Test
    void withinParallelismLimit_shouldIgnoreCompletedAssignments() {
        var team = new Team(uuid("t1"), "Team");
        var sprint = new Sprint(uuid("s1"), LocalDate.now(), LocalDate.now().plusWeeks(2));

        var completed = createAssignment(uuid("t1"), uuid("s1"), uuid("f1"));
        completed.setStatus(AssignmentStatus.COMPLETED);

        var assignments = List.of(
            completed,
            createAssignment(uuid("t1"), uuid("s1"), uuid("f2")),
            createAssignment(uuid("t1"), uuid("s1"), uuid("f3")),
            createAssignment(uuid("t1"), uuid("s1"), uuid("f4"))
        );

        assertThat(ParallelismValidator.withinParallelismLimit(team, sprint, assignments)).isTrue();
    }

    @Test
    void withinParallelismLimit_withCustomMax_shouldUseProvidedLimit() {
        var team = new Team(uuid("t1"), "Team");
        var sprint = new Sprint(uuid("s1"), LocalDate.now(), LocalDate.now().plusWeeks(2));

        var assignments = List.of(
            createAssignment(uuid("t1"), uuid("s1"), uuid("f1")),
            createAssignment(uuid("t1"), uuid("s1"), uuid("f2"))
        );

        assertThat(ParallelismValidator.withinParallelismLimit(team, sprint, assignments, 1)).isFalse();
        assertThat(ParallelismValidator.withinParallelismLimit(team, sprint, assignments, 2)).isTrue();
        assertThat(ParallelismValidator.withinParallelismLimit(team, sprint, assignments, 3)).isTrue();
    }

    @Test
    void countActiveFeatures_shouldReturnCorrectCount() {
        var assignments = List.of(
            createAssignment(uuid("t1"), uuid("s1"), uuid("f1")),
            createAssignment(uuid("t1"), uuid("s1"), uuid("f2")),
            createAssignment(uuid("t1"), uuid("s1"), uuid("f3"))
        );

        var count = ParallelismValidator.countActiveFeatures(uuid("t1"), uuid("s1"), assignments);
        assertThat(count).isEqualTo(3);
    }

    @Test
    void countActiveFeatures_shouldIgnoreOtherTeamsAndSprints() {
        var assignments = List.of(
            createAssignment(uuid("t1"), uuid("s1"), uuid("f1")),
            createAssignment(uuid("t2"), uuid("s1"), uuid("f2")),
            createAssignment(uuid("t1"), uuid("s2"), uuid("f3"))
        );

        var count = ParallelismValidator.countActiveFeatures(uuid("t1"), uuid("s1"), assignments);
        assertThat(count).isEqualTo(1);
    }

    private Assignment createAssignment(UUID teamId, UUID sprintId, UUID featureId) {
        return new Assignment(
            UUID.randomUUID(), featureId, teamId, sprintId, new EffortEstimate(10, 10, 5, 5)
        );
    }

    private UUID uuid(String s) {
        return UUID.nameUUIDFromBytes(s.getBytes());
    }
}
