package org.eclipse.slm.resource_management.service.rest.aas.resources;

import org.eclipse.slm.common.aas.repositories.AbstractSubmodelRepository;
import org.eclipse.slm.resource_management.service.rest.aas.resources.consul.ConsulSubmodelServiceFactory;

public class ResourcesSubmodelRepository extends AbstractSubmodelRepository {

    public ResourcesSubmodelRepository(String aasId) {
        super(aasId);
        this.addSubmodelServiceFactory("Consul", new ConsulSubmodelServiceFactory());
    }
}
