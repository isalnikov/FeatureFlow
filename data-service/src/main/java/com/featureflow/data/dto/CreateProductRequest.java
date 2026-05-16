package com.featureflow.data.dto;

import java.util.Set;

public record CreateProductRequest(
    String name,
    String description,
    Set<String> technologyStack
) {}
