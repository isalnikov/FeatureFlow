package com.featureflow.integration.model;
import java.util.List;
public record ExportResult(List<Entry> entries) { public record Entry(String key, boolean success, String error) {} }
