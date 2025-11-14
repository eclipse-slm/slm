package org.eclipse.slm.common.aas.clients.shellregistry;

import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEndpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProtocolInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelDescriptor;
import org.eclipse.slm.common.aas.model.shellregistry.exceptions.ShellDescriptorNotFoundException;
import org.eclipse.slm.common.aas.model.shellregistry.exceptions.SubmodellDescriptorNotFoundException;
import org.eclipse.slm.common.aas.model.shellregistry.requests.GetAllShellDescriptorsFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
public class AasRegistryApiClientTests {

    @Container
    static GenericContainer<?> aasRegistry = new GenericContainer<>("eclipsebasyx/aas-registry-log-mem:2.0.0-milestone-07")
            .withExposedPorts(8080)
            .withEnv("BASYX_CORS_ALLOWEDORIGINS", "*")
            .withEnv("BASYX_CORS_ALLOWEDMETHODS", "GET,POST,PATCH,DELETE,PUT,OPTIONS,HEAD")
            .withEnv("MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE", "health,info")
            .withEnv("MANAGEMENT_INFO_GIT_ENABLED", "false")
            .waitingFor(Wait.forHttp("/actuator/health").forStatusCode(200));

    private AasRegistryClient aasRegistryClient;

    private Endpoint testSubmodelEndpoint = new DefaultEndpoint.Builder()
            ._interface("SUBMODEL-3.0")
            .protocolInformation(new DefaultProtocolInformation.Builder()
                    .endpointProtocol("http")
                    .href("localhost/testsubmodel")
                    .build()
            )
            .build();

    @BeforeEach
    public void setUp() {
        var registryUrl = "http://" + aasRegistry.getHost() + ":" + aasRegistry.getMappedPort(8080);
        this.aasRegistryClient = new AasRegistryClient(registryUrl, null);
    }

    @Nested
    @DisplayName("CRUD Shell Descriptor")
    public class ShellDescriptorCrud {
        @Test
        @DisplayName("Should return Shell Descriptor when exists")
        public void shouldReturnAssetAdministrationShellDescriptorWhenExists() {
            var aasId = UUID.randomUUID().toString();
            var aasDescriptor = new DefaultAssetAdministrationShellDescriptor.Builder().id(aasId).build();

            aasRegistryClient.createOrUpdateShellDescriptor(aasDescriptor);

            var optionalAasDescriptor = aasRegistryClient.getAasDescriptor(aasId);

            assertThat(optionalAasDescriptor).isPresent();
        }

        @Test
        @DisplayName("Should return empty optional when descriptor doesn't exists")
        public void shouldReturnEmptyOptionalWhenDescriptorsDoesntExists() {
            var aasId = UUID.randomUUID().toString();

            var optionalAasDescriptor = aasRegistryClient.getAasDescriptor(aasId);

            assertThat(optionalAasDescriptor).isEmpty();
        }

        @Test
        @DisplayName("Should throw exception when shell descriptor doesn't exists")
        public void shouldThrowExceptionWhenServerIsNotReachable() {
            var aasId = UUID.randomUUID().toString();

            assertThatThrownBy(() -> {
                aasRegistryClient.getAasDescriptorOrThrow(aasId);
            }).isInstanceOf(ShellDescriptorNotFoundException.class);
        }

        @Test
        @DisplayName("Should create and update Shell Descriptor")
        public void shouldCreateAndUpdateShellDescriptor() {
            var aasId = UUID.randomUUID().toString();
            var aasDescriptor = new DefaultAssetAdministrationShellDescriptor.Builder().id(aasId).idShort("testShell").build();

            aasRegistryClient.createOrUpdateShellDescriptor(aasDescriptor);
            var optionalAasDescriptor = aasRegistryClient.getAasDescriptor(aasId);
            assertThat(optionalAasDescriptor).isPresent();
            assertThat(optionalAasDescriptor.get().getIdShort()).isEqualTo("testShell");

            var updatedDescriptor = new DefaultAssetAdministrationShellDescriptor.Builder().id(aasId).idShort("updatedShell").build();
            aasRegistryClient.createOrUpdateShellDescriptor(updatedDescriptor);
            var updatedOptional = aasRegistryClient.getAasDescriptor(aasId);

            assertThat(updatedOptional).isPresent();
            assertThat(updatedOptional.get().getIdShort()).isEqualTo("updatedShell");
        }

        @Test
        @DisplayName("Should delete Shell Descriptor")
        public void shouldDeleteShellDescriptor() {
            var aasId = UUID.randomUUID().toString();
            var aasDescriptor = new DefaultAssetAdministrationShellDescriptor.Builder().id(aasId).build();

            aasRegistryClient.createOrUpdateShellDescriptor(aasDescriptor);
            aasRegistryClient.deleteShellDescriptor(aasId);
            var optionalAasDescriptor = aasRegistryClient.getAasDescriptor(aasId);

            assertThat(optionalAasDescriptor).isEmpty();
        }

        @Test
        @DisplayName("Should throw when deleting non-existing Shell Descriptor")
        public void shouldThrowWhenDeletingNonExistingShellDescriptor() {
            var aasId = UUID.randomUUID().toString();

            assertThatThrownBy(() -> aasRegistryClient.deleteShellDescriptor(aasId)).isInstanceOf(ShellDescriptorNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("GET all Shell Descriptors")
    public class GetAllShellDescriptors {
        @Test
        @DisplayName("Should return list of Shell Descriptors")
        public void shouldReturnListOfShellDescriptors() {
            var list = aasRegistryClient.getAllShellDescriptors(GetAllShellDescriptorsFilter.builder().build());
            assertThat(list).isNotNull();
        }
    }

    @Nested
    @DisplayName("Submodel Descriptor CRUD")
    public class SubmodelDescriptorCrud {
        @Test
        @DisplayName("Should create and delete Submodel Descriptor")
        public void shouldCreateAndDeleteSubmodelDescriptor() {
            var aasId = UUID.randomUUID().toString();
            var aasDescriptor = new DefaultAssetAdministrationShellDescriptor.Builder().id(aasId).build();
            aasRegistryClient.createOrUpdateShellDescriptor(aasDescriptor);

            var submodelId = UUID.randomUUID().toString();
            var submodelDescriptor = new DefaultSubmodelDescriptor.Builder()
                    .id(submodelId)
                    .idShort("submodel1")
                    .endpoints(testSubmodelEndpoint)
                    .build();
            aasRegistryClient.createOrUpdateSubmodelDescriptor(aasId, submodelDescriptor);

            var submodelsResult = aasRegistryClient.getAllSubmodelDescriptors(aasId);
            assertThat(submodelsResult).isNotNull();
            assertThat(submodelsResult.getResult()).anyMatch(s -> submodelId.equals(s.getId()));

            aasRegistryClient.deleteSubmodelDescriptor(aasId, submodelId);
            var submodelsAfterDeleteResult = aasRegistryClient.getAllSubmodelDescriptors(aasId);
            assertThat(submodelsAfterDeleteResult.getResult().stream().noneMatch(s -> submodelId.equals(s.getId()))).isTrue();
        }

        @Test
        @DisplayName("Should throw when deleting non-existing Submodel Descriptor")
        public void shouldThrowWhenDeletingNonExistingSubmodelDescriptor() {
            var aasId = UUID.randomUUID().toString();
            var submodelId = UUID.randomUUID().toString();
            assertThatThrownBy(() -> aasRegistryClient.deleteSubmodelDescriptor(aasId, submodelId)).isInstanceOf(ShellDescriptorNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("GET all Submodel Descriptors")
    public class GetAllSubmodelDescriptors {
        @Test
        @DisplayName("Should return list of Submodel Descriptors")
        public void shouldReturnListOfSubmodelDescriptors() {
            var aasId = UUID.randomUUID().toString();
            var aasDescriptor = new DefaultAssetAdministrationShellDescriptor.Builder().id(aasId).build();
            aasRegistryClient.createOrUpdateShellDescriptor(aasDescriptor);
            var submodels = aasRegistryClient.getAllSubmodelDescriptors(aasId);
            assertThat(submodels).isNotNull();
        }
    }

    @Nested
    @DisplayName("GET Submodel Descriptor by Id")
    public class GetSubmodelDescriptorById {
        @Test
        @DisplayName("Should return Submodel Descriptor when exists")
        public void shouldReturnSubmodelDescriptorWhenExists() {
            var aasId = UUID.randomUUID().toString();
            var aasDescriptor = new DefaultAssetAdministrationShellDescriptor.Builder().id(aasId).build();
            aasRegistryClient.createOrUpdateShellDescriptor(aasDescriptor);

            var submodelId = UUID.randomUUID().toString();
            var submodelDescriptor = new DefaultSubmodelDescriptor.Builder()
                    .id(submodelId)
                    .idShort("submodel1")
                    .endpoints(testSubmodelEndpoint)
                    .build();
            aasRegistryClient.createOrUpdateSubmodelDescriptor(aasId, submodelDescriptor);

            var optionalSubmodel = aasRegistryClient.getSubmodelDescriptor(aasId, submodelId);
            assertThat(optionalSubmodel).isPresent();
            assertThat(optionalSubmodel.get().getIdShort()).isEqualTo("submodel1");
        }

        @Test
        @DisplayName("Should return empty optional when Submodel Descriptor doesn't exist")
        public void shouldReturnEmptyOptionalWhenSubmodelDescriptorDoesntExist() {
            var aasId = UUID.randomUUID().toString();
            var submodelId = UUID.randomUUID().toString();
            var optionalSubmodel = aasRegistryClient.getSubmodelDescriptor(aasId, submodelId);
            assertThat(optionalSubmodel).isEmpty();
        }

        @Test
        @DisplayName("Should throw exception when Submodel descriptor doesn't exists")
        public void shouldThrowExceptionWhenServerIsNotReachable() {
            var aasId = UUID.randomUUID().toString();
            var submodelId = UUID.randomUUID().toString();

            assertThatThrownBy(() -> {
                aasRegistryClient.getSubmodelDescriptorOrThrow(aasId, submodelId);
            }).isInstanceOf(SubmodellDescriptorNotFoundException.class);
        }
    }

}
