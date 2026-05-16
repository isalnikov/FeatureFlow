package com.featureflow.data.dto;

import java.util.Set;

public record CreateTeamRequest(
    String name,
    Double focusFactor,
    Double bugReservePercent,
    Double techDebtReservePercent,
    Set<String> expertiseTags,
    String externalId
) {}
