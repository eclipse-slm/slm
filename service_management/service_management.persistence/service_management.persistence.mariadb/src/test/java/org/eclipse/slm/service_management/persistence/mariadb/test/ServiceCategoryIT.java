package org.eclipse.slm.service_management.persistence.mariadb.test;

import org.eclipse.slm.service_management.model.offerings.ServiceCategory;
import org.eclipse.slm.service_management.persistence.api.ServiceCategoryJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = { SpringTestConfiguration.class })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class ServiceCategoryIT {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ServiceCategoryJpaRepository serviceCategoryJpaRepository;

    @DisplayName("Find all service categories")
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void findAllServiceCategories() {
        var serviceCategoryName1 = UUID.randomUUID().toString();
        var serviceCategoryName2 = UUID.randomUUID().toString();
        var testServiceCategory1 = new ServiceCategory(serviceCategoryName1);
        var testServiceCategory2 = new ServiceCategory(serviceCategoryName2);
        entityManager.persist(testServiceCategory1);
        entityManager.persist(testServiceCategory2);

        var serviceCategories = serviceCategoryJpaRepository.findAll();

        assertThat(serviceCategories).hasSize(2)
                .usingRecursiveFieldByFieldElementComparator()
                .contains(testServiceCategory1)
                .contains(testServiceCategory2);
    }

    @DisplayName("Find service category by id")
    @Test
    public void findServiceCategoryById() {
        var serviceCategoryName = UUID.randomUUID().toString();
        var testServiceCategory = new ServiceCategory(serviceCategoryName);
        testServiceCategory = entityManager.persist(testServiceCategory);

        var serviceVendorOptional = serviceCategoryJpaRepository.findById(testServiceCategory.getId());

        assertThat(serviceVendorOptional).isPresent();
    }

    @DisplayName("Find service categories by name")
    @Test
    public void findServiceCategoryByName() {
        var serviceCategoryName = UUID.randomUUID().toString();
        var testServiceCategory = new ServiceCategory(serviceCategoryName);
        testServiceCategory = entityManager.persist(testServiceCategory);
        var savedServiceCategory = entityManager.find(ServiceCategory.class, testServiceCategory.getId());
        assertThat(savedServiceCategory).isNotNull();

        var serviceCategories = serviceCategoryJpaRepository.findByName(serviceCategoryName);

        assertThat(serviceCategories)
                .hasSize(1)
                .usingRecursiveFieldByFieldElementComparator()
                .contains(testServiceCategory);
    }

    @DisplayName("Save service category")
    @Test
    public void saveServiceCategory() {
        var testServiceCategory = new ServiceCategory(UUID.randomUUID().toString());

        serviceCategoryJpaRepository.save(testServiceCategory);

        var persistedServiceCategories = serviceCategoryJpaRepository.findAll();
        var optionalServiceCategory = persistedServiceCategories.stream()
                .filter(serviceCategory -> serviceCategory.getName().equals(testServiceCategory.getName()))
                .findAny();
        assertThat(optionalServiceCategory).isPresent();
    }

    @DisplayName("Update service category")
    @Test
    public void updateServiceCategory() {
        var serviceCategoryName = UUID.randomUUID().toString();
        var testServiceCategory = new ServiceCategory(serviceCategoryName);
        testServiceCategory = entityManager.persist(testServiceCategory);
        var savedServiceCategory = entityManager.find(ServiceCategory.class, testServiceCategory.getId());
        assertThat(savedServiceCategory).isNotNull();
        assertThat(savedServiceCategory.getName()).isEqualTo(serviceCategoryName);

        var newName = UUID.randomUUID().toString();
        testServiceCategory.setName(newName);
        testServiceCategory = serviceCategoryJpaRepository.save(testServiceCategory);

        savedServiceCategory = entityManager.find(ServiceCategory.class, testServiceCategory.getId());
        assertThat(savedServiceCategory).isNotNull();
        assertThat(savedServiceCategory.getName()).isEqualTo(newName);
    }

    @DisplayName("Delete service category by id")
    @Test
    public void deleteServiceCategory() {
        var testServiceCategory = new ServiceCategory(UUID.randomUUID().toString());
        testServiceCategory = entityManager.persist(testServiceCategory);
        var savedServiceCategory = entityManager.find(ServiceCategory.class, testServiceCategory.getId());
        assertThat(savedServiceCategory).isNotNull();

        serviceCategoryJpaRepository.deleteById(testServiceCategory.getId());

        savedServiceCategory = entityManager.find(ServiceCategory.class, testServiceCategory.getId());
        assertThat(savedServiceCategory).isNull();
    }
}
