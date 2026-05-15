package com.featureflow.integration.model;
import java.util.List;
public record ExternalTeam(String externalId, String name, List<ExternalTeamMember> members) {}
