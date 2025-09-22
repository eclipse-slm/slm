package org.eclipse.slm.resource_management.features.capabilities;

import org.eclipse.slm.common.model.DeploymentType;
import org.eclipse.slm.resource_management.features.capabilities.model.Capability;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityType;
import org.eclipse.slm.resource_management.features.capabilities.model.DeploymentCapability;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.Action;
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionType;
import org.eclipse.slm.resource_management.features.capabilities.model.awx.AwxAction;
import org.eclipse.slm.resource_management.features.capabilities.persistence.CapabilityJpaRepository;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@EnableAutoConfiguration
@ContextConfiguration(classes = {
        CapabilityJpaRepository.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class CapabilityJpaDeploymentCapabilityTest {
    public final static Logger LOG = LoggerFactory.getLogger(CapabilityJpaDeploymentCapabilityTest.class);

    @Autowired
    private CapabilityJpaRepository capabilityJpaRepository;

    private static DeploymentCapability dockerDeploymentCapability;

    private static List<DeploymentCapability> deploymentCapabilityList = new ArrayList<>();

    @Order(10)
    @DisplayName("Pretest")
    void injectedComponentsAreNotNull(){
            assertNotNull(capabilityJpaRepository);
        }

    @BeforeAll
    static void beforeAll() {
        int dcCount = 5;

        ArrayList<DeploymentCapability> list = new ArrayList<>(dcCount);
        DeploymentCapability dc = null;

        for(int i = 0; i < dcCount; i++) {
            //region Create Java Object of Docker DeploymentCapability:
            dc = new DeploymentCapability();
            dc.setName("Docker");
            dc.setLogo("mdi-docker");
            dc.setType(Arrays.asList(
                    CapabilityType.SETUP,
                    CapabilityType.DEPLOY
            ));
            dc.setCapabilityClass("DeploymentCapability");

            dc.setSupportedDeploymentTypes(Arrays.asList(
                    DeploymentType.DOCKER_CONTAINER,
                    DeploymentType.DOCKER_COMPOSE
            ));

            // Set AWX Capability Actions

            HashMap<ActionType, Action> capabilityActions = new HashMap();
            capabilityActions.put(
                    ActionType.INSTALL,
                    new AwxAction("awxRepo", "awxBranch", "playbook")
            );

            dc.setActions(capabilityActions);
            //endregion

            list.add(dc);
        }

        deploymentCapabilityList = list;
        dockerDeploymentCapability = list.get(0);
    }

    @Test
    @Order(20)
    void findAllIfNoCapabilityPersisted() {
        List<Capability> capabilities = capabilityJpaRepository.findAll();

        assertEquals(0, capabilities.size());
    }

    @Test
    @Order(30)
    public void persistOneDeploymentCapability() {
        List<Capability> capabilitiesBefore = capabilityJpaRepository.findAll();

        DeploymentCapability persistedSingleHostDeploymentCapability = capabilityJpaRepository.save(dockerDeploymentCapability);

        assertEquals(capabilitiesBefore.size()+1, capabilityJpaRepository.findAll().size());
    }

    @Test
    @Order(40)
    public void getDeploymentCapabilityById() {
        Optional<Capability> optionalFoundDeploymentCapability = capabilityJpaRepository.findById(
                dockerDeploymentCapability.getId()
        );

        assertEquals(
                true,
                optionalFoundDeploymentCapability.isPresent()
        );

        Capability foundDeploymentCapability = optionalFoundDeploymentCapability.get();

        assertEquals(
                dockerDeploymentCapability.getId(),
                foundDeploymentCapability.getId()
        );
    }

    @Test
    @Order(50)
    public void removeOneDeploymentCapability() {
        List<Capability> capabilitiesBefore = capabilityJpaRepository.findAll();

        capabilityJpaRepository.delete(dockerDeploymentCapability);

        assertEquals(capabilitiesBefore.size()-1, capabilityJpaRepository.findAll().size());
    }

    @Test
    @Order(60)
    public void persistMultipleDeploymentCapabilities() {
        List<Capability> capabilitiesBefore = capabilityJpaRepository.findAll();

        capabilityJpaRepository.saveAll(deploymentCapabilityList);

        assertEquals(
                capabilitiesBefore.size()+deploymentCapabilityList.size(),
                capabilityJpaRepository.findAll().size()
        );
    }

    @Test
    @Order(70)
    public void removeAllDeploymentCapabilities() {
        assertNotEquals(
                0,
                capabilityJpaRepository.findAll().size()
        );

        capabilityJpaRepository.deleteAll();

        assertEquals(
                0,
                capabilityJpaRepository.findAll().size()
        );
    }

    @Test
    @Order(80)
    void testUsernamePasswordGetNotPersisted() {
        //Add Username/Password to all actions of DC:
        dockerDeploymentCapability.getActions().forEach((k,v) -> {
            AwxAction awxCapabilityAction = (AwxAction) v;
            awxCapabilityAction.setUsername("username");
            awxCapabilityAction.setPassword("password");
        });

        capabilityJpaRepository.save(dockerDeploymentCapability);

        //Get all capabilities from DB
        List<Capability> persistedCapabilities = capabilityJpaRepository.findAll();

        //Check persisted DC has correct properties:
        Capability persistedCapability = persistedCapabilities.stream().filter(c -> c.getName().equals(dockerDeploymentCapability.getName())).findFirst().get();

        //Assert all actions have empty username/password:
        persistedCapability.getActions().forEach((k,v) -> {
            AwxAction awxCapabilityAction = (AwxAction) v;
            assertEquals("", awxCapabilityAction.getUsername());
            assertEquals("", awxCapabilityAction.getPassword());
        });
    }

    @Test
    @Order(90)
    void deleteAllCapabilities() {
        assertNotEquals(
                0,
                capabilityJpaRepository.findAll().size()
        );

        capabilityJpaRepository.deleteAll();

        assertEquals(
                0,
                capabilityJpaRepository.findAll().size()
        );
    }
}
