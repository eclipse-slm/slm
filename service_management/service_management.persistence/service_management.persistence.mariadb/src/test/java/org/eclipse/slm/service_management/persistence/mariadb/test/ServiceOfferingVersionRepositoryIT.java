package org.eclipse.slm.service_management.persistence.mariadb.test;

import org.eclipse.slm.service_management.model.offerings.ServiceCategory;
import org.eclipse.slm.service_management.model.offerings.ServiceOffering;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersion;
import org.eclipse.slm.service_management.model.offerings.docker.compose.DockerComposeDeploymentDefinition;
import org.eclipse.slm.service_management.model.offerings.docker.container.DockerContainerDeploymentDefinition;
import org.eclipse.slm.service_management.model.offerings.docker.container.DockerRestartPolicy;
import org.eclipse.slm.service_management.model.vendors.ServiceVendor;
import org.eclipse.slm.service_management.persistence.api.ServiceOfferingJpaRepository;
import org.eclipse.slm.service_management.persistence.api.ServiceOfferingVersionJpaRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@DataJpaTest
@ContextConfiguration(classes = { SpringTestConfiguration.class })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class ServiceOfferingVersionRepositoryIT {

    @Autowired
    protected ServiceOfferingVersionJpaRepository serviceOfferingVersionJpaRepository;

    @Autowired
    protected ServiceOfferingJpaRepository serviceOfferingJpaRepository;

    @Autowired
    private TestEntityManager entityManager;

    protected ServiceCategory sampleServiceCategory = new ServiceCategory(UUID.randomUUID().toString());

    protected ServiceVendor sampleServiceVendor = new ServiceVendor(UUID.randomUUID());

    protected ServiceOffering sampleServiceOffering1 = new ServiceOffering(UUID.randomUUID());

    protected ServiceOffering sampleServiceOffering2 = new ServiceOffering(UUID.randomUUID());

    @BeforeEach
    public void init() {
        sampleServiceCategory = entityManager.merge(sampleServiceCategory);
        sampleServiceVendor = entityManager.merge(sampleServiceVendor);

        sampleServiceOffering1.setServiceCategory(sampleServiceCategory);
        sampleServiceOffering1.setServiceVendor(sampleServiceVendor);
        sampleServiceOffering1 = entityManager.merge(sampleServiceOffering1);

        sampleServiceOffering2.setServiceCategory(sampleServiceCategory);
        sampleServiceOffering2.setServiceVendor(sampleServiceVendor);
        sampleServiceOffering2 = entityManager.merge(sampleServiceOffering2);
    }

    @Nested
    @TestInstance(PER_CLASS)
    public class find {
        @DisplayName("Find all service offering versions")
        @Test
        public void findAllServiceOfferingVersions() {
            var deploymentDefinition = new DockerContainerDeploymentDefinition();
            var testServiceOfferingVersion1 = new ServiceOfferingVersion(
                    UUID.randomUUID(), sampleServiceOffering1, "1.0.0", deploymentDefinition);
            var testServiceOfferingVersion2 = new ServiceOfferingVersion(
                    UUID.randomUUID(), sampleServiceOffering2, "1.0.1", deploymentDefinition);
            testServiceOfferingVersion1 = entityManager.persist(testServiceOfferingVersion1);
            testServiceOfferingVersion2 = entityManager.persist(testServiceOfferingVersion2);

            var serviceOfferingVersions = serviceOfferingVersionJpaRepository.findAll();

            assertThat(serviceOfferingVersions).hasSize(2)
                    .usingRecursiveFieldByFieldElementComparator()
                    .contains(testServiceOfferingVersion1)
                    .contains(testServiceOfferingVersion2);
        }

        @DisplayName("Find service offering version by id")
        @Test
        public void findServiceOfferingVersionById() {
            var serviceOfferingVersionId = UUID.randomUUID();
            var deploymentDefinition = new DockerContainerDeploymentDefinition();
            var testServiceOfferingVersion = new ServiceOfferingVersion(
                    serviceOfferingVersionId, sampleServiceOffering1, "1.0.0", deploymentDefinition);
            testServiceOfferingVersion = entityManager.persist(testServiceOfferingVersion);

            var serviceOfferingVersionOptional = serviceOfferingVersionJpaRepository.findById(serviceOfferingVersionId);
            assertThat(serviceOfferingVersionOptional).isPresent();
        }

        @DisplayName("Find service offering versions for service offering")
        @Test
        public void findServiceOfferingVersionsForServiceOffering() {
            var deploymentDefinition = new DockerContainerDeploymentDefinition();
            var testServiceOfferingVersion11 = new ServiceOfferingVersion(
                    UUID.randomUUID(), sampleServiceOffering1, "1.0.0", deploymentDefinition);
            var testServiceOfferingVersion12 = new ServiceOfferingVersion(
                    UUID.randomUUID(), sampleServiceOffering1, "1.0.1", deploymentDefinition);
            var testServiceOfferingVersion2 = new ServiceOfferingVersion(
                    UUID.randomUUID(), sampleServiceOffering2, "2.0.0", deploymentDefinition);
            testServiceOfferingVersion11 = entityManager.persist(testServiceOfferingVersion11);
            testServiceOfferingVersion12 = entityManager.persist(testServiceOfferingVersion12);
            testServiceOfferingVersion2 = entityManager.persist(testServiceOfferingVersion2);

            var serviceOfferingVersions = serviceOfferingVersionJpaRepository
                    .findByServiceOfferingId(sampleServiceOffering1.getId());

            assertThat(serviceOfferingVersions).hasSize(2)
                    .usingRecursiveFieldByFieldElementComparator()
                    .contains(testServiceOfferingVersion11)
                    .contains(testServiceOfferingVersion12);
        }
    }

    @Nested
    @TestInstance(PER_CLASS)
    public class save {

        @Test
        @DisplayName("Save service offering version with missing deployment definition and missing service offering")
        public void saveWithMissingDeploymentDefinitionAndServiceOffering() {
            var testServiceOfferingVersion = new ServiceOfferingVersion(
                    UUID.randomUUID(), null, "1.0.0", null);

            Exception exception = assertThrows(DataIntegrityViolationException.class, () -> {
                serviceOfferingVersionJpaRepository.save(testServiceOfferingVersion);
            });

            assertThat(exception.getMessage())
                    .contains("not-null property references a null or transient value");
        }

        @Test
        @DisplayName("Save service offering version with missing deployment definition")
        public void saveWithMissingDeploymentDefinition() {
            var testServiceOfferingVersion = new ServiceOfferingVersion(
                    UUID.randomUUID(), sampleServiceOffering1, "1.0.0", null);

            Exception exception = assertThrows(DataIntegrityViolationException.class, () -> {
                serviceOfferingVersionJpaRepository.save(testServiceOfferingVersion);
            });

            assertThat(exception.getMessage())
                    .contains("not-null property references a null or transient value")
                    .contains("org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersion.deploymentDefinition");
        }

        @Test
        @DisplayName("Save service offering version with missing service offering")
        public void saveWithMissingServiceOffering() {
            var deploymentDefinition = new DockerContainerDeploymentDefinition();
            var testServiceOfferingVersion = new ServiceOfferingVersion(
                    UUID.randomUUID(), null, "1.0.0", deploymentDefinition);

            Exception exception = assertThrows(DataIntegrityViolationException.class, () -> {
                serviceOfferingVersionJpaRepository.save(testServiceOfferingVersion);
            });

            assertThat(exception.getMessage())
                    .contains("not-null property references a null or transient value")
                    .contains("org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersion.serviceOffering");
        }

        @Test
        @DisplayName("Save service offering without id")
        public void saveServiceOfferingWithoutId() {
            var deploymentDefinition = new DockerContainerDeploymentDefinition();
            var version = UUID.randomUUID().toString();
            var testServiceOfferingVersion = new ServiceOfferingVersion(
                    null, sampleServiceOffering1, version, deploymentDefinition);

            serviceOfferingVersionJpaRepository.save(testServiceOfferingVersion);

            var savedServiceOfferings = serviceOfferingVersionJpaRepository.findAll();

            var optionalFoundOffering = savedServiceOfferings.stream()
                    .filter(serviceOffering -> testServiceOfferingVersion.getVersion().equals(version)).findAny();

            assertThat(optionalFoundOffering).isPresent();
        }

        @Test
        @DisplayName("Save service offering with id")
        public void saveServiceOfferingWithId() {
            var deploymentDefinition = new DockerContainerDeploymentDefinition();
            var serviceOfferingVersionId = UUID.randomUUID();
            var testServiceOfferingVersion = new ServiceOfferingVersion(
                    serviceOfferingVersionId, sampleServiceOffering1, "1.0.0", deploymentDefinition);

            testServiceOfferingVersion = serviceOfferingVersionJpaRepository.save(testServiceOfferingVersion);

            var serviceOfferingVersion = entityManager.find(ServiceOfferingVersion.class, serviceOfferingVersionId);
            assertThat(serviceOfferingVersion).isNotNull();
        }
    }

    @Nested
    @TestInstance(PER_CLASS)
    public class update
    {
        @Test
        @DisplayName("Update existing service offering version")
        public void updateExistingServiceOfferingVersion()
        {
            var deploymentDefinition = new DockerContainerDeploymentDefinition();
            var version = UUID.randomUUID().toString();
            var serviceOfferingVersionId = UUID.randomUUID();
            var testServiceOfferingVersion = new ServiceOfferingVersion(
                    serviceOfferingVersionId, sampleServiceOffering1, version, deploymentDefinition);
            testServiceOfferingVersion = entityManager.persist(testServiceOfferingVersion);
            var savedServiceOfferingVersion = entityManager.find(ServiceOfferingVersion.class, serviceOfferingVersionId);
            assertThat(savedServiceOfferingVersion).isNotNull();

            var newVersion = UUID.randomUUID().toString();
            testServiceOfferingVersion.setVersion(newVersion);
            serviceOfferingVersionJpaRepository.save(testServiceOfferingVersion);

            savedServiceOfferingVersion = entityManager.find(ServiceOfferingVersion.class, serviceOfferingVersionId);
            assertThat(savedServiceOfferingVersion).isNotNull();
            assertThat(savedServiceOfferingVersion.getVersion()).isEqualTo(newVersion);
        }
    }

    @Nested
    @TestInstance(PER_CLASS)
    public class deploymentDefinitions {
        @Test
        @DisplayName("Read deployment definition - Docker Container")
        public void readDeploymentDefinitionDockerContainer() {
            var deploymentDefinition = new DockerContainerDeploymentDefinition();
            deploymentDefinition.setImageRepository("myImage");
            deploymentDefinition.setImageTag("v1");
            deploymentDefinition.setRestartPolicy(DockerRestartPolicy.ON_FAILURE);
            var serviceOfferingVersionId = UUID.randomUUID();
            var testServiceOfferingVersion = new ServiceOfferingVersion(
                    serviceOfferingVersionId, sampleServiceOffering1, "1.0.0", deploymentDefinition);
            testServiceOfferingVersion = entityManager.persist(testServiceOfferingVersion);

            var optionalServiceOfferingVersion = serviceOfferingVersionJpaRepository.findById(serviceOfferingVersionId);

            assertThat(optionalServiceOfferingVersion).isPresent();
            assertThat(optionalServiceOfferingVersion.get().getDeploymentDefinition())
                    .usingRecursiveComparison().isEqualTo(deploymentDefinition);
        }

        @Test
        @DisplayName("Read deployment definition - Docker Compose")
        public void readDeploymentDefinitionDockerCompose() {
            var deploymentDefinition = new DockerComposeDeploymentDefinition();
            deploymentDefinition.setComposeFile("asdasdasd");
            deploymentDefinition.setDotEnvFile("sdfgdsfgdsgdsg");
            deploymentDefinition.setEnvFiles(Map.of("env.list", "asdasd=asdasd"));
            var serviceOfferingVersionId = UUID.randomUUID();
            var testServiceOfferingVersion = new ServiceOfferingVersion(
                    serviceOfferingVersionId, sampleServiceOffering1, "1.0.0", deploymentDefinition);
            testServiceOfferingVersion = entityManager.persist(testServiceOfferingVersion);

            var optionalServiceOfferingVersion = serviceOfferingVersionJpaRepository.findById(serviceOfferingVersionId);

            assertThat(optionalServiceOfferingVersion).isPresent();
            assertThat(optionalServiceOfferingVersion.get().getDeploymentDefinition())
                    .usingRecursiveComparison().isEqualTo(deploymentDefinition);
        }
    }
}
