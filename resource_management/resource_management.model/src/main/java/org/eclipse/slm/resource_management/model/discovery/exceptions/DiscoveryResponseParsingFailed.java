package org.eclipse.slm.resource_management.model.discovery.exceptions;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class DiscoveryResponseParsingFailed extends Exception {

    public DiscoveryResponseParsingFailed(JsonNode jsonNode, String errorDetails) {
        super("Failed to parse discovery response: " + errorDetails + " | " + jsonNode.toString());
    }

}
