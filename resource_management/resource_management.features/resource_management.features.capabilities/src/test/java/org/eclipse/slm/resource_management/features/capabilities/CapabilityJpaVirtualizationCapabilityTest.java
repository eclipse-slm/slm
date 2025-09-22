package org.eclipse.slm.resource_management.features.capabilities;

import org.eclipse.slm.common.awx.model.SurveyItem;
import org.eclipse.slm.common.awx.model.SurveyItemType;
import org.eclipse.slm.resource_management.features.capabilities.model.Capability;
import org.eclipse.slm.resource_management.features.capabilities.model.CapabilityType;
import org.eclipse.slm.resource_management.features.capabilities.model.VirtualizationCapability;
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
public class CapabilityJpaVirtualizationCapabilityTest {
    public final static Logger LOG = LoggerFactory.getLogger(CapabilityJpaVirtualizationCapabilityTest.class);

    private static VirtualizationCapability virtualizationCapability;

    private static List<VirtualizationCapability> virtualizationCapabilityList = new ArrayList<>();

    private static List<SurveyItem> awxCapabilityParams;

    @Autowired
    private CapabilityJpaRepository capabilityJpaRepository;

    @Order(10)
    @DisplayName("Pretest")
    void injectedComponentsAreNotNull(){
            assertNotNull(capabilityJpaRepository);
        }

    @BeforeAll
    static void beforeAll() {
        int vcCount = 5;

        ArrayList<VirtualizationCapability> list = new ArrayList<>(vcCount);
        VirtualizationCapability vc = null;

        for(int i = 0; i < vcCount; i++) {
            //region Create Java Object of Docker DeploymentCapability:
            vc = new VirtualizationCapability();
            vc.setName("KVM/QEMU");
            vc.setLogo("mdi-kvm");
            vc.setType(Arrays.asList(
                    CapabilityType.SETUP,
                    CapabilityType.DEPLOY,
                    CapabilityType.VM
            ));
            vc.setCapabilityClass("VirtualizationCapability");

            // Set AWX Capability Actions
            HashMap<ActionType, Action> capabilityActions = new HashMap();
            capabilityActions.put(
                    ActionType.INSTALL,
                    new AwxAction("awxRepo", "awxBranch", "install.yml")
            );
            capabilityActions.put(
                    ActionType.UNINSTALL,
                    new AwxAction("awxRepo", "awxBranch", "uninstall.yml")
            );
            capabilityActions.put(
                    ActionType.CREATE_VM,
                    new AwxAction("awxRepo", "awxBranch", "create_vm.yml")
            );
            capabilityActions.put(
                    ActionType.DELETE_VM,
                    new AwxAction("awxRepo", "awxBranch", "delete_vm.yml")
            );

            vc.setActions(capabilityActions);
            //endregion

            list.add(vc);
        }

        virtualizationCapabilityList = list;
        virtualizationCapability = list.get(0);

        //region Survey Items
        SurveyItem surveyItemText = new SurveyItem(
                SurveyItemType.text,
                "Question 1",
                "Question 1 Description",
                "question_1",
                new ArrayList<String>(),
                null,
                null,
                false,
                "default-text"
        );

        SurveyItem surveyItemMultiplechoice = new SurveyItem(
                SurveyItemType.multiplechoice,
                "Question 2",
                "Question 2 Description",
                "question_2",
                Arrays.asList("Choice1", "Choice2", "Choice3"),
                null,
                null,
                true,
                ""
        );

        SurveyItem surveyItemInteger = new SurveyItem(
                SurveyItemType.integer,
                "Question 3",
                "Question 3 Description",
                "question_3",
                new ArrayList<String>(),
                3,
                45,
                true,
                ""
        );

        SurveyItem surveyItemPassword = new SurveyItem(
                SurveyItemType.password,
                "Question 4",
                "Question 4 Description",
                "question_4",
                new ArrayList<String>(),
                null,
                null,
                true,
                ""
        );

        SurveyItem surveyItemMultiselect = new SurveyItem(
                SurveyItemType.multiselect,
                "Question 5",
                "Question 5 Description",
                "question_5",
                Arrays.asList("Choice4", "Choice5", "Choice6"),
                null,
                null,
                false,
                ""
        );
        awxCapabilityParams = Arrays.asList(
                surveyItemText,
                surveyItemPassword,
                surveyItemInteger,
                surveyItemMultiplechoice,
                surveyItemMultiselect
        );
        //endregion
    }

    @Test
    @Order(20)
    void findAllIfNoCapabilityPersisted() {
        List<Capability> capabilities = capabilityJpaRepository.findAll();

        assertEquals(0, capabilities.size());
    }

    @Test
    @Order(30)
    void persistOneVirtualizationCapability() {
        List<Capability> capabilitiesBefore = capabilityJpaRepository.findAll();

        VirtualizationCapability persistedVirtualizationCapability = capabilityJpaRepository.save(virtualizationCapability);

        assertEquals(capabilitiesBefore.size()+1, capabilityJpaRepository.findAll().size());
    }

    @Test
    @Order(40)
    void getVirtualizationCapabilityById() {
        Optional<Capability> optionalFoundVirtualizationCapability = capabilityJpaRepository.findById(
                virtualizationCapability.getId()
        );

        assertTrue(optionalFoundVirtualizationCapability.isPresent());

        Capability foundVirtualizationCapability = optionalFoundVirtualizationCapability.get();

        assertEquals(
                virtualizationCapability.getId(),
                foundVirtualizationCapability.getId()
        );

        assertEquals(
                VirtualizationCapability.class,
                foundVirtualizationCapability.getClass()
        );
    }

    @Test
    @Order(50)
    void removeOneVirtualizationCapability() {
        List<Capability> capabilitiesBefore = capabilityJpaRepository.findAll();

        capabilityJpaRepository.delete(virtualizationCapability);

        assertEquals(capabilitiesBefore.size()-1, capabilityJpaRepository.findAll().size());
    }

    @Test
    @Order(60)
    void persistMultipleVirtualizationCapabilities() {
        List<Capability> capabilitiesBefore = capabilityJpaRepository.findAll();

        capabilityJpaRepository.saveAll(virtualizationCapabilityList);

        assertEquals(
                capabilitiesBefore.size()+virtualizationCapabilityList.size(),
                capabilityJpaRepository.findAll().size()
        );
    }

    @Test
    @Order(70)
    void removeAllVirtualizationCapabilities() {
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
        virtualizationCapability.getActions().forEach((k,v) -> {
            AwxAction awxCapabilityAction = (AwxAction) v;
            awxCapabilityAction.setUsername("username");
            awxCapabilityAction.setPassword("password");
        });

        capabilityJpaRepository.save(virtualizationCapability);

        //Get all capabilities from DB
        List<Capability> persistedCapabilities = capabilityJpaRepository.findAll();

        //Check persisted DC has correct properties:
        Capability persistedCapability = persistedCapabilities.stream().filter(c -> c.getName().equals(virtualizationCapability.getName())).findFirst().get();

        //Assert all actions have empty username/password:
        persistedCapability.getActions().forEach((k,v) -> {
            AwxAction awxCapabilityAction = (AwxAction) v;
            assertEquals("", awxCapabilityAction.getUsername());
            assertEquals("", awxCapabilityAction.getPassword());
        });

        capabilityJpaRepository.deleteAll();
    }

    @Test
    @Order(90)
    void persistCapabilityWithAwxActionContainingParameter() {
        //Add params to one Action:
        AwxAction action = (AwxAction) virtualizationCapability.getActions().get(ActionType.CREATE_VM);
        action.setParameter(awxCapabilityParams);
        List<SurveyItem> paramsToPersist = action.getParameter();

        List<Capability> capabilitiesBefore = capabilityJpaRepository.findAll();

        VirtualizationCapability persistedVirtualizationCapability = capabilityJpaRepository.save(virtualizationCapability);

        assertEquals(capabilitiesBefore.size()+1, capabilityJpaRepository.findAll().size());

        AwxAction persistedAction = (AwxAction) persistedVirtualizationCapability.getActions().get(ActionType.CREATE_VM);
        List<SurveyItem> paramsPersisted = persistedAction.getParameter();

        assertIterableEquals(paramsToPersist, paramsPersisted);
    }

    @Test
    @Order(100)
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
