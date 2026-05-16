package com.featureflow.data.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateTeamRequest(
    String name,
    Double focusFactor,
    Double bugReservePercent,
    Double techDebtReservePercent,
    Double velocity,
    Set<String> expertiseTags
) {}
