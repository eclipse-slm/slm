package org.eclipse.slm.common.aas.repositories.api.submodels;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.basyx.http.pagination.Base64UrlEncodedCursor;
import org.eclipse.digitaltwin.basyx.http.pagination.PagedResult;
import org.eclipse.digitaltwin.basyx.pagination.GetSubmodelElementsResult;
import org.eclipse.digitaltwin.basyx.submodelservice.value.SubmodelElementValue;
import org.eclipse.digitaltwin.basyx.submodelservice.value.SubmodelValueOnly;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Validated

@RequestMapping("{aasId}")
@ApiResponses(value = {
	@ApiResponse(responseCode = "400", description = "Bad Request, e.g. the request parameters of the format of the request body is wrong.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	@ApiResponse(responseCode = "401", description = "Unauthorized, e.g. the server refused the authorization attempt.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	@ApiResponse(responseCode = "501", description = "Method not implemented or supported for this submodel"),
	@ApiResponse(responseCode = "default", description = "Default error handling for unmentioned status codes", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class)))

})
public interface MultiSubmodelServiceHTTPApi {

	@Operation(	summary = "Deletes a submodel element at a specified path within the submodel elements hierarchy",
				tags = { "Submodel API" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Submodel element deleted successfully")
	})
	@RequestMapping(value = "/submodel/submodel-elements/{idShortPath}",
					method = RequestMethod.DELETE)
	ResponseEntity<Void> deleteSubmodelElementByPath(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel belongs to", required = true, schema = @Schema()) @PathVariable("aasId") String aasId,
			@Parameter(in = ParameterIn.PATH, description = "IdShort path to the submodel element (dot-separated)", required = true, schema = @Schema()) @PathVariable("idShortPath") String idShortPath);

	@Operation(	summary = "Returns all submodel elements including their hierarchy",
				tags = { "Submodel API" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "List of found submodel elements", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = GetSubmodelElementsResult.class))))
	})
	@RequestMapping(value = "/submodel/submodel-elements",
					method = RequestMethod.GET)
	ResponseEntity<PagedResult> getAllSubmodelElements(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel belongs to", required = true, schema = @Schema()) @PathVariable("aasId") String aasId,
			@Min(1) @Parameter(in = ParameterIn.QUERY, description = "The maximum number of elements in the response array", schema = @Schema(allowableValues = {
					"1" }, minimum = "1")) @Valid @RequestParam(value = "limit", required = false) Integer limit,
			@Parameter(in = ParameterIn.QUERY, description = "A server-generated identifier retrieved from pagingMetadata that specifies from which position the result listing should continue", schema = @Schema()) @Valid @RequestParam(value = "cursor", required = false) Base64UrlEncodedCursor cursor,
			@Parameter(in = ParameterIn.QUERY, description = "Determines the structural depth of the respective resource content", schema = @Schema(allowableValues = { "deep",
					"core" }, defaultValue = "deep")) @Valid @RequestParam(value = "level", required = false, defaultValue = "deep") String level,
			@Parameter(in = ParameterIn.QUERY, description = "Determines to which extent the resource is being serialized", schema = @Schema(allowableValues = { "withBlobValue",
					"withoutBlobValue" }, defaultValue = "withoutBlobValue")) @Valid @RequestParam(value = "extent", required = false, defaultValue = "withoutBlobValue") String extent);

	@Operation(summary = "Returns the Submodel",
			tags = { "Submodel API" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Requested Submodel", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Submodel.class))),
	})
	@RequestMapping(value = "/submodel",
					method = RequestMethod.GET)
	ResponseEntity<Submodel> getSubmodel(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel belongs to", required = true, schema = @Schema()) @PathVariable("aasId") String aasId,
			@Parameter(in = ParameterIn.QUERY, description = "Determines the structural depth of the respective resource content", schema = @Schema(allowableValues = { "deep",
					"core" }, defaultValue = "deep")) @Valid @RequestParam(value = "level", required = false, defaultValue = "deep") String level,
			@Parameter(in = ParameterIn.QUERY, description = "Determines to which extent the resource is being serialized", schema = @Schema(allowableValues = { "withBlobValue",
					"withoutBlobValue" }, defaultValue = "withoutBlobValue")) @Valid @RequestParam(value = "extent", required = false, defaultValue = "withoutBlobValue") String extent);

	@Operation(	summary = "Returns a specific submodel element from the Submodel at a specified path",
				tags = { "Submodel API" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Requested submodel element", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetSubmodelElementsResult.class))),
	})
	@RequestMapping(value = "/submodel/submodel-elements/{idShortPath}",
					method = RequestMethod.GET)
	ResponseEntity<SubmodelElement> getSubmodelElementByPath(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel belongs to", required = true, schema = @Schema()) @PathVariable("aasId") String aasId,
			@Parameter(in = ParameterIn.PATH, description = "IdShort path to the submodel element (dot-separated)", required = true, schema = @Schema()) @PathVariable("idShortPath") String idShortPath,
			@Min(1) @Parameter(in = ParameterIn.QUERY, description = "The maximum number of elements in the response array", schema = @Schema(allowableValues = {
					"1" }, minimum = "1")) @Valid @RequestParam(value = "limit", required = false) Integer limit,
			@Parameter(in = ParameterIn.QUERY, description = "A server-generated identifier retrieved from pagingMetadata that specifies from which position the result listing should continue", schema = @Schema()) @Valid @RequestParam(value = "cursor", required = false) Base64UrlEncodedCursor cursor,
			@Parameter(in = ParameterIn.QUERY, description = "Determines the structural depth of the respective resource content", schema = @Schema(allowableValues = { "deep",
					"core" }, defaultValue = "deep")) @Valid @RequestParam(value = "level", required = false, defaultValue = "deep") String level,
			@Parameter(in = ParameterIn.QUERY, description = "Determines to which extent the resource is being serialized", schema = @Schema(allowableValues = { "withBlobValue",
					"withoutBlobValue" }, defaultValue = "withoutBlobValue")) @Valid @RequestParam(value = "extent", required = false, defaultValue = "withoutBlobValue") String extent);

	@Operation(	summary = "Returns a specific submodel element from the Submodel at a specified path in the ValueOnly representation",
				tags = { "Submodel API" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Requested submodel element", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SubmodelElementValue.class))),
	})
	@RequestMapping(value = "/submodel/submodel-elements/{idShortPath}/$value",
					method = RequestMethod.GET)
	ResponseEntity<SubmodelElementValue> getSubmodelElementByPathValueOnly(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel belongs to", required = true, schema = @Schema()) @PathVariable("aasId") String aasId,
			@Parameter(in = ParameterIn.PATH, description = "IdShort path to the submodel element (dot-separated)", required = true, schema = @Schema()) @PathVariable("idShortPath") String idShortPath,
			@Min(1) @Parameter(in = ParameterIn.QUERY, description = "The maximum number of elements in the response array", schema = @Schema(allowableValues = {
					"1" }, minimum = "1")) @Valid @RequestParam(value = "limit", required = false) Integer limit,
			@Parameter(in = ParameterIn.QUERY, description = "A server-generated identifier retrieved from pagingMetadata that specifies from which position the result listing should continue", schema = @Schema()) @Valid @RequestParam(value = "cursor", required = false) Base64UrlEncodedCursor cursor,
			@Parameter(in = ParameterIn.QUERY, description = "Determines the structural depth of the respective resource content", schema = @Schema(allowableValues = { "deep",
					"core" }, defaultValue = "deep")) @Valid @RequestParam(value = "level", required = false, defaultValue = "deep") String level,
			@Parameter(in = ParameterIn.QUERY, description = "Determines to which extent the resource is being serialized", schema = @Schema(allowableValues = { "withBlobValue",
					"withoutBlobValue" }, defaultValue = "withoutBlobValue")) @Valid @RequestParam(value = "extent", required = false, defaultValue = "withoutBlobValue") String extent);

	@Operation(	summary = "Returns the metadata attributes of a specific Submodel",
				tags = { "Submodel API" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Requested Submodel in the metadata representation", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Submodel.class))),
	})
	@RequestMapping(value = "/submodel/$metadata",
					method = RequestMethod.GET)
	ResponseEntity<Submodel> getSubmodelMetadata(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel belongs to", required = true, schema = @Schema()) @PathVariable("aasId") String aasId,
			@Parameter(in = ParameterIn.QUERY, description = "Determines the structural depth of the respective resource content", schema = @Schema(allowableValues = { "deep",
			"core" }, defaultValue = "deep")) @Valid @RequestParam(value = "level", required = false, defaultValue = "deep") String level);

	@Operation(	summary = "Returns the Submodel in the ValueOnly representation",
				tags = { "Submodel API" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "ValueOnly representation of the Submodel", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SubmodelValueOnly.class))),
	})
	@RequestMapping(value = "/submodel/$value",
					method = RequestMethod.GET)
	ResponseEntity<SubmodelValueOnly> getSubmodelValueOnly(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel belongs to", required = true, schema = @Schema()) @PathVariable("aasId") String aasId,
			@Parameter(in = ParameterIn.QUERY, description = "Determines the structural depth of the respective resource content", schema = @Schema(allowableValues = { "deep",
					"core" }, defaultValue = "deep")) @Valid @RequestParam(value = "level", required = false, defaultValue = "deep") String level,
			@Parameter(in = ParameterIn.QUERY, description = "Determines to which extent the resource is being serialized", schema = @Schema(allowableValues = { "withBlobValue",
					"withoutBlobValue" }, defaultValue = "withoutBlobValue")) @Valid @RequestParam(value = "extent", required = false, defaultValue = "withoutBlobValue") String extent);

	@Operation(	summary = "Updates the value of an existing SubmodelElement",
				tags = { "Submodel API" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Submodel updated successfully"),
	})
	@RequestMapping(value = "/submodel/submodel-elements/{idShortPath}/$value",
					consumes = { "application/json" },
					method = RequestMethod.PATCH)
	ResponseEntity<Void> patchSubmodelElementByPathValueOnly(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel belongs to", required = true, schema = @Schema()) @PathVariable("aasId") String aasId,
			@Parameter(in = ParameterIn.PATH, description = "IdShort path to the submodel element (dot-separated)", required = true, schema = @Schema()) @PathVariable("idShortPath") String idShortPath,
			@Parameter(in = ParameterIn.DEFAULT, description = "The SubmodelElement in its ValueOnly representation", required = true, schema = @Schema()) @Valid @RequestBody SubmodelElementValue body,
			@Min(1) @Parameter(in = ParameterIn.QUERY, description = "The maximum number of elements in the response array", schema = @Schema(allowableValues = {
					"1" }, minimum = "1")) @Valid @RequestParam(value = "limit", required = false) Integer limit,
			@Parameter(in = ParameterIn.QUERY, description = "A server-generated identifier retrieved from pagingMetadata that specifies from which position the result listing should continue", schema = @Schema()) @Valid @RequestParam(value = "cursor", required = false) Base64UrlEncodedCursor cursor,
			@Parameter(in = ParameterIn.QUERY, description = "Determines the structural depth of the respective resource content", schema = @Schema(allowableValues = {
					"core" }, defaultValue = "core")) @Valid @RequestParam(value = "level", required = false, defaultValue = "core") String level);

	@Operation(	summary = "Creates a new submodel element",
				tags = { "Submodel API" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Submodel element created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SubmodelElement.class))),
	 })
	@RequestMapping(value = "/submodel/submodel-elements",
					consumes = { "application/json" },
					method = RequestMethod.POST)
	ResponseEntity<SubmodelElement> postSubmodelElement(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel belongs to", required = true, schema = @Schema()) @PathVariable("aasId") String aasId,
			@Parameter(in = ParameterIn.DEFAULT, description = "Requested submodel element", required = true, schema = @Schema()) @Valid @RequestBody SubmodelElement body,
			@Parameter(in = ParameterIn.QUERY, description = "Determines the structural depth of the respective resource content", schema = @Schema(allowableValues = { "deep",
					"core" }, defaultValue = "deep")) @Valid @RequestParam(value = "level", required = false, defaultValue = "deep") String level);

	@Operation(	summary = "Creates a new submodel element at a specified path within submodel elements hierarchy",
				tags = { "Submodel API" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Submodel element created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SubmodelElement.class))),
	})
	@RequestMapping(value = "/submodel/submodel-elements/{idShortPath}",
					consumes = { "application/json" },
					method = RequestMethod.POST)
	ResponseEntity<SubmodelElement> postSubmodelElementByPath(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel belongs to", required = true, schema = @Schema()) @PathVariable("aasId") String aasId,
			@Parameter(in = ParameterIn.PATH, description = "IdShort path to the submodel element (dot-separated)", required = true, schema = @Schema()) @PathVariable("idShortPath") String idShortPath,
			@Parameter(in = ParameterIn.DEFAULT, description = "Requested submodel element", required = true, schema = @Schema()) @Valid @RequestBody SubmodelElement body);

	@Operation(	summary = "Synchronously invokes an Operation at a specified path",
				tags = { "Submodel API" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Operation result object", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OperationResult.class))),
			@ApiResponse(responseCode = "405", description = "Method not allowed - Invoke only valid for Operation submodel element", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	 })
	@RequestMapping(value = "/submodel/submodel-elements/{idShortPath}/invoke",
					consumes = { "application/json" },
					method = RequestMethod.POST)
	ResponseEntity<OperationResult> invokeOperation(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel belongs to", required = true, schema = @Schema()) @PathVariable(name = "aasId") String aasId,
			@Parameter(in = ParameterIn.PATH, description = "IdShort path to the submodel element (dot-separated)", required = true, schema = @Schema()) @PathVariable("idShortPath") String idShortPath,
			@Parameter(in = ParameterIn.DEFAULT, description = "Operation request object", required = true, schema = @Schema()) @Valid @RequestBody OperationRequest body);

	@Operation(	summary = "Updates an existing submodel element at a specified path within submodel elements hierarchy",
				tags = { "Submodel API" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Submodel element updated successfully"),
	})
	@RequestMapping(value = "/submodel/submodel-elements/{idShortPath}",
					consumes = { "application/json" },
					method = RequestMethod.PUT)
	ResponseEntity<Void> putSubmodelElementByPath(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel belongs to", required = true, schema = @Schema()) @PathVariable("aasId") String aasId,
			@Parameter(in = ParameterIn.PATH, description = "IdShort path to the submodel element (dot-separated)", required = true, schema = @Schema()) @PathVariable("idShortPath") String idShortPath,
			@Parameter(in = ParameterIn.DEFAULT, description = "Requested submodel element", required = true, schema = @Schema()) @Valid @RequestBody SubmodelElement body,
			@Parameter(in = ParameterIn.QUERY, description = "Determines the structural depth of the respective resource content", schema = @Schema(allowableValues = {
					"deep" }, defaultValue = "deep")) @Valid @RequestParam(value = "level", required = false, defaultValue = "deep") String level);

	@Operation(	summary = "Updates the values of the Submodel",
				tags = { "Submodel API" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Submodel updated successfully"),
	})
	@RequestMapping(value = "/submodel/$value",
					consumes = { "application/json" },
					method = RequestMethod.PATCH)
	ResponseEntity<Void> patchSubmodelValueOnly(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel belongs to", required = true, schema = @Schema()) @PathVariable("aasId") String aasId,
			@Parameter(in = ParameterIn.DEFAULT, description = "Submodel object in its ValueOnly representation", required = false, schema = @Schema()) @Valid @RequestBody List<SubmodelElement> body,
			@Parameter(in = ParameterIn.QUERY, description = "Determines the structural depth of the respective resource content", schema = @Schema(allowableValues = {
					"deep" }, defaultValue = "deep")) @Valid @RequestParam(value = "level", required = false, defaultValue = "deep") String level);

	@Operation(	summary = "Deletes file content of an existing submodel element at a specified path within submodel elements hierarchy",
				tags = { "Submodel API" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Submodel element updated successfully")
	})
	@RequestMapping(value = "/submodel/submodel-elements/{idShortPath}/attachment",
					method = RequestMethod.DELETE)
	ResponseEntity<Void> deleteFileByPath(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel belongs to", required = true, schema = @Schema()) @PathVariable("aasId") String aasId,
			@Parameter(in = ParameterIn.PATH, description = "IdShort path to the submodel element (dot-separated)", required = true, schema = @Schema())
			@PathVariable("idShortPath") String idShortPath);

	@Operation(	summary = "Downloads file content from a specific submodel element from the Submodel at a specified path",
				tags = { "Submodel API" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Requested file", content = @Content(mediaType = "application/octet-stream", schema = @Schema(implementation = Resource.class))),
			@ApiResponse(responseCode = "405", description = "Method not allowed - Download only valid for File submodel element", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class)))
	})
	@RequestMapping(value = "/submodel/submodel-elements/{idShortPath}/attachment",
					method = RequestMethod.GET)
	ResponseEntity<Resource> getFileByPath(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel belongs to", required = true, schema = @Schema()) @PathVariable("aasId") String aasId,
			@Parameter(in = ParameterIn.PATH, description = "IdShort path to the submodel element (dot-separated)", required = true, schema = @Schema()) @PathVariable("idShortPath") String idShortPath);

	@Operation(summary = "Uploads file content to an existing submodel element at a specified path within submodel elements hierarchy",
			tags = { "Submodel API" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Submodel element updated successfully"),
			@ApiResponse(responseCode = "405", description = "Method not allowed - Upload only valid for File submodel element", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class)))
	})
	@RequestMapping(value = "/submodel/submodel-elements/{idShortPath}/attachment",
					consumes = { "multipart/form-data" },
					method = RequestMethod.PUT)
	ResponseEntity<Void> putFileByPath(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel belongs to", required = true, schema = @Schema()) @PathVariable("aasId") String aasId,
			@Parameter(in = ParameterIn.PATH, description = "IdShort path to the submodel element (dot-separated)", required = true, schema = @Schema()) @PathVariable("idShortPath") String idShortPath,
			@Parameter(in = ParameterIn.DEFAULT, description = "", required = true, schema = @Schema()) @RequestParam(value = "fileName", required = true) String fileName,
			@Parameter(description = "file detail") @Valid @RequestPart("file") MultipartFile file);
}
