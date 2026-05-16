package com.featureflow.data.service;

import com.featureflow.data.entity.FeatureEntity;
import com.featureflow.data.repository.FeatureRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReportService {

    private final FeatureRepository featureRepo;

    public ReportService(FeatureRepository featureRepo) {
        this.featureRepo = featureRepo;
    }

    public byte[] exportFeaturesCsv(String productId) {
        List<FeatureEntity> features = productId != null
            ? featureRepo.findByProductId(UUID.fromString(productId))
            : featureRepo.findAll();

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Title,Business Value,Class of Service,Backend Hours,Frontend Hours,QA Hours,Deadline\n");

        for (FeatureEntity f : features) {
            Map<String, Double> effort = f.getEffortEstimate();
            csv.append(f.getId()).append(",");
            csv.append(escapeCsv(f.getTitle())).append(",");
            csv.append(f.getBusinessValue()).append(",");
            csv.append(f.getClassOfService()).append(",");
            csv.append(effort.getOrDefault("backendHours", 0.0)).append(",");
            csv.append(effort.getOrDefault("frontendHours", 0.0)).append(",");
            csv.append(effort.getOrDefault("qaHours", 0.0)).append(",");
            csv.append(f.getDeadline() != null ? f.getDeadline() : "").append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
