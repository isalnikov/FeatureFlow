package com.featureflow.data.dto;

import java.time.LocalDate;

public record CreatePlanningWindowRequest(
    LocalDate startDate,
    LocalDate endDate
) {}
