package org.eclipse.slm.service_management.service.rest.service_categories;

import org.eclipse.slm.service_management.model.offerings.ServiceCategory;
import org.eclipse.slm.service_management.model.offerings.responses.ServiceCategoryCreateResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;

@RestController
@RequestMapping("/services/offerings/categories")
public class ServiceCategoriesRestController {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceCategoriesRestController.class);

    private final ServiceCategoryHandler serviceCategoryHandler;

    public ServiceCategoriesRestController(ServiceCategoryHandler serviceCategoryHandler) {
        this.serviceCategoryHandler = serviceCategoryHandler;
    }

    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get service categories")
    @ResponseBody
    public List<ServiceCategory> getServiceCategories() {
        var serviceCategories = this.serviceCategoryHandler.getServiceCategories();
        return serviceCategories;
    }

    @RequestMapping(value = "/{serviceCategoryId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get service categories")
    @ResponseBody
    public ServiceCategory getServiceCategoryById(@PathVariable(name = "serviceCategoryId") long serviceCategoryId)
            throws ServiceCategoryNotFoundException {
        try {
            var serviceCategory = this.serviceCategoryHandler.getServiceCategoryById(serviceCategoryId);
            return serviceCategory;
        } catch (ServiceCategoryNotFoundException e) {
            LOG.error(e.getMessage());
            throw e;
        }
    }

    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a service category")
    @ResponseBody
    public ResponseEntity<ServiceCategoryCreateResponse> createServiceCategory(@RequestBody ServiceCategory serviceCategory) {
        var createdServiceCategory = this.serviceCategoryHandler.createServiceCategory(serviceCategory);
        var response = new ServiceCategoryCreateResponse(createdServiceCategory.getId());

        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create or update a service category")
    @ResponseBody
    @Transactional
    public ResponseEntity<Void> createOrUpdateServiceCategory(@RequestBody ServiceCategory serviceCategory) {
        var createdOrUpdatedServiceCategory = this.serviceCategoryHandler.createOrUpdateServiceCategory(serviceCategory);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/{serviceCategoryId}", method = RequestMethod.DELETE)
    @Operation(summary = "Delete a service category")
    @ResponseBody
    public ResponseEntity<ServiceCategoryCreateResponse> deleteServiceCategories(@PathVariable("serviceCategoryId") long serviceCategoryId)
            throws ServiceCategoryNotFoundException {
        try {
            var serviceCategory =  this.serviceCategoryHandler.getServiceCategoryById(serviceCategoryId);
            this.serviceCategoryHandler.deleteCategory(serviceCategoryId);
            return ResponseEntity.ok().build();
        } catch (ServiceCategoryNotFoundException e) {
            LOG.error(e.getMessage());
            throw e;
        }
    }
}
