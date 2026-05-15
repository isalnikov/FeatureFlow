package com.featureflow.integration.service;

import org.springframework.stereotype.Service;

@Service
public class SyncOrchestrator {

    public SyncResult fullSync() {
        return new SyncResult(0, 0, 0L);
    }

    public record SyncResult(int synced, int errors, long duration) {}
}
