package com.featureflow.domain.valueobject;

public record EffortEstimate(
    double backendHours,
    double frontendHours,
    double qaHours,
    double devopsHours
) {
    public EffortEstimate {
        if (backendHours < 0 || frontendHours < 0 || qaHours < 0 || devopsHours < 0) {
            throw new IllegalArgumentException("Effort cannot be negative");
        }
    }

    public EffortEstimate() {
        this(0, 0, 0, 0);
    }

    public double totalHours() {
        return backendHours + frontendHours + qaHours + devopsHours;
    }

    public double forRole(Role role) {
        return switch (role) {
            case BACKEND -> backendHours;
            case FRONTEND -> frontendHours;
            case QA -> qaHours;
            case DEVOPS -> devopsHours;
        };
    }

    public EffortEstimate withScaledEffort(double factor) {
        return new EffortEstimate(
            backendHours * factor,
            frontendHours * factor,
            qaHours * factor,
            devopsHours * factor
        );
    }

    public EffortEstimate add(EffortEstimate other) {
        return new EffortEstimate(
            this.backendHours + other.backendHours,
            this.frontendHours + other.frontendHours,
            this.qaHours + other.qaHours,
            this.devopsHours + other.devopsHours
        );
    }
}
