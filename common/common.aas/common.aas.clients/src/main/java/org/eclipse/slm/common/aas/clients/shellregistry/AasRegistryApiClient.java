package org.eclipse.slm.common.aas.clients.shellregistry;

import feign.Body;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.eclipse.slm.common.aas.model.shellregistry.respones.GetAssetAdministrationShellDescriptorsResult;
import org.eclipse.slm.common.aas.model.shellregistry.respones.GetSubmodelDescriptorsResult;

public interface AasRegistryApiClient {

    /**
     * Returns a specific Asset Administration Shell Descriptor by its unique identifier (UTF8-BASE64-URL-encoded).
     * @param aasIdentifier The Asset Administration Shell’s unique id (UTF8-BASE64-URL-encoded)
     * @return The requested Asset Administration Shell Descriptor, or null if not found.
     */
    @RequestLine("GET /shell-descriptors/{aasIdentifier}")
    AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptorById(@Param("aasIdentifier") String aasIdentifier);

    /**
     * Returns all Asset Administration Shell Descriptors, optionally filtered by limit, cursor, assetKind, and assetType.
     * @param limit Maximum number of results to return
     * @param cursor Cursor for paginated results
     * @param assetKind Filter by asset kind (Instance, NotApplicable, Type)
     * @param assetType Filter by asset type
     * @return List of Asset Administration Shell Descriptors
     */
    @RequestLine("GET /shell-descriptors?limit={limit}&cursor={cursor}&assetKind={assetKind}&assetType={assetType}")
    GetAssetAdministrationShellDescriptorsResult getAllAssetAdministrationShellDescriptors(
        @Param("limit") Integer limit,
        @Param("cursor") String cursor,
        @Param("assetKind") AssetKind assetKind,
        @Param("assetType") String assetType
    );

    /**
     * Creates a new Asset Administration Shell Descriptor (registers an AAS).
     * @param body Asset Administration Shell Descriptor object
     * @return The created Asset Administration Shell Descriptor
     */
    @RequestLine("POST /shell-descriptors")
    @Headers("Content-Type: application/json")
    @Body("{body}")
    AssetAdministrationShellDescriptor postAssetAdministrationShellDescriptor(AssetAdministrationShellDescriptor body);

    /**
     * Updates an existing Asset Administration Shell Descriptor by its unique identifier.
     * @param aasIdentifier The Asset Administration Shell’s unique id (UTF8-BASE64-URL-encoded)
     * @param body Updated Asset Administration Shell Descriptor object
     */
    @RequestLine("PUT /shell-descriptors/{aasIdentifier}")
    @Headers("Content-Type: application/json")
    @Body("{body}")
    void putAssetAdministrationShellDescriptorById(@Param("aasIdentifier") String aasIdentifier, AssetAdministrationShellDescriptor body);

    /**
     * Deletes an Asset Administration Shell Descriptor (de-registers an AAS) by its unique identifier.
     * @param aasIdentifier The Asset Administration Shell’s unique id (UTF8-BASE64-URL-encoded)
     */
    @RequestLine("DELETE /shell-descriptors/{aasIdentifier}")
    void deleteAssetAdministrationShellDescriptorById(@Param("aasIdentifier") String aasIdentifier);

    /**
     * Returns all Submodel Descriptors for a specific Asset Administration Shell.
     * @param aasIdentifier The Asset Administration Shell’s unique id (UTF8-BASE64-URL-encoded)
     * @return List of Submodel Descriptors
     */
    @RequestLine("GET /shell-descriptors/{aasIdentifier}/submodel-descriptors")
    GetSubmodelDescriptorsResult getAllSubmodelDescriptors(@Param("aasIdentifier") String aasIdentifier);

    /**
     * Creates a new Submodel Descriptor (registers a submodel) for a specific Asset Administration Shell.
     * @param aasIdentifier The Asset Administration Shell’s unique id (UTF8-BASE64-URL-encoded)
     * @param body Submodel Descriptor object
     * @return The created Submodel Descriptor
     */
    @RequestLine("POST /shell-descriptors/{aasIdentifier}/submodel-descriptors")
    @Headers("Content-Type: application/json")
    @Body("{body}")
    SubmodelDescriptor postSubmodelDescriptor(@Param("aasIdentifier") String aasIdentifier, SubmodelDescriptor body);

    /**
     * Returns a specific Submodel Descriptor by its unique identifier for a given Asset Administration Shell.
     * @param aasIdentifier The Asset Administration Shell’s unique id (UTF8-BASE64-URL-encoded)
     * @param submodelIdentifier The Submodel’s unique id
     * @return The requested Submodel Descriptor, or null if not found
     */
    @RequestLine("GET /shell-descriptors/{aasIdentifier}/submodel-descriptors/{submodelIdentifier}")
    SubmodelDescriptor getSubmodelDescriptorById(@Param("aasIdentifier") String aasIdentifier, @Param("submodelIdentifier") String submodelIdentifier);

    /**
     * Updates an existing Submodel Descriptor by its unique identifier for a given Asset Administration Shell.
     * @param aasIdentifier The Asset Administration Shell’s unique id (UTF8-BASE64-URL-encoded)
     * @param submodelIdentifier The Submodel’s unique id
     * @param body Updated Submodel Descriptor object
     */
    @RequestLine("PUT /shell-descriptors/{aasIdentifier}/submodel-descriptors/{submodelIdentifier}")
    @Headers("Content-Type: application/json")
    @Body("{body}")
    void putSubmodelDescriptorById(@Param("aasIdentifier") String aasIdentifier, @Param("submodelIdentifier") String submodelIdentifier, SubmodelDescriptor body);

    /**
     * Deletes a Submodel Descriptor (de-registers a submodel) by its unique identifier for a given Asset Administration Shell.
     * @param aasIdentifier The Asset Administration Shell’s unique id (UTF8-BASE64-URL-encoded)
     * @param submodelIdentifier The Submodel’s unique id
     */
    @RequestLine("DELETE /shell-descriptors/{aasIdentifier}/submodel-descriptors/{submodelIdentifier}")
    void deleteSubmodelDescriptorById(@Param("aasIdentifier") String aasIdentifier, @Param("submodelIdentifier") String submodelIdentifier);

    /**
     * Returns the self-describing information of a network resource (ServiceDescription).
     * @return The requested ServiceDescription object
     */
    @RequestLine("GET /description")
    Object getDescription();

}
