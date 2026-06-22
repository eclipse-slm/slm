package org.eclipse.slm.service_management.features.service_offerings.impl;

import org.eclipse.slm.service_management.features.service_offerings.api.categories.ServiceCategoryNotFoundException;
import org.eclipse.slm.service_management.features.service_offerings.impl.categories.ServiceOfferingCategoryHandler;

import org.eclipse.slm.service_management.features.service_offerings.api.categories.ServiceOfferingCategory;
import org.eclipse.slm.service_management.features.service_offerings.impl.categories.ServiceCategoryJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ContextConfiguration(classes = {
        ServiceOfferingCategoryHandler.class,
        ServiceCategoryJpaRepository.class
})
@EntityScan( basePackages = { "org.eclipse.slm.service_management.features.service_offerings.api" })
@EnableJpaRepositories(basePackages = "org.eclipse.slm.service_management.features.service_offerings.impl")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class ServiceOfferingCategoryHandlerTest {

    @Autowired
    private ServiceOfferingCategoryHandler serviceOfferingCategoryHandler;

    @Autowired
    private ServiceCategoryJpaRepository serviceCategoryJpaRepository;

    @Nested
    @DisplayName("Get service categories")
    public class getServiceCategories {

        @Test
        @DisplayName("Get all categories when no category is defined")
        public void getServiceCategoriesWithoutDefinedServiceCategory() {
            var serviceCategories = serviceOfferingCategoryHandler.getServiceCategories();
            assertThat(serviceCategories).hasSize(0);
        }

        @Test
        @DisplayName("Get all categories when one category is defined")
        public void getServiceCategories() {
            var serviceCategory = new ServiceOfferingCategory("TestCategory");
            serviceCategory = serviceCategoryJpaRepository.save(serviceCategory);

            var serviceCategories = serviceOfferingCategoryHandler.getServiceCategories();
            assertThat(serviceCategories).hasSize(1)
                    .usingRecursiveFieldByFieldElementComparator().contains(serviceCategory);
        }

        @Test
        @DisplayName("Get category by id")
        public void getCategoryById() throws ServiceCategoryNotFoundException {
            var serviceCategory = new ServiceOfferingCategory("TestCategory");
            serviceCategory = serviceCategoryJpaRepository.save(serviceCategory);

            var receivedServiceCategory = serviceOfferingCategoryHandler.getServiceCategoryById(serviceCategory.getId());
            assertThat(receivedServiceCategory).usingRecursiveComparison().isEqualTo(serviceCategory);
        }

        @Test
        @DisplayName("Get non existing category by id")
        public void getNonExistingCategoryById() throws ServiceCategoryNotFoundException {
            assertThatThrownBy(() -> {
                var serviceCategory = serviceOfferingCategoryHandler.getServiceCategoryById(1234);
            }).isInstanceOf(ServiceCategoryNotFoundException.class);
        }
    }

    @Test
    @DisplayName("Create service category")
    public void createServiceCategory() {
        var serviceCategory = new ServiceOfferingCategory("TestCategory");
        serviceCategory = this.serviceOfferingCategoryHandler.createServiceCategory(serviceCategory);

        var serviceCategories = this.serviceCategoryJpaRepository.findAll();
        assertThat(serviceCategories).hasSize(1)
                .usingRecursiveFieldByFieldElementComparator().contains(serviceCategory);
    }

    @Test
    @DisplayName("Create service category via createOrUpdate method")
    public void createServiceCategoryViaCreateOrUpdate() {
        var serviceCategory = new ServiceOfferingCategory("TestCategory");
        serviceCategory = this.serviceOfferingCategoryHandler.createOrUpdateServiceCategory(serviceCategory);

        var serviceCategories = this.serviceCategoryJpaRepository.findAll();
        assertThat(serviceCategories).hasSize(1)
                .usingRecursiveFieldByFieldElementComparator().contains(serviceCategory);
    }

    @Test
    @DisplayName("Update a service category")
    public void updateServiceCategory() {
        var serviceCategory = new ServiceOfferingCategory("TestCategory");
        serviceCategory = this.serviceCategoryJpaRepository.save(serviceCategory);

        serviceCategory.setName("UpdatedTestCategory");
        var updatedServiceCategory = this.serviceOfferingCategoryHandler.createOrUpdateServiceCategory(serviceCategory);

        var serviceCategories = this.serviceCategoryJpaRepository.findAll();
        assertThat(serviceCategories).hasSize(1)
                .usingRecursiveFieldByFieldElementComparator().contains(updatedServiceCategory);
    }

    @Test
    @DisplayName("Delete service category")
    public void deleteServiceCategory() {
        var serviceCategory = new ServiceOfferingCategory("TestCategory");
        serviceCategory = this.serviceCategoryJpaRepository.save(serviceCategory);
        var serviceCategories = this.serviceCategoryJpaRepository.findAll();
        assertThat(serviceCategories).hasSize(1)
                .usingRecursiveFieldByFieldElementComparator().contains(serviceCategory);

        this.serviceOfferingCategoryHandler.deleteCategory(serviceCategory.getId());

        serviceCategories = this.serviceCategoryJpaRepository.findAll();
        assertThat(serviceCategories).hasSize(0);
    }

}

