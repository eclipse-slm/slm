package org.eclipse.slm.resource_management.features.metrics;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;
import java.util.UUID;


public interface MetricsRestApi {

    @GetMapping("/{resourceId}")
    ResponseEntity<Map<String, Object>> getMetric(
            @PathVariable(name = "resourceId") UUID resourceId
    );
}

