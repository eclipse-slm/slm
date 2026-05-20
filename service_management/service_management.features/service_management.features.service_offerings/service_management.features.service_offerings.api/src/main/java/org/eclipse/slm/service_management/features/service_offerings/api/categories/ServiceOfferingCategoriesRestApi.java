package org.eclipse.slm.service_management.features.service_offerings.api.categories;

import io.swagger.v3.oas.annotations.Operation;
import org.eclipse.slm.service_management.features.service_offerings.api.offerings.responses.ServiceCategoryCreateResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

public interface ServiceOfferingCategoriesRestApi {

    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get service categories")
    @ResponseBody
    List<ServiceOfferingCategory> getServiceCategories();

    @RequestMapping(value = "/{serviceCategoryId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get service categories")
    @ResponseBody
    ServiceOfferingCategory getServiceCategoryById(
            @PathVariable(name = "serviceCategoryId") long serviceCategoryId
    ) throws ServiceCategoryNotFoundException;

    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a service category")
    @ResponseBody
    ResponseEntity<ServiceCategoryCreateResponse> createServiceCategory(
            @RequestBody ServiceOfferingCategory serviceCategory
    );

    @RequestMapping(value = "", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create or update a service category")
    @ResponseBody
    ResponseEntity<Void> createOrUpdateServiceCategory(
            @RequestBody ServiceOfferingCategory serviceCategory
    );

    @RequestMapping(value = "/{serviceCategoryId}", method = RequestMethod.DELETE)
    @Operation(summary = "Delete a service category")
    @ResponseBody
    ResponseEntity<ServiceCategoryCreateResponse> deleteServiceCategories(
            @PathVariable(name = "serviceCategoryId") long serviceCategoryId
    ) throws ServiceCategoryNotFoundException;
}

