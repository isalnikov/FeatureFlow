package com.featureflow.integration.controller;

import com.featureflow.integration.model.ConnectorConfig;
import com.featureflow.integration.model.ExportResult;
import com.featureflow.integration.model.ImportResult;
import com.featureflow.integration.service.ExportService;
import com.featureflow.integration.service.ImportService;
import com.featureflow.integration.service.SyncOrchestrator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/integration")
public class IntegrationController {

    private final ImportService importService;
    private final ExportService exportService;
    private final SyncOrchestrator syncOrchestrator;

    public IntegrationController(ImportService importService,
                                 ExportService exportService,
                                 SyncOrchestrator syncOrchestrator) {
        this.importService = importService;
        this.exportService = exportService;
        this.syncOrchestrator = syncOrchestrator;
    }

    @PostMapping("/{source}/import")
    public ResponseEntity<ImportResult> importFromSource(
            @PathVariable String source,
            @RequestBody ConnectorConfig config) {
        ImportResult result = importService.importFrom(source, config);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{source}/export")
    public ResponseEntity<ExportResult> exportToSource(
            @PathVariable String source,
            @RequestBody Map<String, Object> data) {
        ExportResult result = exportService.exportTo(source, data);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{source}/status")
    public ResponseEntity<Map<String, Object>> getStatus(@PathVariable String source) {
        Map<String, Object> status = Map.of(
                "source", source,
                "connected", false,
                "lastSync", null,
                "status", "not_configured"
        );
        return ResponseEntity.ok(status);
    }

    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> triggerFullSync() {
        var result = syncOrchestrator.fullSync();
        return ResponseEntity.ok(Map.of(
                "synced", result.synced(),
                "errors", result.errors(),
                "duration", result.duration()
        ));
    }
}
