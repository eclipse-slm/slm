package org.eclipse.slm.service_management.service.rest.aas;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.basyx.http.Base64UrlEncodedIdentifier;
import org.eclipse.digitaltwin.basyx.submodelregistry.client.ApiException;
import org.eclipse.slm.common.aas.clients.AasRegistryClient;
import org.eclipse.slm.common.aas.clients.AasRepositoryClient;
import org.eclipse.slm.common.aas.clients.SubmodelRegistryClient;
import org.eclipse.slm.common.aas.clients.SubmodelRepositoryClient;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersion;
import org.eclipse.slm.service_management.persistence.api.ServiceOfferingVersionJpaRepository;
import org.eclipse.slm.service_management.service.rest.aas.serviceofferingversions.ServiceOfferingVersionAas;
import org.eclipse.slm.service_management.service.rest.aas.serviceofferingversions.ServiceOfferingVersionsSubmodelRepositoryApiHTTPController;
import org.eclipse.slm.service_management.service.rest.aas.serviceofferingversions.softwarenameplate.SoftwareNameplateSubmodel;
import org.eclipse.slm.service_management.service.rest.service_offerings.ServiceOfferingVersionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Component
public class AasHandler implements ApplicationListener<ServiceOfferingVersionEvent> {

    private final Logger LOG = LoggerFactory.getLogger(AasHandler.class);

    private final AasRegistryClient aasRegistryClient;

    private final AasRepositoryClient aasRepositoryClient;

    private final SubmodelRegistryClient submodelRegistryClient;

    private final SubmodelRepositoryClient submodelRepositoryClient;

    private final ServiceOfferingVersionJpaRepository serviceOfferingVersionRepository;

    private final String externalScheme;
    private final String externalHostname;
    private final String externalPort;


    public AasHandler(AasRegistryClient aasRegistryClient, AasRepositoryClient aasRepositoryClient,
                      SubmodelRegistryClient submodelRegistryClient, SubmodelRepositoryClient submodelRepositoryClient,
                      ServiceOfferingVersionJpaRepository serviceOfferingVersionRepository,
                      @Value("${deployment.scheme}") String externalScheme,
                      @Value("${deployment.hostname}") String externalHostname,
                      @Value("${deployment.port}") String externalPort) {
        this.aasRegistryClient = aasRegistryClient;
        this.aasRepositoryClient = aasRepositoryClient;
        this.submodelRegistryClient = submodelRegistryClient;
        this.submodelRepositoryClient = submodelRepositoryClient;
        this.serviceOfferingVersionRepository = serviceOfferingVersionRepository;
        this.externalScheme = externalScheme;
        this.externalHostname = externalHostname;
        this.externalPort = externalPort;
    }

    @PostConstruct
    public void init() {
        // Create AAS for all service offering versions
        try {
            var serviceOfferingVersions = this.serviceOfferingVersionRepository.findAll();

            for (var serviceOfferingVersion : serviceOfferingVersions) {
                this.createServiceOfferingAasAndSubmodels(serviceOfferingVersion);
            }
        } catch (RuntimeException e) {
            LOG.error(e.getMessage());
        }
    }

    private String getServiceOfferingVersionsSubmodelRepositoryUrl(Base64UrlEncodedIdentifier aasId) {
        var basePath = ServiceOfferingVersionsSubmodelRepositoryApiHTTPController.class.getAnnotation(RequestMapping.class).value()[0];
        basePath = basePath.replace("{aasId}", aasId.getEncodedIdentifier());
        var url = this.externalScheme + "://" + this.externalHostname + ":" + this.externalPort + basePath;

        return url;
    }

    private void createServiceOfferingAasAndSubmodels(ServiceOfferingVersion serviceOfferingVersion) {
        try {
            var serviceOfferingVersionAas = new ServiceOfferingVersionAas(serviceOfferingVersion);
            var serviceOfferingVersionAasSIdEncoded = new Base64UrlEncodedIdentifier(serviceOfferingVersionAas.getId());
            this.aasRepositoryClient.createOrUpdateAas(serviceOfferingVersionAas);

            var softwareNameplateSubmodelId = SoftwareNameplateSubmodel.getSubmodelIdForServiceOfferingVersionId(serviceOfferingVersion.getId());
            var softwareNameplateSubmodelIdEncoded = new Base64UrlEncodedIdentifier(softwareNameplateSubmodelId);
            var softwareNameplateSubmodelUrl = this.getServiceOfferingVersionsSubmodelRepositoryUrl(serviceOfferingVersionAasSIdEncoded)
                    + "/submodels/" + softwareNameplateSubmodelIdEncoded.getEncodedIdentifier();
            this.aasRepositoryClient.addSubmodelReferenceToAas(serviceOfferingVersionAas.getId(), softwareNameplateSubmodelId);
            this.submodelRegistryClient.registerSubmodel(
                    softwareNameplateSubmodelUrl,
                    softwareNameplateSubmodelId,
                    softwareNameplateSubmodelId,
                    SoftwareNameplateSubmodel.SEMANTIC_ID_VALUE);
        }
        catch (ApiException e) {
            LOG.error("Unable to create AAS and submodels for ServiceOfferingVersion [id='" + serviceOfferingVersion.getId() + "']: " + e.getMessage());
        }
    }

    private void deleteServiceOfferingVersionAasAndSubmodels(UUID serviceOfferingVersion) {
        try {
            var serviceOfferingVersionAasId = ServiceOfferingVersionAas.createAasIdFromServiceOfferingVersionId(serviceOfferingVersion);
            var serviceOfferingVersionAas = this.aasRepositoryClient.getAas(serviceOfferingVersionAasId);

            for (var submodelRef : serviceOfferingVersionAas.getSubmodels()) {
                if (submodelRef.getKeys().get(0).getType().equals(KeyTypes.SUBMODEL)) {
                    var submodelId = submodelRef.getKeys().get(0).getValue();

                    var submodelDescriptorOptional = this.submodelRegistryClient.findSubmodelDescriptor(submodelId);
                    if (submodelDescriptorOptional.isPresent()) {
                        var endpoint = submodelDescriptorOptional.get().getEndpoints().get(0).getProtocolInformation().getHref();

                        if (endpoint.startsWith(this.submodelRepositoryClient.getSubmodelRepositoryUrl())) {
                            this.submodelRepositoryClient.deleteSubmodel(submodelId);
                        }
                    }
                    this.submodelRegistryClient.unregisterSubmodel(submodelId);
                }
            }
            this.aasRepositoryClient.deleteAAS(serviceOfferingVersionAasId);

        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onApplicationEvent(ServiceOfferingVersionEvent serviceOfferingVersionEvent) {
        switch (serviceOfferingVersionEvent.getOperation()) {
            case CREATE -> {
                var optionalServiceOfferingVersion = this.serviceOfferingVersionRepository
                        .findById(serviceOfferingVersionEvent.getServiceOfferingVersionId());
                optionalServiceOfferingVersion.ifPresent(this::createServiceOfferingAasAndSubmodels);
            }
            case DELETE -> {
                this.deleteServiceOfferingVersionAasAndSubmodels(serviceOfferingVersionEvent.getServiceOfferingVersionId());
            }
        }
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}
