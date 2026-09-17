package org.eclipse.slm.resource_management.common.aas.submodels;

import org.eclipse.slm.aas.clients.shellregistry.AasRegistryClientFactory;
import org.eclipse.slm.aas.clients.shellrepository.AasRepositoryClientFactory;
import org.eclipse.slm.aas.clients.submodelregistry.SubmodelRegistryClientFactory;
import org.eclipse.slm.aas.clients.submodelrepository.SubmodelRepositoryClientFactory;
import org.eclipse.slm.aas.repositories.submodels.SubmodelRepository;
import org.eclipse.slm.resource_management.common.aas.submodels.deviceinfo.DeviceInfoSubmodelServiceFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;

/**
 * Covers the fail-fast guard against ResourceSubmodelContributor key collisions, added per a
 * code-review recommendation from Task 7 now that a real second contributor
 * (DeploymentSubmodelRepositoryFactory) exists.
 */
@ExtendWith(MockitoExtension.class)
class ResourcesSubmodelRepositoryTest {

    @Mock private AasRegistryClientFactory aasRegistryClientFactory;
    @Mock private AasRepositoryClientFactory aasRepositoryClientFactory;
    @Mock private SubmodelRegistryClientFactory submodelRegistryClientFactory;
    @Mock private SubmodelRepositoryClientFactory submodelRepositoryClientFactory;
    @Mock private DeviceInfoSubmodelServiceFactory deviceInfoSubmodelServiceFactory;

    private static class StubContributor implements ResourceSubmodelContributor {
        private final String key;

        StubContributor(String key) {
            this.key = key;
        }

        @Override
        public String getContributorKey() {
            return key;
        }

        @Override
        public SubmodelRepository getSubmodelRepository(String aasId) {
            return null;
        }
    }

    private ResourcesSubmodelRepository repository(List<ResourceSubmodelContributor> contributors) {
        lenient().when(aasRegistryClientFactory.getClient()).thenReturn(null);
        lenient().when(aasRepositoryClientFactory.getClient()).thenReturn(null);
        lenient().when(submodelRegistryClientFactory.getClient()).thenReturn(null);
        lenient().when(submodelRepositoryClientFactory.getClient()).thenReturn(null);

        return new ResourcesSubmodelRepository("Resource_test", aasRegistryClientFactory,
                aasRepositoryClientFactory, submodelRegistryClientFactory, submodelRepositoryClientFactory,
                deviceInfoSubmodelServiceFactory, contributors);
    }

    @Test
    @DisplayName("Two contributors with distinct keys are both registered")
    void distinctKeysAreBothRegistered() {
        var repository = repository(List.of(new StubContributor("A"), new StubContributor("B")));

        assertThat(repository.getSubmodelRepositoryFactories()).containsKeys("A", "B");
    }

    @Test
    @DisplayName("No contributors leaves the repository factory map untouched")
    void noContributorsIsFine() {
        var repository = repository(List.of());

        assertThat(repository.getSubmodelRepositoryFactories()).doesNotContainKey("Deployment");
    }

    @Test
    @DisplayName("Two contributors claiming the same key fail fast with a clear message")
    void duplicateKeyFailsFast() {
        var first = new StubContributor("Deployment");
        var second = new StubContributor("Deployment");

        assertThatThrownBy(() -> repository(List.of(first, second)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Deployment")
                .hasMessageContaining(first.getClass().getName())
                .hasMessageContaining(second.getClass().getName());
    }
}
