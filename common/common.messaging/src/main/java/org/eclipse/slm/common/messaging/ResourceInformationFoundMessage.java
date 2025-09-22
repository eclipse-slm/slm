package org.eclipse.slm.common.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public record ResourceInformationFoundMessage(
        @JsonProperty("resourceId") UUID resourceId,
        @JsonProperty("resourceInformationEntries") List<ResourceInformationEntry> resourceInformationEntries
) implements Serializable {

    public static final String QUEUE_NAME_PREFIX_RESOURCE_INFORMATION = "resource.information_";

    public static final String ROUTING_KEY = "resource.information";

    public List<ResourceInformationEntry> getResourceInformationEntries() {
        return resourceInformationEntries;
    }

    public UUID getResourceId() {
        return resourceId;
    }
}
