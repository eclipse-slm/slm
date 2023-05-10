package org.eclipse.slm.service_management.persistence.mariadb.test;

import org.eclipse.slm.service_management.model.vendors.ServiceVendor;
import org.eclipse.slm.service_management.persistence.api.ServiceVendorJpaRepository;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ServiceVendorIT {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ServiceVendorJpaRepository serviceVendorJpaRepository;

    @DisplayName("Find all service vendors")
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void findAllServiceVendors() {
        var serviceVendorId1 = UUID.randomUUID();
        var serviceVendorId2 = UUID.randomUUID();
        var testServiceVendor1 = new ServiceVendor(serviceVendorId1);
        var testServiceVendor2 = new ServiceVendor(serviceVendorId2);
        entityManager.persist(testServiceVendor1);
        entityManager.persist(testServiceVendor2);

        var serviceVendors = serviceVendorJpaRepository.findAll();

        assertThat(serviceVendors).hasSize(2)
                .usingRecursiveFieldByFieldElementComparator()
                .contains(testServiceVendor1)
                .contains(testServiceVendor2);
    }

    @DisplayName("Find service vendor by id")
    @Test
    public void findServiceVendorById() {
        var serviceVendorId = UUID.randomUUID();
        var testServiceVendor = new ServiceVendor(serviceVendorId);
        entityManager.persist(testServiceVendor);

        var serviceVendorOptional = serviceVendorJpaRepository.findById(serviceVendorId);

        assertThat(serviceVendorOptional).isPresent();
    }

    @DisplayName("Save service vendor")
    @Test
    public void saveServiceVendor() {
        var serviceVendorId = UUID.randomUUID();
        var testServiceVendor = new ServiceVendor(serviceVendorId);

        serviceVendorJpaRepository.save(testServiceVendor);

        var savedServiceVendor = entityManager.find(ServiceVendor.class, serviceVendorId);
        assertThat(savedServiceVendor).isNotNull();
    }

    @DisplayName("Update service vendor")
    @Test
    public void updateServiceVendor() {
        var serviceVendorId = UUID.randomUUID();
        var serviceVendorName = UUID.randomUUID().toString();
        var testServiceVendor = new ServiceVendor(serviceVendorId);
        testServiceVendor.setName(serviceVendorName);
        entityManager.persist(testServiceVendor);
        var savedServiceVendor = entityManager.find(ServiceVendor.class, serviceVendorId);
        assertThat(savedServiceVendor).isNotNull();
        assertThat(savedServiceVendor.getName()).isEqualTo(serviceVendorName);

        var newName = UUID.randomUUID().toString();
        testServiceVendor.setName(newName);
        testServiceVendor = serviceVendorJpaRepository.save(testServiceVendor);

        savedServiceVendor = entityManager.find(ServiceVendor.class, serviceVendorId);
        assertThat(savedServiceVendor).isNotNull();
        assertThat(savedServiceVendor.getName()).isEqualTo(newName);
    }

    @DisplayName("Delete service vendor by id")
    @Test
    public void deleteServiceVendorById() {
        var serviceVendorId = UUID.randomUUID();
        var testServiceVendor = new ServiceVendor(serviceVendorId);
        entityManager.persist(testServiceVendor);
        var savedServiceVendor = entityManager.find(ServiceVendor.class, serviceVendorId);
        assertThat(savedServiceVendor).isNotNull();

        serviceVendorJpaRepository.deleteById(serviceVendorId);

        savedServiceVendor = entityManager.find(ServiceVendor.class, serviceVendorId);
        assertThat(savedServiceVendor).isNull();
    }
}
