package org.eclipse.slm.common.aas.repositories.api.submodels;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.eclipse.digitaltwin.aas4j.v3.model.Result;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.eclipse.slm.common.aas.model.submodelregistry.respones.GetSubmodelDescriptorsResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

// According to V3.1.1_SSP-002 (Read Profile): https://app.swaggerhub.com/apis/Plattform_i40/SubmodelRegistryServiceSpecification/V3.1.1_SSP-002
@RequestMapping("")
@Tag(name = "Submodel Registry API")
@ApiResponses(value = {
        @ApiResponse(responseCode = "default", description = "Default error handling for unmentioned status codes",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request, e.g. the request parameters of the format of the request body is wrong.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Result.class))),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Result.class))),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Result.class))),
})
public interface SubmodelRegistryHTTPApi {

    @Operation(summary = "Returns all Submodel Descriptors")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Requested Submodel Descriptors", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = GetSubmodelDescriptorsResult.class)))
    })
    @RequestMapping(value = "/submodel-descriptors",
            produces = { "application/json" },
            method = RequestMethod.GET)
    ResponseEntity<GetSubmodelDescriptorsResult> getAllSubmodelDescriptors(
            @Parameter(in = ParameterIn.QUERY, description = "The maximum number of elements in the response array",
                    schema=@Schema(allowableValues={ "1" }, minimum="1")) @Min(1) @Valid
            @RequestParam(value = "limit", required = false, defaultValue = "100") Integer limit,
            @Parameter(in = ParameterIn.QUERY, description = "A server-generated identifier retrieved from pagingMetadata that specifies from which position " +
                    "the result listing should continue" ,schema=@Schema()) @Valid
            @RequestParam(value = "cursor", required = false) String cursor
    );

    @Operation(summary = "Returns a specific Submodel Descriptor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Requested Submodel Descriptor", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SubmodelDescriptor.class)))
    })
    @RequestMapping(value = "/submodel-descriptors/{submodelIdentifier}",
            produces = { "application/json" },
            method = RequestMethod.GET)
    ResponseEntity<SubmodelDescriptor> getSubmodelDescriptorById(
            @Parameter(in = ParameterIn.PATH, description = "The Submodel’s unique id (UTF8-BASE64-URL-encoded)", required=true, schema=@Schema())
            @PathVariable("submodelIdentifier") String submodelIdentifierBase64Encoded
    );

}
