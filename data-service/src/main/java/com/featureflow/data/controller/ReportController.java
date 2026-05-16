package com.featureflow.data.controller;

import com.featureflow.data.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/features/csv")
    public ResponseEntity<byte[]> exportFeaturesCsv(
        @RequestParam(required = false) String productId
    ) {
        byte[] csv = reportService.exportFeaturesCsv(productId);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, "text/csv")
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=features.csv")
            .body(csv);
    }
}
