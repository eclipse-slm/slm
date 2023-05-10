package org.eclipse.slm.service_management.service.rest.service_categories;

import org.eclipse.slm.service_management.model.offerings.ServiceCategory;
import org.eclipse.slm.service_management.persistence.api.ServiceCategoryJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/***
 * Handles {@link ServiceCategory}s.
 */
@Component
public class ServiceCategoryHandler {

    public final static Logger LOG = LoggerFactory.getLogger(ServiceCategoryHandler.class);

    private final ServiceCategoryJpaRepository serviceCategoryJpaRepository;

    /***
     * Instantiates a new {@link ServiceCategoryHandler}.
     * @param serviceCategoryJpaRepository Repository to persist {@link ServiceCategory}s.
     */
    public ServiceCategoryHandler(ServiceCategoryJpaRepository serviceCategoryJpaRepository) {
        this.serviceCategoryJpaRepository = serviceCategoryJpaRepository;
    }

    /***
     * Gets all {@link ServiceCategory}s.
     * @return List of all {@link ServiceCategory}s.
     */
    public List<ServiceCategory> getServiceCategories() {
        var serviceCategories = this.serviceCategoryJpaRepository.findAll();
        return serviceCategories;
    }

    /***
     * Get a {@link ServiceCategory} by id.
     * @return {@link ServiceCategory} with found with specified id.
     */
    public ServiceCategory getServiceCategoryById(long serviceCategoryId) throws ServiceCategoryNotFoundException {
        var serviceCategoryOptional = this.serviceCategoryJpaRepository.findById(serviceCategoryId);
        if (serviceCategoryOptional.isPresent()) {
            return serviceCategoryOptional.get();
        } else {
            throw new ServiceCategoryNotFoundException("Service category with id '" + serviceCategoryId + "' not found");
        }
    }

    /***
     * Get a {@link ServiceCategory} by name.
     * @return {@link ServiceCategory} with found with specified name.
     */
    public List<ServiceCategory> getServiceCategoriesByName(String serviceCategoryName) throws ServiceCategoryNotFoundException {
        var serviceCategories = this.serviceCategoryJpaRepository.findByName(serviceCategoryName);
        if (serviceCategories.size() > 0) {
            return serviceCategories;
        } else {
            throw new ServiceCategoryNotFoundException("Service category with name '" + serviceCategoryName + "' not found");
        }
    }

    /***
     * Creates a new {@link ServiceCategory}.
     * @param serviceCategory The {@link ServiceCategory} to create.
     * @return The created {@link ServiceCategory}.
     */
    public ServiceCategory createServiceCategory(ServiceCategory serviceCategory) {
        var createdServiceCategory = this.serviceCategoryJpaRepository.save(serviceCategory);
        return createdServiceCategory;
    }

    /***
     * Creates a new {@link ServiceCategory} or updates the {@link ServiceCategory} if it already exists.
     * @param serviceCategory The {@link ServiceCategory} to create or update.
     * @return Created or updated {@link ServiceCategory}.
     */
    public ServiceCategory createOrUpdateServiceCategory(ServiceCategory serviceCategory) {
        var savedServiceCategory = this.serviceCategoryJpaRepository.findById(serviceCategory.getId());
        ServiceCategory createdOrUpdatedServiceCategory;
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
     * Deletes the {@link ServiceCategory} with the specified id.
     * @param serviceCategoryId Id of the {@link ServiceCategory} to delete.
     */
    public void deleteCategory(long serviceCategoryId) {
        this.serviceCategoryJpaRepository.deleteById(serviceCategoryId);
    }
}
