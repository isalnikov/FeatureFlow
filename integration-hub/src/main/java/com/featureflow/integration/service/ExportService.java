package com.featureflow.integration.service;

import com.featureflow.integration.model.ExportResult;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
public class ExportService {

    public ExportResult exportTo(String source, Map<String, Object> data) {
        return new ExportResult(Collections.emptyList());
    }
}
