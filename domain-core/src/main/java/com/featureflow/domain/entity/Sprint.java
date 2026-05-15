package com.featureflow.domain.entity;

import com.featureflow.domain.valueobject.Capacity;
import com.featureflow.domain.valueobject.DateRange;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record Sprint(
    UUID id,
    LocalDate startDate,
    LocalDate endDate,
    Map<UUID, Capacity> capacityOverrides
) {
    public Sprint {
        Objects.requireNonNull(id);
        Objects.requireNonNull(startDate);
        Objects.requireNonNull(endDate);
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
    }

    public Sprint(UUID id, LocalDate startDate, LocalDate endDate) {
        this(id, startDate, endDate, Map.of());
    }

    public DateRange dateRange() {
        return new DateRange(startDate, endDate);
    }

    public boolean containsDate(LocalDate date) {
        return dateRange().contains(date);
    }

    public long durationDays() {
        return dateRange().days();
    }
}
