package org.eclipse.slm.service_management.features.service_offerings.api.offerings;

import io.swagger.v3.oas.annotations.Operation;
import org.eclipse.slm.service_management.features.service_offerings.api.service_repositories.ServiceRepository;
import org.eclipse.slm.service_management.features.service_offerings.api.service_repositories.ServiceRepositoryCreateResponse;
import org.eclipse.slm.service_management.features.service_offerings.api.service_repositories.ServiceRepositoryDTOApiRead;
import org.eclipse.slm.service_management.features.service_offerings.api.vendors.exceptions.ServiceVendorAccessDenied;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.UUID;

public interface ServiceRepositoriesRestApi {

    @RequestMapping(value = "/{serviceVendorId}/repositories", method = RequestMethod.GET)
    @Operation(summary = "Get repositories containing files for service offerings")
    ResponseEntity<List<ServiceRepositoryDTOApiRead>> getRepositories(
            @PathVariable(name = "serviceVendorId") UUID serviceVendorId
    ) throws ServiceVendorAccessDenied, ServiceRepositoryNotFound;

    @RequestMapping(value = "/{serviceVendorId}/repositories", method = RequestMethod.POST)
    @Operation(summary = "Create repository containing files for service offerings")
    ResponseEntity<ServiceRepositoryCreateResponse> createRepository(
            @PathVariable(name = "serviceVendorId") UUID serviceVendorId,
            @RequestBody ServiceRepository serviceRepository
    ) throws ServiceVendorAccessDenied;

    @RequestMapping(value = "/{serviceVendorId}/repositories/{serviceRepositoryId}", method = RequestMethod.PUT)
    @Operation(summary = "Create or update a repository containing files for service offerings")
    ResponseEntity<ServiceRepositoryCreateResponse> createOrUpdateRepository(
            @PathVariable(name = "serviceVendorId") UUID serviceVendorId,
            @PathVariable(name = "serviceRepositoryId") UUID serviceRepositoryId,
            @RequestBody ServiceRepository serviceRepository
    ) throws ServiceVendorAccessDenied;

    @RequestMapping(value = "/{serviceVendorId}/repositories/{repositoryId}", method = RequestMethod.DELETE)
    @Operation(summary = "Delete repository containing files for service offerings")
    ResponseEntity<Void> deleteRepository(
            @PathVariable(name = "serviceVendorId") UUID serviceVendorId,
            @PathVariable(name = "repositoryId") UUID repositoryId
    ) throws ServiceVendorAccessDenied;
}

