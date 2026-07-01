package org.eclipse.slm.service_management.features.service_offerings.impl.servicecategories;

import org.eclipse.slm.service_management.features.service_offerings.api.servicecategories.ServiceCategoryNotFoundException;
import org.eclipse.slm.service_management.features.service_offerings.api.servicecategories.ServiceOfferingCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/***
 * Handles {@link ServiceOfferingCategory}s.
 */
@Component
public class ServiceOfferingCategoryHandler {

    private final static Logger LOG = LoggerFactory.getLogger(ServiceOfferingCategoryHandler.class);

    private final ServiceCategoryJpaRepository serviceCategoryJpaRepository;

    /***
     * Instantiates a new {@link ServiceOfferingCategoryHandler}.
     * @param serviceCategoryJpaRepository Repository to persist {@link ServiceOfferingCategory}s.
     */
    public ServiceOfferingCategoryHandler(ServiceCategoryJpaRepository serviceCategoryJpaRepository) {
        this.serviceCategoryJpaRepository = serviceCategoryJpaRepository;
    }

    /***
     * Gets all {@link ServiceOfferingCategory}s.
     * @return List of all {@link ServiceOfferingCategory}s.
     */
    public List<ServiceOfferingCategory> getServiceCategories() {
        var serviceCategories = this.serviceCategoryJpaRepository.findAll();
        return serviceCategories;
    }

    /***
     * Get a {@link ServiceOfferingCategory} by id.
     * @return {@link ServiceOfferingCategory} with found with specified id.
     */
    public ServiceOfferingCategory getServiceCategoryById(long serviceCategoryId) throws ServiceCategoryNotFoundException {
        var serviceCategoryOptional = this.serviceCategoryJpaRepository.findById(serviceCategoryId);
        if (serviceCategoryOptional.isPresent()) {
            return serviceCategoryOptional.get();
        } else {
            throw new ServiceCategoryNotFoundException("Service category with id '" + serviceCategoryId + "' not found");
        }
    }

    /***
     * Get a {@link ServiceOfferingCategory} by name.
     * @return {@link ServiceOfferingCategory} with found with specified name.
     */
    public List<ServiceOfferingCategory> getServiceCategoriesByName(String serviceCategoryName) throws ServiceCategoryNotFoundException {
        var serviceCategories = this.serviceCategoryJpaRepository.findByName(serviceCategoryName);
        if (serviceCategories.size() > 0) {
            return serviceCategories;
        } else {
            throw new ServiceCategoryNotFoundException("Service category with name '" + serviceCategoryName + "' not found");
        }
    }

    /***
     * Creates a new {@link ServiceOfferingCategory}.
     * @param serviceCategory The {@link ServiceOfferingCategory} to create.
     * @return The created {@link ServiceOfferingCategory}.
     */
    public ServiceOfferingCategory createServiceCategory(ServiceOfferingCategory serviceCategory) {
        var createdServiceCategory = this.serviceCategoryJpaRepository.save(serviceCategory);
        return createdServiceCategory;
    }

    /***
     * Creates a new {@link ServiceOfferingCategory} or updates the {@link ServiceOfferingCategory} if it already exists.
     * @param serviceCategory The {@link ServiceOfferingCategory} to create or update.
     * @return Created or updated {@link ServiceOfferingCategory}.
     */
    public ServiceOfferingCategory createOrUpdateServiceCategory(ServiceOfferingCategory serviceCategory) {
        var savedServiceCategory = this.serviceCategoryJpaRepository.findById(serviceCategory.getId());
        ServiceOfferingCategory createdOrUpdatedServiceCategory;
        if (savedServiceCategory.isPresent())
        {
            var serviceCategoryUpdate = savedServiceCategory.get();
            serviceCategoryUpdate.setName(serviceCategory.getName());
            createdOrUpdatedServiceCategory = this.serviceCategoryJpaRepository.saveAndFlush(serviceCategoryUpdate);
            LOG.info("Service category with id '" + serviceCategory.getId() + "' updated");
        }
        else
        {
            createdOrUpdatedServiceCategory = this.serviceCategoryJpaRepository.saveAndFlush(serviceCategory);
            LOG.info("Service category with id '" + serviceCategory.getId() + "' created");
        }

        return createdOrUpdatedServiceCategory;
    }

    /***
     * Deletes the {@link ServiceOfferingCategory} with the specified id.
     * @param serviceCategoryId Id of the {@link ServiceOfferingCategory} to delete.
     */
    public void deleteCategory(long serviceCategoryId) {
        this.serviceCategoryJpaRepository.deleteById(serviceCategoryId);
    }
}

