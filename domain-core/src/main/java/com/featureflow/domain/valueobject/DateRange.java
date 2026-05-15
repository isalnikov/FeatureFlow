package com.featureflow.domain.valueobject;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public record DateRange(LocalDate start, LocalDate end) {
    public DateRange {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End must be after or equal to start");
        }
    }

    public long days() {
        return ChronoUnit.DAYS.between(start, end);
    }

    public boolean overlaps(DateRange other) {
        return !this.end.isBefore(other.start) && !other.end.isBefore(this.start);
    }

    public boolean contains(LocalDate date) {
        return !date.isBefore(start) && !date.isAfter(end);
    }

    public boolean contains(DateRange other) {
        return contains(other.start) && contains(other.end);
    }

    public DateRange withStart(LocalDate newStart) {
        return new DateRange(newStart, end);
    }

    public DateRange withEnd(LocalDate newEnd) {
        return new DateRange(start, newEnd);
    }
}
