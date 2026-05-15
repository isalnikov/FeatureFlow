package com.featureflow.domain.valueobject;

import java.util.UUID;

public record CapacitySlot(
    UUID teamId,
    int sprintIndex,
    EffortEstimate availableEffort
) {
    public CapacitySlot deduct(EffortEstimate effort) {
        return new CapacitySlot(
            teamId,
            sprintIndex,
            new EffortEstimate(
                Math.max(0, availableEffort.backendHours() - effort.backendHours()),
                Math.max(0, availableEffort.frontendHours() - effort.frontendHours()),
                Math.max(0, availableEffort.qaHours() - effort.qaHours()),
                Math.max(0, availableEffort.devopsHours() - effort.devopsHours())
            )
        );
    }

    public boolean canFit(EffortEstimate effort) {
        return availableEffort.backendHours() >= effort.backendHours()
            && availableEffort.frontendHours() >= effort.frontendHours()
            && availableEffort.qaHours() >= effort.qaHours()
            && availableEffort.devopsHours() >= effort.devopsHours();
    }
}
