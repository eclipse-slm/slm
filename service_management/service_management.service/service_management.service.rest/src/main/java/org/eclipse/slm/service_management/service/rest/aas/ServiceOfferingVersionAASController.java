package org.eclipse.slm.service_management.service.rest.aas;

import org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersion;
import org.eclipse.slm.service_management.persistence.api.ServiceOfferingVersionJpaRepository;
import org.eclipse.slm.service_management.service.rest.service_offerings.ServiceOfferingVersionEvent;
import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.registration.api.IAASRegistry;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.identifier.Identifier;
import org.eclipse.basyx.vab.exception.provider.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
public class ServiceOfferingVersionAASController implements ApplicationListener<ServiceOfferingVersionEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceOfferingVersionAASController.class);

    private final String aasServerUrl;
    private final ConnectedAssetAdministrationShellManager aasManager;

    private final ServiceOfferingVersionJpaRepository serviceOfferingVersionJpaRepository;

    ServiceOfferingVersionAASController(
            @Value("${basyx.aas-registry.url}") String aasRegistryUrl,
            @Value("${basyx.aas-server.url}") String aasServerUrl,
            @Autowired ServiceOfferingVersionJpaRepository serviceOfferingVersionJpaRepository) {
        this.aasServerUrl = aasServerUrl;
        IAASRegistry registry = new AASRegistryProxy(aasRegistryUrl);
        this.aasManager = new ConnectedAssetAdministrationShellManager(registry);
        this.serviceOfferingVersionJpaRepository = serviceOfferingVersionJpaRepository;
        createAASandSubmodelsFromDB();
    }

    private void createAASandSubmodelsFromDB() {
        List<ServiceOfferingVersion> serviceOfferingVersions = serviceOfferingVersionJpaRepository.findAll();
        for (ServiceOfferingVersion serviceOfferingVersion: serviceOfferingVersions) {
            createAASandSubmodels(serviceOfferingVersion);
        }
    }

    private void createAASandSubmodels(ServiceOfferingVersion serviceOfferingVersion) {
        Identifier aasIdentifier = ServiceAASConverter.getAASIdentifierFromServiceOfferingVersionUUID(serviceOfferingVersion.getId());
        Boolean createAAS = false;
        try {
            aasManager.retrieveAAS(aasIdentifier);
        } catch (ResourceNotFoundException e) {
            createAAS = true;
        }

        if(createAAS) {
            AssetAdministrationShell aas = ServiceAASConverter.getAASFromServiceOfferingVersion(serviceOfferingVersion);
            try {
                aasManager.createAAS(aas, aasServerUrl);
            } catch (ResourceNotFoundException e) {
                LOG.error("Failed to create AAS for resource. Message: " + e.getMessage());
                return;
            }
        }

        Submodel softwareNameplateSM = ServiceAASConverter.getSoftwareNameplateSMForServiceOfferingVersion(serviceOfferingVersion);
        Submodel requirementsSM = RequirementsConverter.createRequirementsSubmodel(serviceOfferingVersion);

        try {
            aasManager.createSubmodel(aasIdentifier, softwareNameplateSM);
            aasManager.createSubmodel(aasIdentifier, requirementsSM);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void onApplicationEvent(ServiceOfferingVersionEvent event) {
        UUID uuid = event.getId();
        if (event.isCreate() || event.isUpdate()) {
            ServiceOfferingVersion serviceOfferingVersion = serviceOfferingVersionJpaRepository.findById(uuid).get();
            createAASandSubmodels(serviceOfferingVersion);
        } else if (event.isDelete()) {
            Identifier aasId = ServiceAASConverter.getAASIdentifierFromServiceOfferingVersionUUID(uuid);
            aasManager.deleteAAS(aasId);
        }
    }
}
