package org.eclipse.slm.service_management.features.service_offerings.impl.servicecategories;

import jakarta.transaction.Transactional;
import org.eclipse.slm.common.restserver.annotations.AuthorizedAsSlmUserOrApiKey;
import org.eclipse.slm.service_management.features.service_offerings.api.servicecategories.ServiceCategoryNotFoundException;
import org.eclipse.slm.service_management.features.service_offerings.api.servicecategories.ServiceOfferingCategoriesRestApi;
import org.eclipse.slm.service_management.features.service_offerings.api.servicecategories.ServiceOfferingCategoriesRestApiConfig;
import org.eclipse.slm.service_management.features.service_offerings.api.servicecategories.ServiceOfferingCategory;
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.responses.ServiceCategoryCreateResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ServiceOfferingCategoriesRestApiConfig.BASE_PATH)
@Tag(name = ServiceOfferingCategoriesRestApiConfig.TAG)
@AuthorizedAsSlmUserOrApiKey
public class ServiceOfferingOfferingCategoriesRestController implements ServiceOfferingCategoriesRestApi {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceOfferingOfferingCategoriesRestController.class);

    private final ServiceOfferingCategoryHandler serviceOfferingCategoryHandler;

    public ServiceOfferingOfferingCategoriesRestController(ServiceOfferingCategoryHandler serviceOfferingCategoryHandler) {
        this.serviceOfferingCategoryHandler = serviceOfferingCategoryHandler;
    }

    @Override
    public List<ServiceOfferingCategory> getServiceCategories() {
        var serviceCategories = this.serviceOfferingCategoryHandler.getServiceCategories();
        return serviceCategories;
    }

    @Override
    public ServiceOfferingCategory getServiceCategoryById(long serviceCategoryId)
            throws ServiceCategoryNotFoundException {
        try {
            var serviceCategory = this.serviceOfferingCategoryHandler.getServiceCategoryById(serviceCategoryId);
            return serviceCategory;
        } catch (ServiceCategoryNotFoundException e) {
            LOG.error(e.getMessage());
            throw e;
        }
    }

    @Override
    @AuthorizedAsSlmUserOrApiKey
    public ResponseEntity<ServiceCategoryCreateResponse> createServiceCategory(ServiceOfferingCategory serviceCategory) {
        var createdServiceCategory = this.serviceOfferingCategoryHandler.createServiceCategory(serviceCategory);
        var response = new ServiceCategoryCreateResponse(createdServiceCategory.getId());

        return ResponseEntity.ok(response);
    }

    @Transactional
    @Override
    @AuthorizedAsSlmUserOrApiKey
    public ResponseEntity<Void> createOrUpdateServiceCategory(ServiceOfferingCategory serviceCategory) {
        var createdOrUpdatedServiceCategory = this.serviceOfferingCategoryHandler.createOrUpdateServiceCategory(serviceCategory);
        return ResponseEntity.ok().build();
    }

    @Override
    @AuthorizedAsSlmUserOrApiKey
    public ResponseEntity<ServiceCategoryCreateResponse> deleteServiceCategories(long serviceCategoryId)
            throws ServiceCategoryNotFoundException {
        try {
            var serviceCategory =  this.serviceOfferingCategoryHandler.getServiceCategoryById(serviceCategoryId);
            this.serviceOfferingCategoryHandler.deleteCategory(serviceCategoryId);
            return ResponseEntity.ok().build();
        } catch (ServiceCategoryNotFoundException e) {
            LOG.error(e.getMessage());
            throw e;
        }
    }
}

