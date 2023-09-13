package org.eclipse.slm.service_management.service.rest.aas;

import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.metamodel.api.IAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.aas.metamodel.map.descriptor.SubmodelDescriptor;
import org.eclipse.basyx.aas.metamodel.map.parts.Asset;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SubmodelGarbageCollectionTestDev {
    public final static Logger LOG = LoggerFactory.getLogger(SubmodelGarbageCollectionTestDev.class);
    public final static GenericContainer<?> aasRegistryContainer;
    public final static GenericContainer<?> aasServerContainer;
    private static int AAS_REGISTRY_PORT = 4000;
    private static int AAS_REGISTRY_PUBLIC_PORT = 4000;
    private static int AAS_SERVER_PORT = 4001;
    private static int AAS_SERVER_PUBLIC_PORT = 4001;
    private static ConnectedAssetAdministrationShellManager aasManager;
    private static AASRegistryProxy aasRegistryProxy;
    private static AssetAdministrationShell testAas = new AssetAdministrationShell(
            "aas-id-short",
            new CustomId("aas-id"),
            new Asset()
    );
    private static Submodel testSubmodel = new Submodel(
            "submodel-id-short",
            new CustomId("submodel-id")
    );

    static {
        aasRegistryContainer = new GenericContainer<>(DockerImageName.parse("eclipsebasyx/aas-registry:1.4.0"))
                .withExposedPorts(AAS_REGISTRY_PORT);
        aasRegistryContainer.start();

        aasServerContainer = new GenericContainer<>(DockerImageName.parse("eclipsebasyx/aas-server:1.4.0"))
                .withExposedPorts(AAS_SERVER_PORT);
        aasServerContainer.start();
    }

    @BeforeAll
    public static void beforeAll() {
        AAS_REGISTRY_PUBLIC_PORT = aasRegistryContainer.getFirstMappedPort();
        AAS_SERVER_PUBLIC_PORT = aasServerContainer.getFirstMappedPort();
        aasRegistryProxy = new AASRegistryProxy("http://localhost:"+AAS_REGISTRY_PUBLIC_PORT+"/registry");
        aasManager = new ConnectedAssetAdministrationShellManager(aasRegistryProxy);
    }

    private static String getAASServerUrl() {
        return "http://localhost:"+AAS_SERVER_PUBLIC_PORT+"/aasServer";
    }

    @Test
    @Order(10)
    public void checkAASCountEqualsZero() {
        Collection<IAssetAdministrationShell> aasCollection = aasManager.retrieveAASAll();

        assertEquals(0, aasCollection.size());
    }

    @Test
    @Order(20)
    public void createAAS() {

        aasManager.createAAS(testAas, getAASServerUrl());

        Collection<IAssetAdministrationShell> aasCollection = aasManager.retrieveAASAll();
        assertEquals(1, aasCollection.size());
    }

    @Test
    @Order(30)
    public void registerSubmodel() {
        aasManager.createSubmodel(testAas.getIdentification(), testSubmodel);
        Map<String, ISubmodel> submodels = aasManager.retrieveSubmodels(testAas.getIdentification());
    }

    @Test
    @Order(40)
    public void deleteSubmodelDescriptor() {
        aasServerContainer.stop();

        Map<String, ISubmodel> submodels = aasManager.retrieveSubmodels(testAas.getIdentification());

        List<SubmodelDescriptor> submodelDescriptors = aasRegistryProxy.lookupSubmodels(testAas.getIdentification());

        SubmodelDescriptor submodelDescriptor = submodelDescriptors
                .stream()
                .filter(sd -> sd.getIdShort().equals(testSubmodel.getIdShort()))
                .findFirst()
                .get();

        aasRegistryProxy.delete(testAas.getIdentification(), submodelDescriptor.getIdentifier());

        List<SubmodelDescriptor> submodelDescriptorsAfter = aasRegistryProxy.lookupSubmodels(testAas.getIdentification());

        assertEquals(submodelDescriptors.size()-1, submodelDescriptorsAfter.size());
    }
}
