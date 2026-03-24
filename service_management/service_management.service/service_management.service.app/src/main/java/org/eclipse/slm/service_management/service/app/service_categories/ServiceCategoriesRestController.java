package org.eclipse.slm.service_management.service.app.service_categories;

import jakarta.transaction.Transactional;
import org.eclipse.slm.common.restserver.annotations.AuthorizedAsSlmUserOrApiKey;
import org.eclipse.slm.service_management.model.offerings.ServiceCategory;
import org.eclipse.slm.service_management.model.offerings.responses.ServiceCategoryCreateResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ServiceCategoriesRestApiConfig.BASE_PATH)
@Tag(name = ServiceCategoriesRestApiConfig.TAG)
@AuthorizedAsSlmUserOrApiKey
public class ServiceCategoriesRestController implements ServiceCategoriesRestApi {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceCategoriesRestController.class);

    private final ServiceCategoryHandler serviceCategoryHandler;

    public ServiceCategoriesRestController(ServiceCategoryHandler serviceCategoryHandler) {
        this.serviceCategoryHandler = serviceCategoryHandler;
    }

    @Override
    public List<ServiceCategory> getServiceCategories() {
        var serviceCategories = this.serviceCategoryHandler.getServiceCategories();
        return serviceCategories;
    }

    @Override
    public ServiceCategory getServiceCategoryById(long serviceCategoryId)
            throws ServiceCategoryNotFoundException {
        try {
            var serviceCategory = this.serviceCategoryHandler.getServiceCategoryById(serviceCategoryId);
            return serviceCategory;
        } catch (ServiceCategoryNotFoundException e) {
            LOG.error(e.getMessage());
            throw e;
        }
    }

    @Override
    @AuthorizedAsSlmUserOrApiKey
    public ResponseEntity<ServiceCategoryCreateResponse> createServiceCategory(ServiceCategory serviceCategory) {
        var createdServiceCategory = this.serviceCategoryHandler.createServiceCategory(serviceCategory);
        var response = new ServiceCategoryCreateResponse(createdServiceCategory.getId());

        return ResponseEntity.ok(response);
    }

    @Transactional
    @Override
    @AuthorizedAsSlmUserOrApiKey
    public ResponseEntity<Void> createOrUpdateServiceCategory(ServiceCategory serviceCategory) {
        var createdOrUpdatedServiceCategory = this.serviceCategoryHandler.createOrUpdateServiceCategory(serviceCategory);
        return ResponseEntity.ok().build();
    }

    @Override
    @AuthorizedAsSlmUserOrApiKey
    public ResponseEntity<ServiceCategoryCreateResponse> deleteServiceCategories(long serviceCategoryId)
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
