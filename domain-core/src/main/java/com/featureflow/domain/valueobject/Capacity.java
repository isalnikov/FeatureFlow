package com.featureflow.domain.valueobject;

import java.util.Map;
import java.util.Objects;

public record Capacity(
    Map<Role, Double> hoursPerRole,
    double focusFactor,
    double bugReservePercent,
    double techDebtReservePercent
) {
    public Capacity {
        Objects.requireNonNull(hoursPerRole);
        if (focusFactor <= 0 || focusFactor > 1) {
            throw new IllegalArgumentException("Focus factor must be in (0, 1]");
        }
        if (bugReservePercent < 0 || bugReservePercent > 1) {
            throw new IllegalArgumentException("Bug reserve must be in [0, 1]");
        }
        if (techDebtReservePercent < 0 || techDebtReservePercent > 1) {
            throw new IllegalArgumentException("Tech debt reserve must be in [0, 1]");
        }
        if (bugReservePercent + techDebtReservePercent >= 1) {
            throw new IllegalArgumentException("Total reserves must be less than 100%");
        }
    }

    public double effectiveHours(Role role) {
        double raw = hoursPerRole.getOrDefault(role, 0.0);
        double reserve = 1.0 - bugReservePercent - techDebtReservePercent;
        return raw * focusFactor * reserve;
    }

    public double totalEffectiveHours() {
        double reserve = 1.0 - bugReservePercent - techDebtReservePercent;
        return hoursPerRole.values().stream()
            .mapToDouble(Double::doubleValue)
            .sum() * focusFactor * reserve;
    }

    public boolean hasCapacityFor(Role role, double requiredHours) {
        return effectiveHours(role) >= requiredHours;
    }

    public Capacity withFocusFactor(double newFocusFactor) {
        return new Capacity(hoursPerRole, newFocusFactor, bugReservePercent, techDebtReservePercent);
    }

    public static Capacity empty() {
        return new Capacity(Map.of(), 1.0, 0, 0);
    }
}
