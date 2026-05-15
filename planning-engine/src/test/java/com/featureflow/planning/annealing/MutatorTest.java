package com.featureflow.planning.annealing;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class MutatorTest {

    @Test
    void shiftFeatureSprint_shiftsForward() {
        UUID f1 = uuid("f1");
        UUID t1 = uuid("t1");

        Solution current = new Solution(
            Map.of(),
            Map.of(f1, 2),
            Map.of(f1, t1),
            100.0
        );

        Mutator mutator = new Mutator();
        Solution mutated = mutator.shiftFeatureSprint(current, List.of(f1), 1);

        assertThat(mutated.featureSprintIndex().get(f1)).isEqualTo(3);
    }

    @Test
    void shiftFeatureSprint_doesNotGoBelowZero() {
        UUID f1 = uuid("f1");
        UUID t1 = uuid("t1");

        Solution current = new Solution(
            Map.of(),
            Map.of(f1, 0),
            Map.of(f1, t1),
            100.0
        );

        Mutator mutator = new Mutator();
        Solution mutated = mutator.shiftFeatureSprint(current, List.of(f1), -1);

        assertThat(mutated.featureSprintIndex().get(f1)).isEqualTo(0);
    }

    @Test
    void swapFeatures_exchangesSprintIndices() {
        UUID f1 = uuid("f1");
        UUID f2 = uuid("f2");
        UUID t1 = uuid("t1");

        Solution current = new Solution(
            Map.of(),
            Map.of(f1, 0, f2, 3),
            Map.of(f1, t1, f2, t1),
            100.0
        );

        Mutator mutator = new Mutator();
        Solution mutated = mutator.swapFeatures(current, f1, f2);

        assertThat(mutated.featureSprintIndex().get(f1)).isEqualTo(3);
        assertThat(mutated.featureSprintIndex().get(f2)).isEqualTo(0);
    }

    @Test
    void reassignTeam_changesTeamForFeature() {
        UUID f1 = uuid("f1");
        UUID t1 = uuid("t1");
        UUID t2 = uuid("t2");

        Solution current = new Solution(
            Map.of(),
            Map.of(f1, 0),
            Map.of(f1, t1),
            100.0
        );

        Mutator mutator = new Mutator();
        Solution mutated = mutator.reassignTeam(current, f1, t2);

        assertThat(mutated.featureTeamMapping().get(f1)).isEqualTo(t2);
    }

    @Test
    void reassignTeam_sameTeam_returnsUnchanged() {
        UUID f1 = uuid("f1");
        UUID t1 = uuid("t1");

        Solution current = new Solution(
            Map.of(),
            Map.of(f1, 0),
            Map.of(f1, t1),
            100.0
        );

        Mutator mutator = new Mutator();
        Solution mutated = mutator.reassignTeam(current, f1, t1);

        assertThat(mutated).isSameAs(current);
    }

    @Test
    void apply_withRandom_shiftsOrSwaps() {
        UUID f1 = uuid("f1");
        UUID f2 = uuid("f2");
        UUID t1 = uuid("t1");

        Solution current = new Solution(
            Map.of(),
            Map.of(f1, 0, f2, 3),
            Map.of(f1, t1, f2, t1),
            100.0
        );

        Mutator mutator = new Mutator();
        Solution mutated = mutator.apply(current, List.of(f1, f2), new Random(42));

        assertThat(mutated).isNotNull();
    }

    @Test
    void apply_withCandidateTeams_reassignsTeam() {
        UUID f1 = uuid("f1");
        UUID t1 = uuid("t1");
        UUID t2 = uuid("t2");

        Solution current = new Solution(
            Map.of(),
            Map.of(f1, 0),
            Map.of(f1, t1),
            100.0
        );

        Mutator mutator = new Mutator();
        Solution mutated = mutator.apply(current, List.of(f1), List.of(t1, t2), new Random(42));

        assertThat(mutated).isNotNull();
        assertThat(mutated.cost()).isEqualTo(Double.MAX_VALUE);
    }

    @Test
    void swapFeatures_withNullSprint_returnsUnchanged() {
        UUID f1 = uuid("f1");
        UUID f2 = uuid("f2");
        UUID t1 = uuid("t1");

        Solution current = new Solution(
            Map.of(),
            Map.of(f1, 0),
            Map.of(f1, t1, f2, t1),
            100.0
        );

        Mutator mutator = new Mutator();
        Solution mutated = mutator.swapFeatures(current, f1, f2);

        assertThat(mutated.featureSprintIndex().get(f1)).isEqualTo(0);
        assertThat(mutated.featureSprintIndex().get(f2)).isNull();
    }

    @Test
    void apply_withSingleFeature_swapReturnsCurrent() {
        UUID f1 = uuid("f1");
        UUID t1 = uuid("t1");

        Solution current = new Solution(
            Map.of(),
            Map.of(f1, 0),
            Map.of(f1, t1),
            100.0
        );

        Mutator mutator = new Mutator();
        Solution mutated = mutator.apply(current, List.of(f1), new Random(42));

        assertThat(mutated).isNotNull();
    }

    private UUID uuid(String suffix) {
        return UUID.nameUUIDFromBytes(suffix.getBytes());
    }
}
