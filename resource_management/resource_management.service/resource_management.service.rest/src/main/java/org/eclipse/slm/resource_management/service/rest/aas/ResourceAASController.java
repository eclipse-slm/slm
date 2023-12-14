package org.eclipse.slm.resource_management.service.rest.aas;

import org.eclipse.slm.common.consul.client.ConsulCredential;
import org.eclipse.slm.common.consul.model.exceptions.ConsulLoginFailedException;
import org.eclipse.slm.resource_management.service.rest.resources.ResourceEvent;
import org.eclipse.slm.resource_management.service.rest.resources.ResourcesConsulClient;
import org.eclipse.slm.resource_management.model.resource.BasicResource;
import org.eclipse.slm.resource_management.model.resource.exceptions.ResourceNotFoundException;
import lombok.SneakyThrows;
import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.aas.metamodel.map.descriptor.SubmodelDescriptor;
import org.eclipse.basyx.aas.registration.api.IAASRegistry;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.vab.exception.provider.ProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.ResourceAccessException;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
public class ResourceAASController implements ApplicationListener<ResourceEvent> {

    private final static Logger LOG = LoggerFactory.getLogger(ResourceAASController.class);
    public static final String ID_SHORT_PLATFORM_RESOURCES = "PlatformResources";
    private final String aasServerUrl;
    private final String aasRegistryUrl;
    private final String monitoringServiceUrl;
    private final ResourcesConsulClient resourcesConsulClient;
    private IAASRegistry registry;
    private ConnectedAssetAdministrationShellManager aasManager = null;

    ResourceAASController(
            @Value("${basyx.aas-registry.url}") String aasRegistryUrl,
            @Value("${basyx.aas-server.url}") String aasServerUrl,
            @Value("${monitoring.service.url}") String monitoringServiceUrl,
            @Autowired ResourcesConsulClient resourcesConsulClient) {
        this.aasServerUrl = aasServerUrl;
        this.aasRegistryUrl = aasRegistryUrl;
        this.monitoringServiceUrl = monitoringServiceUrl;
        this.resourcesConsulClient = resourcesConsulClient;
    }

    @PostConstruct
    public void init() {
        this.registry = new AASRegistryProxy(this.aasRegistryUrl);
        try {
            registry.lookupAll();
            this.aasManager = new ConnectedAssetAdministrationShellManager(registry);
            createAASandSubmodelsFromDB();
        } catch (ProviderException e) {
            LOG.warn("Could not connect to AAS registry. Will not create or register any resource AAS: "+e);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

    private void createAASandSubmodelsFromDB() {
        try {
            List<BasicResource> resources = resourcesConsulClient.getResources(new ConsulCredential());

            for (BasicResource resource: resources) {
                createAASandSubmodel(resource);
                registerMetricsSubmodel(resource);
            }
        } catch (ResourceAccessException e) {
            LOG.warn("Could not access or find resources. " + e);
        } catch (ConsulLoginFailedException e) {
            throw new RuntimeException(e);
        }
    }

    private void createAASandSubmodel(BasicResource resource) {
        IIdentifier aasIdentifier = ResourceAAS.createIdentification(resource.getId());
        try {
            aasManager.retrieveAAS(aasIdentifier);
        } catch (org.eclipse.basyx.vab.exception.provider.ResourceNotFoundException e) {
            AssetAdministrationShell aas = new ResourceAAS(resource);
            aasManager.createAAS(aas, aasServerUrl);
        }
        IIdentifier smIdentifier = new CustomId(ConsulSubmodel.SUBMODELID + "-" + resource.getId());
        Submodel consulSubmodel = new ConsulSubmodel(smIdentifier, resource.getIp(), resource.getHostname());
        aasManager.createSubmodel(aasIdentifier, consulSubmodel);
    }

    private void registerMetricsSubmodel(BasicResource resource) {
        IIdentifier aasIdentifier = ResourceAAS.createIdentification(resource.getId());
        IIdentifier smIdentifier = new CustomId(ID_SHORT_PLATFORM_RESOURCES + "-" + resource.getId());
        String endpoint = this.monitoringServiceUrl + "/shells/" + resource.getId() + "/aas/submodels/"+ID_SHORT_PLATFORM_RESOURCES+"/submodel";
        SubmodelDescriptor smDescriptor = new SubmodelDescriptor(ID_SHORT_PLATFORM_RESOURCES, smIdentifier, endpoint);
        registry.register(aasIdentifier, smDescriptor);
    }

    @SneakyThrows
    @Override
    public void onApplicationEvent(ResourceEvent event) {
        if (aasManager == null) {
            return;
        }

        UUID uuid = event.getId();
        if (event.isCreate()) {

            try {
                Optional<BasicResource> optionalResource = resourcesConsulClient.getResourceById(
                        new ConsulCredential(),
                        uuid
                );
                createAASandSubmodel(optionalResource.get());
                registerMetricsSubmodel(optionalResource.get());
            } catch(NullPointerException e) {
                LOG.warn("Unable to find resource with id = '"+uuid+"'.");
            } catch (ResourceNotFoundException | ConsulLoginFailedException e) {
                LOG.error(e.getMessage());
            }
        } else if (event.isDelete()) {
            try {
                IIdentifier aasId = ResourceAAS.createIdentification(uuid);
                aasManager.deleteAAS(aasId);
            }
            catch (Exception e) {
                LOG.error(e.getMessage());
            }
        }
    }

}

