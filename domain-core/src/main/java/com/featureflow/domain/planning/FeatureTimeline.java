package com.featureflow.domain.planning;

import java.time.LocalDate;
import java.util.UUID;

public record FeatureTimeline(
    UUID featureId,
    LocalDate startDate,
    LocalDate endDate,
    double probabilityOfMeetingDeadline
) {}
