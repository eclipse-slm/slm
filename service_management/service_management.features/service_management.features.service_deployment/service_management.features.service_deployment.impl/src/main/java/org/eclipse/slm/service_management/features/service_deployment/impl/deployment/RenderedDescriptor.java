package org.eclipse.slm.service_management.features.service_deployment.impl.deployment;

import java.util.List;
import java.util.Map;

/**
 * Ergebnis des Renderns: die Nutzlast fuer das Ziel plus die Angaben,
 * die Service Management selbst fuer die ServiceInstance braucht.
 */
public record RenderedDescriptor(
        byte[] content,
        String contentType,
        Map<String, String> serviceMetaData,
        List<Integer> servicePorts
) {
}
