package org.eclipse.slm.common.messaging.resources;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.UUID;

public record ResourceCreatedMessage(
        @JsonProperty("resourceId") UUID resourceId,
        @JsonProperty("assetId") String assetId
) implements Serializable {

    public static final String QUEUE_NAME_PREFIX_RESOURCE_CREATED = "resource.created_";

    public static final String ROUTING_KEY = "resource.created";

    public UUID resourceId() {
        return resourceId;
    }

    public String assetId() {
        return assetId;
    }
}
