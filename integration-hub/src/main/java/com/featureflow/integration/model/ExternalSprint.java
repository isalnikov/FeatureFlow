package com.featureflow.integration.model;

import java.time.LocalDate;
import java.util.List;

public record ExternalSprint(String externalId, String name, LocalDate startDate, LocalDate endDate, String state, String goal) {}
