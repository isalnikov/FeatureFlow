package com.featureflow.domain.valueobject;

public record ThreePointEstimate(
    double optimistic,
    double mostLikely,
    double pessimistic
) {
    public ThreePointEstimate {
        if (optimistic < 0 || mostLikely < 0 || pessimistic < 0) {
            throw new IllegalArgumentException("Estimates cannot be negative");
        }
        if (optimistic > mostLikely || mostLikely > pessimistic) {
            throw new IllegalArgumentException("Invalid estimate range: optimistic <= mostLikely <= pessimistic required");
        }
    }

    public double expected() {
        return (optimistic + 4 * mostLikely + pessimistic) / 6.0;
    }

    public double standardDeviation() {
        return (pessimistic - optimistic) / 6.0;
    }

    public double variance() {
        double sd = standardDeviation();
        return sd * sd;
    }
}
