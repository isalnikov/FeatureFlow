package com.featureflow.integration.controller;

import com.featureflow.integration.model.AssignmentExport;
import com.featureflow.integration.model.ConnectorConfig;
import com.featureflow.integration.model.ExportResult;
import com.featureflow.integration.model.ImportFilter;
import com.featureflow.integration.model.ImportResult;
import com.featureflow.integration.service.ExportService;
import com.featureflow.integration.service.ImportService;
import com.featureflow.integration.service.SyncOrchestrator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/integrations")
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

    public record ImportRequest(ConnectorConfig config, ImportFilter filter) {}
    public record ExportRequest(ConnectorConfig config, List<AssignmentExport> assignments) {}

    @PostMapping("/{type}/import")
    public ResponseEntity<ImportResult> importData(
        @PathVariable String type,
        @RequestBody ImportRequest request
    ) {
        ImportFilter filter = request.filter() != null ? request.filter()
            : new ImportFilter(null, null, null, null, 100);
        var result = importService.importFrom(type, request.config(), filter);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{type}/export")
    public ResponseEntity<ExportResult> exportData(
        @PathVariable String type,
        @RequestBody ExportRequest request
    ) {
        var result = exportService.exportTo(type, request.config(), request.assignments());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{type}/test")
    public ResponseEntity<Map<String, Boolean>> testConnection(
        @PathVariable String type,
        @RequestBody ConnectorConfig config
    ) {
        boolean ok = importService.testConnection(type, config);
        return ResponseEntity.ok(Map.of("connected", ok));
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
