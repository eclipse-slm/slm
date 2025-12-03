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
import org.eclipse.slm.common.aas.model.submodelrepository.responses.SubmodelQueryResult;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("")
@Tag(name = "Submodel Repository API")
@ApiResponses(value = {
        @ApiResponse(responseCode = "default", description = "Default error handling for unmentioned status codes", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class)))
})
public interface SubmodelRepositoryQueryApi {

    @Operation(	summary = "Returns all Submodels that confirm to the input query")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request, e.g. the request parameters of the format of the request body is wrong.",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized, e.g. the server refused the authorization attempt.",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json",
                         schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json",
                         schema = @Schema(implementation = Result.class))),
    })
    @RequestMapping(value = "/query/submodels",
            consumes = { MediaType.APPLICATION_JSON_VALUE,  MediaType.TEXT_PLAIN_VALUE },
            method = RequestMethod.POST)
    SubmodelQueryResult querySubmodels(@Parameter(in = ParameterIn.QUERY, description = "The maximum number of elements in the response array",
                                               schema = @Schema(allowableValues = { "1" }, minimum = "1")) @Min(1) @Valid
                                       @RequestParam(value = "limit", required = false, defaultValue = "100") Integer limit,
                                       @Parameter(in = ParameterIn.QUERY, description = "A server-generated identifier retrieved from pagingMetadata " +
                                               "that specifies from which position the result listing should continue", schema = @Schema()) @Valid
                                       @RequestParam(value = "cursor", required = false) String base64UrlEncodedCursor,
                                       @Parameter(in = ParameterIn.DEFAULT, description = "Query object", required = true, schema = @Schema()) @Valid
                                       @RequestBody String query
    );

}
