package com.featureflow.data.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateProductRequest(
    String name,
    String description,
    Set<String> technologyStack
) {}
