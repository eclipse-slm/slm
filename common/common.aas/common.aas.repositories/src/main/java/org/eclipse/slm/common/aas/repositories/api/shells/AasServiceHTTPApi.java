package org.eclipse.slm.common.aas.repositories.api.shells;

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
import org.eclipse.digitaltwin.basyx.http.Base64UrlEncodedIdentifier;
import org.eclipse.digitaltwin.basyx.http.Base64UrlEncodedIdentifierSize;
import org.eclipse.digitaltwin.basyx.http.pagination.Base64UrlEncodedCursor;
import org.eclipse.digitaltwin.basyx.http.pagination.PagedResult;
import org.eclipse.digitaltwin.basyx.submodelrepository.http.pagination.GetSubmodelsResult;
import org.eclipse.digitaltwin.basyx.submodelservice.value.SubmodelElementValue;
import org.eclipse.slm.common.aas.repositories.api.submodels.GetSubmodelsValueOnlyResult;
import org.eclipse.slm.common.aas.repositories.api.submodels.SubmodelValueOnly;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("")
@ApiResponses(value = {
		@ApiResponse(responseCode = "401", description = "Unauthorized, e.g. the server refused the authorization attempt.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
		@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
		@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class)))
})
public interface AasServiceHTTPApi {

	@Operation(summary = "Returns a specific Asset Administration Shell", description = "")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Requested Asset Administration Shell", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AssetAdministrationShell.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request, e.g. the request parameters of the format of the request body is wrong.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	})
	@RequestMapping(value = "/aas", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<AssetAdministrationShell> getAssetAdministrationShell();

	@Operation(summary = "Returns the Asset Information", description = "")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Requested Asset Information", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AssetInformation.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request, e.g. the request parameters of the format of the request body is wrong.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	})
	@RequestMapping(value = "/aas/asset-information", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<AssetInformation> getAssetInformation();

	@Operation(summary = "Returns all submodel references", description = "")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Requested submodel references", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = GetReferencesResult.class)))),
			@ApiResponse(responseCode = "400", description = "Bad Request, e.g. the request parameters of the format of the request body is wrong.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	})
	@RequestMapping(value = "/aas/submodel-refs", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<PagedResult> getSubmodelReferences(
			@Min(0) @Parameter(in = ParameterIn.QUERY, description = "The maximum number of elements in the response array", schema = @Schema(allowableValues = {
					"1" }, minimum = "0")) @Valid @RequestParam(value = "limit", required = false) Integer limit,
			@Parameter(in = ParameterIn.QUERY, description = "A server-generated identifier retrieved from pagingMetadata that specifies from which position the result listing should continue", schema = @Schema()) @Valid @RequestParam(value = "cursor", required = false) Base64UrlEncodedCursor cursor);

	@Operation(summary = "Creates a submodel reference at the Asset Administration Shell", description = "")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Submodel reference created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Reference.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request, e.g. the request parameters of the format of the request body is wrong.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
			@ApiResponse(responseCode = "409", description = "Conflict, a resource which shall be created exists already. Might be thrown if a Submodel or SubmodelElement with the same ShortId is contained in a POST request.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	})
	@RequestMapping(value = "/aas/submodel-refs", produces = { "application/json" }, consumes = { "application/json" }, method = RequestMethod.POST)
	ResponseEntity<Reference> postSubmodelReferenceAas(
			@Parameter(in = ParameterIn.DEFAULT, description = "Reference to the Submodel", required = true, schema = @Schema()) @Valid @RequestBody Reference body);

	@Operation(summary = "Deletes the submodel reference from the Asset Administration Shell. Does not delete the submodel itself!", description = "")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Submodel reference deleted successfully"),
			@ApiResponse(responseCode = "400", description = "Bad Request, e.g. the request parameters of the format of the request body is wrong.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	})
	@RequestMapping(value = "/aas/submodel-refs/{submodelIdentifier}", produces = { "application/json" }, method = RequestMethod.DELETE)
	ResponseEntity<Void> deleteSubmodelReferenceByIdAas(
			@Parameter(in = ParameterIn.PATH, description = "The Submodel’s unique id (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("submodelIdentifier") Base64UrlEncodedIdentifier submodelIdentifier);

	// Submodel APIS

	@Operation(	summary = "Returns all Submodels", description = "")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Requested Submodels", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetSubmodelsResult.class))),
	})
	@RequestMapping(value = "/submodels", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<PagedResult> getAllSubmodels(
			@Base64UrlEncodedIdentifierSize(min = 1, max = 3072) @Parameter(in = ParameterIn.QUERY, description = "The value of the semantic id reference (UTF8-BASE64-URL-encoded)", schema = @Schema(implementation = String.class)) @Valid @RequestParam(value = "semanticId", required = false) Base64UrlEncodedIdentifier semanticId,
			@Parameter(in = ParameterIn.QUERY, description = "The Asset Administration Shell’s IdShort", schema = @Schema()) @Valid @RequestParam(value = "idShort", required = false) String idShort,
			@Min(1) @Parameter(in = ParameterIn.QUERY, description = "The maximum number of elements in the response array", schema = @Schema(allowableValues = {
					"1" }, minimum = "1")) @Valid @RequestParam(value = "limit", required = false) Integer limit,
			@Parameter(in = ParameterIn.QUERY, description = "A server-generated identifier retrieved from pagingMetadata that specifies from which position the result listing should continue", schema = @Schema()) @Valid @RequestParam(value = "cursor", required = false) Base64UrlEncodedCursor cursor,
			@Parameter(in = ParameterIn.QUERY, description = "Determines the structural depth of the respective resource content", schema = @Schema(allowableValues = { "deep",
					"core" }, defaultValue = "deep")) @Valid @RequestParam(value = "level", required = false, defaultValue = "deep") String level,
			@Parameter(in = ParameterIn.QUERY, description = "Determines to which extent the resource is being serialized", schema = @Schema(allowableValues = { "withBlobValue",
					"withoutBlobValue" }, defaultValue = "withoutBlobValue")) @Valid @RequestParam(value = "extent", required = false, defaultValue = "withoutBlobValue") String extent);

	@Operation(	summary = "Returns all Submodels in their ValueOnly representation", description = "")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Requested Submodels in value only representation", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetSubmodelsValueOnlyResult.class))),
	})
	@RequestMapping(value = "/submodels/aas/$value", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<GetSubmodelsValueOnlyResult> getAllSubmodelsValueOnly(
			@Base64UrlEncodedIdentifierSize(min = 1, max = 3072) @Parameter(in = ParameterIn.QUERY, description = "The value of the semantic id reference (UTF8-BASE64-URL-encoded)", schema = @Schema(implementation = String.class)) @Valid @RequestParam(value = "semanticId", required = false) Base64UrlEncodedIdentifier semanticId,
			@Parameter(in = ParameterIn.QUERY, description = "The Asset Administration Shell’s IdShort", schema = @Schema()) @Valid @RequestParam(value = "idShort", required = false) String idShort,
			@Min(1) @Parameter(in = ParameterIn.QUERY, description = "The maximum number of elements in the response array", schema = @Schema(allowableValues = {
					"1" }, minimum = "1")) @Valid @RequestParam(value = "limit", required = false) Integer limit,
			@Parameter(in = ParameterIn.QUERY, description = "A server-generated identifier retrieved from pagingMetadata that specifies from which position the result listing should continue", schema = @Schema()) @Valid @RequestParam(value = "cursor", required = false) Base64UrlEncodedCursor cursor,
			@Parameter(in = ParameterIn.QUERY, description = "Determines the structural depth of the respective resource content", schema = @Schema(allowableValues = { "deep",
					"core" }, defaultValue = "deep")) @Valid @RequestParam(value = "level", required = false, defaultValue = "deep") String level,
			@Parameter(in = ParameterIn.QUERY, description = "Determines to which extent the resource is being serialized", schema = @Schema(allowableValues = { "withBlobValue",
					"withoutBlobValue" }, defaultValue = "withoutBlobValue")) @Valid @RequestParam(value = "extent", required = false, defaultValue = "withoutBlobValue") String extent);

	@Operation(summary = "Returns a specific Submodel", description = "")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Requested Submodel", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Submodel.class))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	})
	@RequestMapping(value = "/submodels/aas/{submodelIdentifier}", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<Submodel> getSubmodelById(
			@Parameter(in = ParameterIn.PATH, description = "The Submodel’s unique id (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema(implementation = String.class)) @PathVariable("submodelIdentifier") Base64UrlEncodedIdentifier submodelIdentifier,
			@Parameter(in = ParameterIn.QUERY, description = "Determines the structural depth of the respective resource content", schema = @Schema(allowableValues = { "deep",
					"core" }, defaultValue = "deep")) @Valid @RequestParam(value = "level", required = false, defaultValue = "deep") String level,
			@Parameter(in = ParameterIn.QUERY, description = "Determines to which extent the resource is being serialized", schema = @Schema(allowableValues = { "withBlobValue",
					"withoutBlobValue" }, defaultValue = "withoutBlobValue")) @Valid @RequestParam(value = "extent", required = false, defaultValue = "withoutBlobValue") String extent);

	@Operation(summary = "Returns a specific Submodel in the value only representation", description = "")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Requested Submodel in value only representation", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SubmodelValueOnly.class))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	})
	@RequestMapping(value = "/submodels/aas/{submodelIdentifier}/$value", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<SubmodelValueOnly> getSubmodelByIdValueOnly(
			@Parameter(in = ParameterIn.PATH, description = "The Submodel’s unique id (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("submodelIdentifier") Base64UrlEncodedIdentifier submodelIdentifier,
			@Parameter(in = ParameterIn.QUERY, description = "Determines the structural depth of the respective resource content", schema = @Schema(allowableValues = { "deep",
					"core" }, defaultValue = "deep")) @Valid @RequestParam(value = "level", required = false, defaultValue = "deep") String level,
			@Parameter(in = ParameterIn.QUERY, description = "Determines to which extent the resource is being serialized", schema = @Schema(allowableValues = { "withBlobValue",
					"withoutBlobValue" }, defaultValue = "withoutBlobValue")) @Valid @RequestParam(value = "extent", required = false, defaultValue = "withoutBlobValue") String extent);

	@Operation(summary = "Returns the metadata attributes of a specific Submodel", description = "")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Requested Submodel in the metadata representation", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Submodel.class))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	})
	@RequestMapping(value = "/submodels/aas/{submodelIdentifier}/$metadata", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<Submodel> getSubmodelByIdMetadata(
			@Parameter(in = ParameterIn.PATH, description = "The Submodel’s unique id (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("submodelIdentifier") Base64UrlEncodedIdentifier submodelIdentifier,
			@Parameter(in = ParameterIn.QUERY, description = "Determines the structural depth of the respective resource content", schema = @Schema(allowableValues = { "deep",
					"core" }, defaultValue = "deep")) @Valid @RequestParam(value = "level", required = false, defaultValue = "deep") String level);

	@RequestMapping(value = "/submodels/aas/{submodelIdentifier}/submodel-elements", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<PagedResult> getAllSubmodelElements(
			@Parameter(in = ParameterIn.PATH, description = "The Submodel’s unique id (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("submodelIdentifier") Base64UrlEncodedIdentifier submodelIdentifier,
			@Min(1) @Parameter(in = ParameterIn.QUERY, description = "The maximum number of elements in the response array", schema = @Schema(allowableValues = {
					"1" }, minimum = "1")) @Valid @RequestParam(value = "limit", required = false) Integer limit,
			@Parameter(in = ParameterIn.QUERY, description = "A server-generated identifier retrieved from pagingMetadata that specifies from which position the result listing should continue", schema = @Schema()) @Valid @RequestParam(value = "cursor", required = false) Base64UrlEncodedCursor cursor,
			@Parameter(in = ParameterIn.QUERY, description = "Determines the structural depth of the respective resource content", schema = @Schema(allowableValues = { "deep",
					"core" }, defaultValue = "deep")) @Valid @RequestParam(value = "level", required = false, defaultValue = "deep") String level,
			@Parameter(in = ParameterIn.QUERY, description = "Determines to which extent the resource is being serialized", schema = @Schema(allowableValues = { "withBlobValue",
					"withoutBlobValue" }, defaultValue = "withoutBlobValue")) @Valid @RequestParam(value = "extent", required = false, defaultValue = "withoutBlobValue") String extent);

	@Operation(summary = "Returns a specific submodel element from the Submodel at a specified path", description = "")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Requested submodel element", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SubmodelElement.class))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	})
	@RequestMapping(value = "/submodels/aas/{submodelIdentifier}/submodel-elements/{idShortPath}", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<SubmodelElement> getSubmodelElementByPathSubmodelRepo(
			@Parameter(in = ParameterIn.PATH, description = "The Submodel’s unique id (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("submodelIdentifier") Base64UrlEncodedIdentifier submodelIdentifier,
			@Parameter(in = ParameterIn.PATH, description = "IdShort path to the submodel element (dot-separated)", required = true, schema = @Schema()) @PathVariable("idShortPath") String idShortPath,
			@Parameter(in = ParameterIn.QUERY, description = "Determines the structural depth of the respective resource content", schema = @Schema(allowableValues = { "deep",
					"core" }, defaultValue = "deep")) @Valid @RequestParam(value = "level", required = false, defaultValue = "deep") String level,
			@Parameter(in = ParameterIn.QUERY, description = "Determines to which extent the resource is being serialized", schema = @Schema(allowableValues = { "withBlobValue",
					"withoutBlobValue" }, defaultValue = "withoutBlobValue")) @Valid @RequestParam(value = "extent", required = false, defaultValue = "withoutBlobValue") String extent);

	@Operation(summary = "Returns a specific submodel element from the Submodel at a specified path in the ValueOnly representation", description = "")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Requested submodel element", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SubmodelElementValue.class))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	})
	@RequestMapping(value = "/submodels/aas/{submodelIdentifier}/submodel-elements/{idShortPath}/$value", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<SubmodelElementValue> getSubmodelElementByPathValueOnlySubmodelRepo(
			@Parameter(in = ParameterIn.PATH, description = "The Submodel’s unique id (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("submodelIdentifier") Base64UrlEncodedIdentifier submodelIdentifier,
			@Parameter(in = ParameterIn.PATH, description = "IdShort path to the submodel element (dot-separated)", required = true, schema = @Schema()) @PathVariable("idShortPath") String idShortPath,
			@Parameter(in = ParameterIn.QUERY, description = "Determines the structural depth of the respective resource content", schema = @Schema(allowableValues = { "deep",
					"core" }, defaultValue = "deep")) @Valid @RequestParam(value = "level", required = false, defaultValue = "deep") String level,
			@Parameter(in = ParameterIn.QUERY, description = "Determines to which extent the resource is being serialized", schema = @Schema(allowableValues = { "withBlobValue",
					"withoutBlobValue" }, defaultValue = "withoutBlobValue")) @Valid @RequestParam(value = "extent", required = false, defaultValue = "withoutBlobValue") String extent);

	@Operation(summary = "Downloads file content from a specific submodel element from the Submodel at a specified path",
			description = "", hidden = true)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Requested file", content = @Content(mediaType = "application/octet-stream", schema = @Schema(implementation = Resource.class))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	})
	@RequestMapping(value = "/submodels/aas/{submodelIdentifier}/submodel-elements/{idShortPath}/attachment", produces = { "application/octet-stream", "application/json" }, method = RequestMethod.GET)
	ResponseEntity<Resource> getFileByPath(
			@Parameter(in = ParameterIn.PATH, description = "The Submodel’s unique id (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("submodelIdentifier") Base64UrlEncodedIdentifier submodelIdentifier,
			@Parameter(in = ParameterIn.PATH, description = "IdShort path to the submodel element (dot-separated)", required = true, schema = @Schema()) @PathVariable("idShortPath") String idShortPath);
}
