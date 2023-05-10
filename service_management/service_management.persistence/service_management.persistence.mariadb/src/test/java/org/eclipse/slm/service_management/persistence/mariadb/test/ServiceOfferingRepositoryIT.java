package org.eclipse.slm.service_management.persistence.mariadb.test;

import org.eclipse.slm.service_management.model.offerings.ServiceCategory;
import org.eclipse.slm.service_management.model.offerings.ServiceOffering;
import org.eclipse.slm.service_management.model.vendors.ServiceVendor;
import org.eclipse.slm.service_management.persistence.api.ServiceOfferingJpaRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@DataJpaTest
@ContextConfiguration(classes = { SpringTestConfiguration.class })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class ServiceOfferingRepositoryIT {

    @Autowired
    protected ServiceOfferingJpaRepository serviceOfferingJpaRepository;

    @Autowired
    private TestEntityManager entityManager;

    protected ServiceCategory sampleServiceCategory = new ServiceCategory(UUID.randomUUID().toString());

    protected ServiceVendor sampleServiceVendor = new ServiceVendor(UUID.randomUUID());

    @BeforeEach
    public void init() {
        sampleServiceCategory = entityManager.merge(sampleServiceCategory);
        sampleServiceVendor = entityManager.merge(sampleServiceVendor);
    }

    @Nested
    @TestInstance(PER_CLASS)
    public class find {
        @DisplayName("Find all service offerings")
        @Test
        public void findAllServiceOfferings() {
            var testServiceOffering1 = new ServiceOffering(UUID.randomUUID());
            testServiceOffering1.setServiceCategory(sampleServiceCategory);
            testServiceOffering1.setServiceVendor(sampleServiceVendor);
            var testServiceOffering2 = new ServiceOffering(UUID.randomUUID());
            testServiceOffering2.setServiceCategory(sampleServiceCategory);
            testServiceOffering2.setServiceVendor(sampleServiceVendor);
            testServiceOffering1 = entityManager.merge(testServiceOffering1);
            testServiceOffering2 = entityManager.merge(testServiceOffering2);

            var serviceOfferings = serviceOfferingJpaRepository.findAll();

            assertThat(serviceOfferings).hasSize(2)
                    .usingRecursiveFieldByFieldElementComparator()
                    .contains(testServiceOffering1)
                    .contains(testServiceOffering2);
        }

        @DisplayName("Find service offering by id")
        @Test
        public void findServiceOfferingById() {
            var serviceOfferingId = UUID.randomUUID();
            var testServiceOffering = new ServiceOffering(serviceOfferingId);
            testServiceOffering.setServiceCategory(sampleServiceCategory);
            testServiceOffering.setServiceVendor(sampleServiceVendor);
            entityManager.persist(testServiceOffering);

            var serviceOfferingOptional = serviceOfferingJpaRepository.findById(serviceOfferingId);

            assertThat(serviceOfferingOptional).isPresent();
        }
    }

    @Nested
    @TestInstance(PER_CLASS)
    public class save {

        @Test
        @DisplayName("Save service offering with missing service vendor and missing service category")
        public void saveWithMissingServiceVendorAndServiceCategory() {
            var testServiceOffering = new ServiceOffering(UUID.randomUUID());

            Exception exception = assertThrows(DataIntegrityViolationException.class, () -> {
                serviceOfferingJpaRepository.save(testServiceOffering);
            });

            assertThat(exception.getMessage())
                    .contains("not-null property references a null or transient value");
        }

        @Test
        @DisplayName("Save service offering with missing service vendor")
        public void saveWithMissingServiceVendor() {
            var testServiceOffering = new ServiceOffering(UUID.randomUUID());
            testServiceOffering.setServiceCategory(sampleServiceCategory);

            Exception exception = assertThrows(DataIntegrityViolationException.class, () -> {
                serviceOfferingJpaRepository.save(testServiceOffering);
            });

            assertThat(exception.getMessage())
                    .contains("not-null property references a null or transient value")
                    .contains("org.eclipse.slm.service_management.model.offerings.ServiceOffering.serviceVendor");
        }

        @Test
        @DisplayName("Save service offering with missing service category")
        public void saveWithMissingServiceCategory() {
            var testServiceOffering = new ServiceOffering(UUID.randomUUID());
            testServiceOffering.setName(UUID.randomUUID().toString());
            testServiceOffering.setServiceVendor(sampleServiceVendor);

            Exception exception = assertThrows(DataIntegrityViolationException.class, () -> {
                serviceOfferingJpaRepository.save(testServiceOffering);
            });

            assertThat(exception.getMessage())
                    .contains("not-null property references a null or transient value")
                    .contains("org.eclipse.slm.service_management.model.offerings.ServiceOffering.serviceCategory");
        }

        @Test
        @DisplayName("Save service offering without id")
        public void saveServiceOfferingWithoutId() {
            var testServiceOffering = new ServiceOffering(UUID.randomUUID());
            testServiceOffering.setName(UUID.randomUUID().toString());
            testServiceOffering.setServiceVendor(sampleServiceVendor);
            testServiceOffering.setServiceCategory(sampleServiceCategory);

            serviceOfferingJpaRepository.save(testServiceOffering);

            var savedServiceOfferings = serviceOfferingJpaRepository.findAll();

            var optionalFoundOffering = savedServiceOfferings.stream()
                    .filter(serviceOffering -> serviceOffering.getName().equals(testServiceOffering.getName())).findAny();

            assertThat(optionalFoundOffering).isPresent();
        }

        @Test
        @DisplayName("Save service offering with id")
        public void saveServiceOfferingWithId() {
            var testServiceOffering = new ServiceOffering(UUID.randomUUID());
            testServiceOffering.setServiceVendor(sampleServiceVendor);
            testServiceOffering.setServiceCategory(sampleServiceCategory);

            serviceOfferingJpaRepository.save(testServiceOffering);

            var optionalServiceOffering = serviceOfferingJpaRepository.findById(testServiceOffering.getId());
            assertThat(optionalServiceOffering).isPresent();
        }
    }

    @Nested
    @TestInstance(PER_CLASS)
    public class update
    {
        @Test
        @DisplayName("Update existing service offering")
        public void updateExistingServiceOffering()
        {
            var testServiceOfferingId = UUID.randomUUID();
            var testServiceOffering = new ServiceOffering(testServiceOfferingId);
            testServiceOffering.setServiceVendor(sampleServiceVendor);
            testServiceOffering.setServiceCategory(sampleServiceCategory);
            testServiceOffering = entityManager.persist(testServiceOffering);
            var savedServiceOffering = entityManager.find(ServiceOffering.class, testServiceOfferingId);
            assertThat(savedServiceOffering).isNotNull();

            var newName = UUID.randomUUID().toString();
            testServiceOffering.setName(newName);
            serviceOfferingJpaRepository.save(testServiceOffering);

            savedServiceOffering = entityManager.find(ServiceOffering.class, testServiceOfferingId);
            assertThat(savedServiceOffering).isNotNull();
            assertThat(savedServiceOffering.getName()).isEqualTo(newName);
        }
    }

    @Nested
    @TestInstance(PER_CLASS)
    @Rollback(false)
    public class delete {
        @Test
        @DisplayName("Delete service offering by id")
        public void deleteServiceOfferingById() {
            var serviceOfferingId = UUID.randomUUID();
            var testServiceOffering = new ServiceOffering(serviceOfferingId);
            testServiceOffering.setServiceVendor(sampleServiceVendor);
            testServiceOffering.setServiceCategory(sampleServiceCategory);
            testServiceOffering = entityManager.persist(testServiceOffering);
            var savedServiceOffering = entityManager.find(ServiceOffering.class, serviceOfferingId);
            assertThat(savedServiceOffering).isNotNull();

            serviceOfferingJpaRepository.deleteById(serviceOfferingId);

            savedServiceOffering = entityManager.find(ServiceOffering.class, serviceOfferingId);
            assertThat(savedServiceOffering).isNull();
        }
    }
}
