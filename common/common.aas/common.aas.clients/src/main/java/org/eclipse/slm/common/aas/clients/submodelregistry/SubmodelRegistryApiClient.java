package org.eclipse.slm.common.aas.clients.submodelregistry;

import feign.Body;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.eclipse.slm.common.aas.model.shellregistry.respones.GetSubmodelDescriptorsResult;

/**
 * Feign Client for the Submodel Registry Service Specification (AAS Part 2, V3.1.1_SSP-001 (Full Profile)).
 * According to the OpenAPI specification: https://admin-shell.io/aas/API/3/1/SubmodelRegistryServiceSpecification/SSP-001
 */
public interface SubmodelRegistryApiClient {

    /**
     * Returns all Submodel Descriptors, optionally filtered by limit and cursor.
     * @param limit Maximum number of results to return
     * @param cursor Cursor for paginated results
     * @return List of Submodel Descriptors
     */
    @RequestLine("GET /submodel-descriptors?limit={limit}&cursor={cursor}")
    GetSubmodelDescriptorsResult getAllSubmodelDescriptors(
        @Param("limit") Integer limit,
        @Param("cursor") String cursor
    );

    /**
     * Creates a new Submodel Descriptor (registers a submodel).
     * @param body Submodel Descriptor object
     * @return The created Submodel Descriptor
     */
    @RequestLine("POST /submodel-descriptors")
    @Headers("Content-Type: application/json")
    @Body("{body}")
    SubmodelDescriptor postSubmodelDescriptor(SubmodelDescriptor body);

    /**
     * Returns a specific Submodel Descriptor by its unique identifier.
     * @param submodelIdentifier The Submodel’s unique id
     * @return The requested Submodel Descriptor, or null if not found
     */
    @RequestLine("GET /submodel-descriptors/{submodelIdentifier}")
    SubmodelDescriptor getSubmodelDescriptorById(@Param("submodelIdentifier") String submodelIdentifier);

    /**
     * Creates or updates an existing Submodel Descriptor by its unique identifier.
     * @param submodelIdentifier The Submodel’s unique id
     * @param body Submodel Descriptor object
     */
    @RequestLine("PUT /submodel-descriptors/{submodelIdentifier}")
    @Headers("Content-Type: application/json")
    @Body("{body}")
    void putSubmodelDescriptorById(@Param("submodelIdentifier") String submodelIdentifier, SubmodelDescriptor body);

    /**
     * Deletes a Submodel Descriptor (de-registers a submodel) by its unique identifier.
     * @param submodelIdentifier The Submodel’s unique id
     */
    @RequestLine("DELETE /submodel-descriptors/{submodelIdentifier}")
    void deleteSubmodelDescriptorById(@Param("submodelIdentifier") String submodelIdentifier);
}

