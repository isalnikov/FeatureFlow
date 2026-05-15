package com.featureflow.integration.mapper;

import com.featureflow.domain.entity.FeatureRequest;
import com.featureflow.integration.model.ExternalIssue;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ExternalIssueMapper {

    public FeatureRequest toFeatureRequest(ExternalIssue externalIssue) {
        if (externalIssue == null) {
            return null;
        }

        FeatureRequest feature = new FeatureRequest(
                UUID.randomUUID(),
                externalIssue.title(),
                externalIssue.description(),
                0.0
        );

        if (externalIssue.dueDate() != null) {
            feature.setDeadline(externalIssue.dueDate());
        }

        return feature;
    }

    public ExternalIssue fromFeatureRequest(FeatureRequest featureRequest) {
        if (featureRequest == null) {
            return null;
        }

        return new ExternalIssue(
                featureRequest.getId().toString(),
                null,
                featureRequest.getTitle(),
                featureRequest.getDescription(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                featureRequest.getDeadline()
        );
    }
}
