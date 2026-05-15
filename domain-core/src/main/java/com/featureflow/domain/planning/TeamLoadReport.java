package com.featureflow.domain.planning;

import com.featureflow.domain.valueobject.Role;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record TeamLoadReport(
    UUID teamId,
    Map<LocalDate, Map<Role, Double>> loadByDateAndRole,
    double utilizationPercent,
    List<String> bottleneckRoles
) {}
