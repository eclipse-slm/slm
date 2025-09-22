package org.eclipse.slm.common.aas.repositories.api.submodels;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Validated

@RequestMapping("{aasId}")
@ApiResponses(value = {
	 			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class)))
})
public interface MultiSubmodelRepositoryHTTPApi {

	@Operation(	summary = "Returns all Submodels", description = "")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Requested Submodels", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetSubmodelsResult.class))),
	})
	@RequestMapping(value = "/submodels", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<PagedResult> getAllSubmodels(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel repository belongs to (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("aasId") Base64UrlEncodedIdentifier aasId,
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
	@RequestMapping(value = "/submodels/$value", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<GetSubmodelsValueOnlyResult> getAllSubmodelsValueOnly(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel repository belongs to (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("aasId") Base64UrlEncodedIdentifier aasId,
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
	@RequestMapping(value = "/submodels/{submodelIdentifier}", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<Submodel> getSubmodelById(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel repository belongs to (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("aasId") Base64UrlEncodedIdentifier aasId,
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
	@RequestMapping(value = "/submodels/{submodelIdentifier}/$value", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<SubmodelValueOnly> getSubmodelByIdValueOnly(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel repository belongs to (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("aasId") Base64UrlEncodedIdentifier aasId,
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
	@RequestMapping(value = "/submodels/{submodelIdentifier}/$metadata", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<Submodel> getSubmodelByIdMetadata(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel repository belongs to (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("aasId") Base64UrlEncodedIdentifier aasId,
			@Parameter(in = ParameterIn.PATH, description = "The Submodel’s unique id (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("submodelIdentifier") Base64UrlEncodedIdentifier submodelIdentifier,
			@Parameter(in = ParameterIn.QUERY, description = "Determines the structural depth of the respective resource content", schema = @Schema(allowableValues = { "deep",
					"core" }, defaultValue = "deep")) @Valid @RequestParam(value = "level", required = false, defaultValue = "deep") String level);

	@Operation(summary = "Creates a new Submodel", description = "", hidden = true)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Submodel created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Submodel.class))),
			@ApiResponse(responseCode = "409", description = "Conflict, a resource which shall be created exists already. Might be thrown if a Submodel or SubmodelElement with the same ShortId is contained in a POST request.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	})
	@RequestMapping(value = "/submodels", produces = { "application/json" }, consumes = { "application/json" }, method = RequestMethod.POST)
	ResponseEntity<Submodel> postSubmodel(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel repository belongs to (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("aasId") Base64UrlEncodedIdentifier aasId,
			@Parameter(in = ParameterIn.DEFAULT, description = "Submodel object", required = true, schema = @Schema()) @Valid @RequestBody Submodel body
	);

	@Operation(summary = "Updates an existing Submodel", description = "", hidden = true)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Submodel updated successfully"),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	})
	@RequestMapping(value = "/submodels/{submodelIdentifier}", produces = { "application/json" }, consumes = { "application/json" }, method = RequestMethod.PUT)
	ResponseEntity<Void> putSubmodelById(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel repository belongs to (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("aasId") Base64UrlEncodedIdentifier aasId,
			@Parameter(in = ParameterIn.PATH, description = "The Submodel’s unique id (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("submodelIdentifier") Base64UrlEncodedIdentifier submodelIdentifier,
			@Parameter(in = ParameterIn.DEFAULT, description = "Submodel object", required = true, schema = @Schema()) @Valid @RequestBody Submodel body,
			@Parameter(in = ParameterIn.QUERY, description = "Determines the structural depth of the respective resource content", schema = @Schema(allowableValues = {
					"deep" }, defaultValue = "deep")) @Valid @RequestParam(value = "level", required = false, defaultValue = "deep") String level);

	@Operation(summary = "Deletes a Submodel", description = "", hidden = true)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Submodel deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	})
	@RequestMapping(value = "/submodels/{submodelIdentifier}", produces = { "application/json" }, method = RequestMethod.DELETE)
	ResponseEntity<Void> deleteSubmodelById(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel repository belongs to (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("aasId") Base64UrlEncodedIdentifier aasId,
			@Parameter(in = ParameterIn.PATH, description = "The Submodel’s unique id (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("submodelIdentifier") Base64UrlEncodedIdentifier submodelIdentifier);

	@Operation(summary = "Returns all submodel elements including their hierarchy", description = "")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "List of found submodel elements", content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	})
	@RequestMapping(value = "/submodels/{submodelIdentifier}/submodel-elements", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<PagedResult> getAllSubmodelElements(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel repository belongs to (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("aasId") Base64UrlEncodedIdentifier aasId,
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
	@RequestMapping(value = "/submodels/{submodelIdentifier}/submodel-elements/{idShortPath}", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<SubmodelElement> getSubmodelElementByPathSubmodelRepo(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel repository belongs to (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("aasId") Base64UrlEncodedIdentifier aasId,
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
	@RequestMapping(value = "/submodels/{submodelIdentifier}/submodel-elements/{idShortPath}/$value", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<SubmodelElementValue> getSubmodelElementByPathValueOnlySubmodelRepo(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel repository belongs to (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("aasId") Base64UrlEncodedIdentifier aasId,
			@Parameter(in = ParameterIn.PATH, description = "The Submodel’s unique id (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("submodelIdentifier") Base64UrlEncodedIdentifier submodelIdentifier,
			@Parameter(in = ParameterIn.PATH, description = "IdShort path to the submodel element (dot-separated)", required = true, schema = @Schema()) @PathVariable("idShortPath") String idShortPath,
			@Parameter(in = ParameterIn.QUERY, description = "Determines the structural depth of the respective resource content", schema = @Schema(allowableValues = { "deep",
					"core" }, defaultValue = "deep")) @Valid @RequestParam(value = "level", required = false, defaultValue = "deep") String level,
			@Parameter(in = ParameterIn.QUERY, description = "Determines to which extent the resource is being serialized", schema = @Schema(allowableValues = { "withBlobValue",
					"withoutBlobValue" }, defaultValue = "withoutBlobValue")) @Valid @RequestParam(value = "extent", required = false, defaultValue = "withoutBlobValue") String extent);

	@Operation(summary = "Updates the value of an existing SubmodelElement", description = "", hidden = true)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Submodel updated successfully"),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	})
	@RequestMapping(value = "/submodels/{submodelIdentifier}/submodel-elements/{idShortPath}/$value", produces = { "application/json" }, consumes = { "application/json" }, method = RequestMethod.PATCH)
	ResponseEntity<Void> patchSubmodelElementByPathValueOnlySubmodelRepo(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel repository belongs to (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("aasId") Base64UrlEncodedIdentifier aasId,
			@Parameter(in = ParameterIn.PATH, description = "The Submodel’s unique id (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("submodelIdentifier") Base64UrlEncodedIdentifier submodelIdentifier,
			@Parameter(in = ParameterIn.PATH, description = "IdShort path to the submodel element (dot-separated)", required = true, schema = @Schema()) @PathVariable("idShortPath") String idShortPath,
			@Parameter(in = ParameterIn.DEFAULT, description = "The SubmodelElement in its ValueOnly representation", required = true, schema = @Schema()) @Valid @RequestBody SubmodelElementValue body,
			@Parameter(in = ParameterIn.QUERY, description = "Determines the structural depth of the respective resource content", schema = @Schema(allowableValues = {
					"core" }, defaultValue = "core")) @Valid @RequestParam(value = "level", required = false, defaultValue = "core") String level);

	@Operation(summary = "Creates a new submodel element at a specified path within submodel elements hierarchy",
			   description = "", hidden = true)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Submodel element created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SubmodelElement.class))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
			@ApiResponse(responseCode = "409", description = "Conflict, a resource which shall be created exists already. Might be thrown if a Submodel or SubmodelElement with the same ShortId is contained in a POST request.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	})
	@RequestMapping(value = "/submodels/{submodelIdentifier}/submodel-elements/{idShortPath}", produces = { "application/json" }, consumes = { "application/json" }, method = RequestMethod.POST)
	ResponseEntity<SubmodelElement> postSubmodelElementByPathSubmodelRepo(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel repository belongs to (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("aasId") Base64UrlEncodedIdentifier aasId,
			@Parameter(in = ParameterIn.PATH, description = "The Submodel’s unique id (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("submodelIdentifier") Base64UrlEncodedIdentifier submodelIdentifier,
			@Parameter(in = ParameterIn.PATH, description = "IdShort path to the submodel element (dot-separated)", required = true, schema = @Schema()) @PathVariable("idShortPath") String idShortPath,
			@Parameter(in = ParameterIn.DEFAULT, description = "Requested submodel element", required = true, schema = @Schema()) @Valid @RequestBody SubmodelElement body,
			@Parameter(in = ParameterIn.QUERY, description = "Determines the structural depth of the respective resource content", schema = @Schema(allowableValues = { "deep",
					"core" }, defaultValue = "deep")) @Valid @RequestParam(value = "level", required = false, defaultValue = "deep") String level,
			@Parameter(in = ParameterIn.QUERY, description = "Determines to which extent the resource is being serialized", schema = @Schema(allowableValues = { "withBlobValue",
					"withoutBlobValue" }, defaultValue = "withoutBlobValue")) @Valid @RequestParam(value = "extent", required = false, defaultValue = "withoutBlobValue") String extent);

	@Operation(summary = "Creates a new submodel element",
			   description = "", hidden = true)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Submodel element created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SubmodelElement.class))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
			@ApiResponse(responseCode = "409", description = "Conflict, a resource which shall be created exists already. Might be thrown if a Submodel or SubmodelElement with the same ShortId is contained in a POST request.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	})
	@RequestMapping(value = "/submodels/{submodelIdentifier}/submodel-elements", produces = { "application/json" }, consumes = { "application/json" }, method = RequestMethod.POST)
	ResponseEntity<SubmodelElement> postSubmodelElementSubmodelRepo(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel repository belongs to (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("aasId") Base64UrlEncodedIdentifier aasId,
			@Parameter(in = ParameterIn.PATH, description = "The Submodel’s unique id (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("submodelIdentifier") Base64UrlEncodedIdentifier submodelIdentifier,
			@Parameter(in = ParameterIn.DEFAULT, description = "Requested submodel element", required = true, schema = @Schema()) @Valid @RequestBody SubmodelElement body);

	@Operation(summary = "Deletes a submodel element at a specified path within the submodel elements hierarchy",
			   description = "", hidden = true)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Submodel element deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	})
	@RequestMapping(value = "/submodels/{submodelIdentifier}/submodel-elements/{idShortPath}", produces = { "application/json" }, method = RequestMethod.DELETE)
	ResponseEntity<Void> deleteSubmodelElementByPathSubmodelRepo(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel repository belongs to (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("aasId") Base64UrlEncodedIdentifier aasId,
			@Parameter(in = ParameterIn.PATH, description = "The Submodel’s unique id (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("submodelIdentifier") Base64UrlEncodedIdentifier submodelIdentifier,
			@Parameter(in = ParameterIn.PATH, description = "IdShort path to the submodel element (dot-separated)", required = true, schema = @Schema()) @PathVariable("idShortPath") String idShortPath);

	@Operation(summary = "Downloads file content from a specific submodel element from the Submodel at a specified path",
			   description = "", hidden = true)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Requested file", content = @Content(mediaType = "application/octet-stream", schema = @Schema(implementation = Resource.class))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	})
	@RequestMapping(value = "/submodels/{submodelIdentifier}/submodel-elements/{idShortPath}/attachment", produces = { "application/octet-stream", "application/json" }, method = RequestMethod.GET)
	ResponseEntity<Resource> getFileByPath(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel repository belongs to (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("aasId") Base64UrlEncodedIdentifier aasId,
			@Parameter(in = ParameterIn.PATH, description = "The Submodel’s unique id (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("submodelIdentifier") Base64UrlEncodedIdentifier submodelIdentifier,
			@Parameter(in = ParameterIn.PATH, description = "IdShort path to the submodel element (dot-separated)", required = true, schema = @Schema()) @PathVariable("idShortPath") String idShortPath);

	@Operation(summary = "Uploads file content to an existing submodel element at a specified path within submodel elements hierarchy",
			   description = "", hidden = true)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Submodel element updated successfully"),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	})
	@RequestMapping(value = "/submodels/{submodelIdentifier}/submodel-elements/{idShortPath}/attachment", produces = { "application/json" }, consumes = { "multipart/form-data" }, method = RequestMethod.PUT)
	ResponseEntity<Void> putFileByPath(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel repository belongs to (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("aasId") Base64UrlEncodedIdentifier aasId,
			@Parameter(in = ParameterIn.PATH, description = "The Submodel’s unique id (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("submodelIdentifier") Base64UrlEncodedIdentifier submodelIdentifier,
			@Parameter(in = ParameterIn.PATH, description = "IdShort path to the submodel element (dot-separated)", required = true, schema = @Schema()) @PathVariable("idShortPath") String idShortPath,
			@Parameter(in = ParameterIn.DEFAULT, description = "", required = true, schema = @Schema()) @RequestParam(value = "fileName", required = true) String fileName,
			@Parameter(description = "file detail") @Valid @RequestPart("file") MultipartFile file);

	@Operation(summary = "Deletes file content of an existing submodel element at a specified path within submodel elements hierarchy",
			   description = "", hidden = true)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Submodel element updated successfully"),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	})
	@RequestMapping(value = "/submodels/{submodelIdentifier}/submodel-elements/{idShortPath}/attachment", produces = { "application/json" }, method = RequestMethod.DELETE)
	ResponseEntity<Void> deleteFileByPath(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel repository belongs to (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("aasId") Base64UrlEncodedIdentifier aasId,
			@Parameter(in = ParameterIn.PATH, description = "The Submodel’s unique id (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("submodelIdentifier") Base64UrlEncodedIdentifier submodelIdentifier,
			@Parameter(in = ParameterIn.PATH, description = "IdShort path to the submodel element (dot-separated)", required = true, schema = @Schema()) @PathVariable("idShortPath") String idShortPath);

	@Operation(summary = "Synchronously or asynchronously invokes an Operation at a specified path",
			   description = "", hidden = true)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Operation result object", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OperationResult.class))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
			@ApiResponse(responseCode = "405", description = "Method not allowed - Invoke only valid for Operation submodel element", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	})
	@RequestMapping(value = "/submodels/{submodelIdentifier}/submodel-elements/{idShortPath}/invoke", produces = { "application/json" }, consumes = { "application/json" }, method = RequestMethod.POST)
	ResponseEntity<OperationResult> invokeOperationSubmodelRepo(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel repository belongs to (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("aasId") Base64UrlEncodedIdentifier aasId,
			@Parameter(in = ParameterIn.PATH, description = "The Submodel’s unique id (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("submodelIdentifier") Base64UrlEncodedIdentifier submodelIdentifier,
			@Parameter(in = ParameterIn.PATH, description = "IdShort path to the submodel element (dot-separated)", required = true, schema = @Schema()) @PathVariable("idShortPath") String idShortPath,
			@Parameter(in = ParameterIn.DEFAULT, description = "Operation request object", required = true, schema = @Schema()) @Valid @RequestBody OperationRequest body,
			@Parameter(in = ParameterIn.QUERY, description = "Determines whether an operation invocation is performed asynchronously or synchronously", schema = @Schema(defaultValue = "false")) @Valid @RequestParam(value = "async", required = false, defaultValue = "false") Boolean async);

	@Operation(summary = "Updates an existing submodel element at a specified path within submodel elements hierarchy",
			   description = "", hidden = true)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Submodel element updated successfully"),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	})
	@RequestMapping(value = "/submodels/{submodelIdentifier}/submodel-elements/{idShortPath}", produces = { "application/json" }, consumes = { "application/json" }, method = RequestMethod.PUT)
	ResponseEntity<Void> putSubmodelElementByPathSubmodelRepo(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel repository belongs to (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("aasId") Base64UrlEncodedIdentifier aasId,
			@Parameter(in = ParameterIn.PATH, description = "The Submodel’s unique id (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("submodelIdentifier") Base64UrlEncodedIdentifier submodelIdentifier,
			@Parameter(in = ParameterIn.PATH, description = "IdShort path to the submodel element (dot-separated)", required = true, schema = @Schema()) @PathVariable("idShortPath") String idShortPath,
			@Parameter(in = ParameterIn.DEFAULT, description = "Requested submodel element", required = true, schema = @Schema()) @Valid @RequestBody SubmodelElement body,
			@Parameter(in = ParameterIn.QUERY, description = "Determines the structural depth of the respective resource content", schema = @Schema(allowableValues = {
					"deep" }, defaultValue = "deep")) @Valid @RequestParam(value = "level", required = false, defaultValue = "deep") String level);

	@Operation(summary = "Updates the values of an existing Submodel", description = "", hidden = true)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Submodel updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SubmodelValueOnly.class))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
	})
	@RequestMapping(value = "/submodels/{submodelIdentifier}/$value", produces = { "application/json" }, method = RequestMethod.PATCH)
	ResponseEntity<Void> patchSubmodelByIdValueOnly(
			@Parameter(in = ParameterIn.PATH, description = "Id of the AAS the submodel repository belongs to (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("aasId") Base64UrlEncodedIdentifier aasId,
			@Parameter(in = ParameterIn.PATH, description = "The Submodel’s unique id (UTF8-BASE64-URL-encoded)", required = true, schema = @Schema()) @PathVariable("submodelIdentifier") Base64UrlEncodedIdentifier submodelIdentifier,
			@Parameter(in = ParameterIn.DEFAULT, description = "Submodel object in its ValueOnly representation", required = false, schema = @Schema()) @Valid @RequestBody List<SubmodelElement> body,
			@Parameter(in = ParameterIn.QUERY, description = "Determines the structural depth of the respective resource content", schema = @Schema(allowableValues = { "deep",
					"core" }, defaultValue = "deep")) @Valid @RequestParam(value = "level", required = false, defaultValue = "deep") String level);
}
