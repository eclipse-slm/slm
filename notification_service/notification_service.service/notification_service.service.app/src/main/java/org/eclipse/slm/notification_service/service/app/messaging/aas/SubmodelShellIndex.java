package org.eclipse.slm.notification_service.service.app.messaging.aas;

import org.eclipse.slm.aas.clients.shellrepository.AasRepositoryClient;
import org.eclipse.slm.resource_management.common.aas.ResourceAas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maps submodel IDs to the ID of the shell referencing them, because BaSyx submodel events do not contain the shell.
 */
public class SubmodelShellIndex {

    private final static Logger LOG = LoggerFactory.getLogger(SubmodelShellIndex.class);

    private final Map<String, String> submodelIdToShellId = new ConcurrentHashMap<>();

    public Optional<String> getShellId(String submodelId) {
        return Optional.ofNullable(this.submodelIdToShellId.get(submodelId));
    }

    public void put(String submodelId, String shellId) {
        this.submodelIdToShellId.put(submodelId, shellId);
    }

    public void remove(String submodelId) {
        this.submodelIdToShellId.remove(submodelId);
    }

    public void removeShell(String shellId) {
        this.submodelIdToShellId.values().removeIf(shellId::equals);
    }

    public void initialize(Collection<UUID> resourceIds, AasRepositoryClient aasRepositoryClient) {
        for (var resourceId : resourceIds) {
            var aasId = ResourceAas.createAasIdFromResourceId(resourceId);
            try {
                var shellOptional = aasRepositoryClient.getAas(aasId);
                if (shellOptional.isPresent() && shellOptional.get().getSubmodels() != null) {
                    for (var submodelReference : shellOptional.get().getSubmodels()) {
                        this.put(submodelReference.getKeys().get(0).getValue(), aasId);
                    }
                }
            } catch (Exception e) {
                LOG.warn("Unable to index submodels of AAS '{}': {}", aasId, e.getMessage());
            }
        }
    }
}
