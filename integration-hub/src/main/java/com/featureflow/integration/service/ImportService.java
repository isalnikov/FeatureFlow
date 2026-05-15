package com.featureflow.integration.service;

import com.featureflow.integration.model.ConnectorConfig;
import com.featureflow.integration.model.ImportResult;
import org.springframework.stereotype.Service;

@Service
public class ImportService {

    public ImportResult importFrom(String source, ConnectorConfig config) {
        return new ImportResult(0, 0, 0, 0);
    }
}
