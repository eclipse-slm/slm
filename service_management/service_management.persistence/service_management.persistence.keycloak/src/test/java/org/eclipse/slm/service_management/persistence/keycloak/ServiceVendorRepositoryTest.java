package org.eclipse.slm.service_management.persistence.keycloak;

import com.c4_soft.springaddons.security.oauth2.test.annotations.*;
import org.eclipse.slm.common.keycloak.config.KeycloakAdminClient;
import org.eclipse.slm.common.keycloak.config.exceptions.KeycloakGroupNotFoundException;
import org.eclipse.slm.common.keycloak.config.exceptions.KeycloakUserNotFoundException;
import org.eclipse.slm.service_management.model.vendors.ServiceVendor;
import org.eclipse.slm.service_management.model.vendors.ServiceVendorDeveloper;
import org.eclipse.slm.service_management.model.vendors.exceptions.ServiceVendorNotFoundException;
import org.eclipse.slm.service_management.persistence.api.ServiceVendorJpaRepository;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@DataJpaTest
@ContextConfiguration(classes = {
        ServiceVendorRepository.class,
        ServiceVendorJpaRepository.class,
        KeycloakAdminClient.class
})
@EntityScan( basePackages = { "org.eclipse.slm.service_management.model" })
@EnableJpaRepositories(basePackages = "org.eclipse.slm.service_management.persistence.api")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class ServiceVendorRepositoryTest {

    @Autowired
    private ServiceVendorJpaRepository serviceVendorJpaRepository;

    @Autowired
    private ServiceVendorRepository serviceVendorRepository;

    @MockBean
    private KeycloakAdminClient keycloakAdminClient;

    private RecursiveComparisonConfiguration defaultRecursiveComparisonConfiguration = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields("id", "persisted")
            .build();

    @Nested
    public class getServiceVendors {
        @Test
        @DisplayName("No vendor defined")
        public void noVendorsDefined() {
            var serviceVendors = serviceVendorRepository.getServiceVendors();
            assertThat(serviceVendors).hasSize(0);
        }

        @Test
        @DisplayName("One vendor defined")
        public void oneVendorDefined() {
            var serviceVendor = addTestServiceVendorInDatabase();

            var serviceVendors = serviceVendorRepository.getServiceVendors();
            assertThat(serviceVendors).hasSize(1);
            assertThat(serviceVendors).usingRecursiveFieldByFieldElementComparator().contains(serviceVendor);
        }
    }

    @Nested
    public class getServiceVendorById {
        @Test
        @DisplayName("Vendor doesn't exist")
        public void vendorNotExists() {
            var serviceVendorOptional = serviceVendorRepository.getServiceVendorById(UUID.randomUUID());
            assertThat(serviceVendorOptional).isNotPresent();
        }

        @Test
        @DisplayName("Vendor exists")
        public void vendorExists() {
            var serviceVendor = addTestServiceVendorInDatabase();

            var serviceVendorOptional = serviceVendorRepository.getServiceVendorById(serviceVendor.getId());
            assertThat(serviceVendorOptional).isPresent();
            assertThat(serviceVendorOptional.get()).usingRecursiveComparison().isEqualTo(serviceVendor);
        }
    }

    @Nested
    public class getServiceVendorLogo {
        @Test
        @DisplayName("No logo defined")
        public void vendorNotExists() {
            assertThatThrownBy(() -> {
                serviceVendorRepository.getServiceVendorLogo(UUID.randomUUID());
            }).isInstanceOf(ServiceVendorNotFoundException.class);
        }

        @Test
        @DisplayName("Logo defined")
        public void vendorExists() throws ServiceVendorNotFoundException {
            var serviceVendor = addTestServiceVendorWithLogoInDatabase();

            var serviceVendorLogo = serviceVendorRepository.getServiceVendorLogo(serviceVendor.getId());
            assertThat(serviceVendorLogo).isEqualTo(serviceVendor.getLogo());
        }
    }

    @Nested
    public class createOrUpdateServiceVendorWithId {
        @Test
        @DisplayName("Create vendor without logo")
        public void createVendorWithoutLogo() {
            when(keycloakAdminClient.createGroup(any(), any(), any())).thenReturn(new GroupRepresentation());

            var createdServiceVendor = generateTestServiceVendor();
            serviceVendorRepository.createOrUpdateServiceVendorWithId(createdServiceVendor, "fabos");

            verify(keycloakAdminClient).createGroup(
                    argThat(realm -> realm.equals("fabos")),
                    argThat(groupName -> groupName.equals(createdServiceVendor.getKeycloakGroupName())),
                    any());
            var serviceVendorInDatabase = serviceVendorJpaRepository.findById(createdServiceVendor.getId());
            assertThat(serviceVendorInDatabase.get())
                    .usingRecursiveComparison(defaultRecursiveComparisonConfiguration).isEqualTo(createdServiceVendor);
        }

        @Test
        @DisplayName("Create vendor with logo")
        public void createVendorWithLogo() {
            when(keycloakAdminClient.createGroup(any(), any(), any())).thenReturn(new GroupRepresentation());

            var createdServiceVendor = generateTestServiceVendorWithLogo();
            serviceVendorRepository.createOrUpdateServiceVendorWithId(createdServiceVendor, "fabos");

            verify(keycloakAdminClient).createGroup(
                    argThat(realm -> realm.equals("fabos")),
                    argThat(groupName -> groupName.equals(createdServiceVendor.getKeycloakGroupName())),
                    any());
            var serviceVendorInDatabase = serviceVendorJpaRepository.findById(createdServiceVendor.getId());
            assertThat(serviceVendorInDatabase.get())
                    .usingRecursiveComparison(defaultRecursiveComparisonConfiguration).isEqualTo(createdServiceVendor);
        }

        @Test
        @DisplayName("Update vendor")
        public void updateVendor() {
            var storedServiceVendor = addTestServiceVendorWithLogoInDatabase();
            var updatedServiceVendor = new ServiceVendor(storedServiceVendor.getId());
            updatedServiceVendor.setName("Updated Service Vendor");
            updatedServiceVendor.setDescription("Updated Service Vendor Description");
            SecureRandom random = new SecureRandom();
            byte[] randomLogo = new byte[20];
            random.nextBytes(randomLogo);
            updatedServiceVendor.setLogo(randomLogo);

            serviceVendorRepository.createOrUpdateServiceVendorWithId(updatedServiceVendor, "fabos");

            var updatedStoredServiceVendorOptional = serviceVendorJpaRepository.findById(storedServiceVendor.getId());
            assertThat(updatedStoredServiceVendorOptional).isPresent();
            assertThat(updatedStoredServiceVendorOptional.get())
                    .usingRecursiveComparison(defaultRecursiveComparisonConfiguration).isEqualTo(updatedServiceVendor);
        }
    }

    @Nested
    public class deleteServiceVendorById {
        @Test
        @DisplayName("Vendor doesn't exist")
        public void vendorNotExists() {
            assertThatThrownBy(() -> {
                serviceVendorRepository.deleteServiceVendorById(UUID.randomUUID(), "fabos");
            }).isInstanceOf(ServiceVendorNotFoundException.class);
        }

        @Test
        @DisplayName("Vendor exists")
        public void vendorExists() throws ServiceVendorNotFoundException, KeycloakGroupNotFoundException {
            var serviceVendorId = UUID.randomUUID();
            var storedServiceVendor = addTestServiceVendorInDatabase(serviceVendorId);

            serviceVendorRepository.deleteServiceVendorById(serviceVendorId, "fabos");

            verify(keycloakAdminClient).deleteGroup(
                    argThat(realm -> realm.equals("fabos")),
                    argThat(groupName -> groupName.equals(storedServiceVendor.getKeycloakGroupName())));
        }
    }

    @Nested
    public class getDevelopersOfServiceVendor {
        @Test
        @DisplayName("Vendor doesn't exist")
        public void vendorNotExists() {
            assertThatThrownBy(() -> {
                serviceVendorRepository.getDevelopersOfServiceVendor(UUID.randomUUID(), "fabos");
            }).isInstanceOf(ServiceVendorNotFoundException.class);
        }

        @Test
        @DisplayName("Vendor exists, but no developers defined")
        public void vendorExistsNoDevelopersDefined()
                throws KeycloakGroupNotFoundException, ServiceVendorNotFoundException {
            var serviceVendor = addTestServiceVendorInDatabase();
            var developers = serviceVendorRepository.getDevelopersOfServiceVendor(serviceVendor.getId(), "fabos");
            assertThat(developers).hasSize(0);
        }

        @Test
        @DisplayName("Vendor exists, one developer defined")
        public void vendorExistsOneDeveloperDefined()
                throws KeycloakUserNotFoundException, KeycloakGroupNotFoundException, ServiceVendorNotFoundException {
            var serviceVendor = addTestServiceVendorInDatabase();
            var serviceVendorDeveloper = generateTestServiceVendorDeveloper();
            var userId = UUID.randomUUID();
            serviceVendorRepository.addDeveloperToServiceVendor(serviceVendor.getId(), userId, "fabos");

            when(keycloakAdminClient.getUsersOfGroup("fabos", serviceVendor.getKeycloakGroupName()))
                    .thenReturn(List.of(getKeycloakUserRepresentationForServiceVendorDeveloper(serviceVendorDeveloper)));

            var developers = serviceVendorRepository.getDevelopersOfServiceVendor(serviceVendor.getId(), "fabos");

            assertThat(developers).hasSize(1);
            assertThat(developers.get(0).getId()).isEqualTo(serviceVendorDeveloper.getId());
        }
    }

    @Nested
    public class addDeveloperToServiceVendor {
        @Test
        @DisplayName("Vendor doesn't exist")
        public void vendorNotExists() {
            assertThatThrownBy(() -> {
                serviceVendorRepository.addDeveloperToServiceVendor(UUID.randomUUID(), UUID.randomUUID(), "fabos");
            }).isInstanceOf(ServiceVendorNotFoundException.class);
        }

        @Test
        @DisplayName("Vendor exists")
        public void vendorExists()
                throws KeycloakUserNotFoundException, KeycloakGroupNotFoundException, ServiceVendorNotFoundException {
            var serviceVendor = addTestServiceVendorInDatabase();
            var serviceVendorDeveloper = generateTestServiceVendorDeveloper();

            serviceVendorRepository.addDeveloperToServiceVendor(serviceVendor.getId(), serviceVendorDeveloper.getId(), "fabos");

            verify(keycloakAdminClient).assignUserToGroup(
                    argThat(realm -> realm.equals("fabos")),
                    argThat(groupName -> groupName.equals(serviceVendor.getKeycloakGroupName())),
                    argThat(userId -> userId.equals(serviceVendorDeveloper.getId())));
        }
    }

    @Nested
    public class removeDeveloperFromServiceVendor {
        @Test
        @DisplayName("Vendor doesn't exist")
        public void vendorNotExists() {
            assertThatThrownBy(() -> {
                serviceVendorRepository.removeDeveloperFromServiceVendor(UUID.randomUUID(), UUID.randomUUID(), "fabos");
            }).isInstanceOf(ServiceVendorNotFoundException.class);
        }

        @Test
        @DisplayName("Vendor exists, but developer doesn't exist")
        public void vendorExistsButDeveloperNot()
                throws KeycloakUserNotFoundException, KeycloakGroupNotFoundException {
            var serviceVendor = addTestServiceVendorInDatabase();
            var developerUserId = UUID.randomUUID();

            doThrow(new KeycloakUserNotFoundException(developerUserId))
                    .when(keycloakAdminClient).removeUserFromGroup("fabos", serviceVendor.getKeycloakGroupName(), developerUserId);

            assertThatThrownBy(() -> {
                serviceVendorRepository.removeDeveloperFromServiceVendor(serviceVendor.getId(), developerUserId, "fabos");
            }).isInstanceOf(KeycloakUserNotFoundException.class);
        }

        @Test
        @DisplayName("Vendor exists, developer exists")
        public void vendorAndDeveloperExist() throws KeycloakUserNotFoundException, KeycloakGroupNotFoundException, ServiceVendorNotFoundException {
            var serviceVendor = addTestServiceVendorInDatabase();
            var serviceVendorDeveloper = generateTestServiceVendorDeveloper();

            serviceVendorRepository.removeDeveloperFromServiceVendor(serviceVendor.getId(), serviceVendorDeveloper.getId(), "fabos");

            verify(keycloakAdminClient).removeUserFromGroup(
                    argThat(realm -> realm.equals("fabos")),
                    argThat(groupName -> groupName.equals(serviceVendor.getKeycloakGroupName())),
                    argThat(userId -> userId.equals(serviceVendorDeveloper.getId())));
        }
    }

    @Nested
    public class getServiceVendorsOfDeveloper {

        @Test
        @DisplayName("No vendors associated")
        @WithMockJwtAuth()
        public void noVendorsAssociated() {
            var jwtAuthenticationToken = (JwtAuthenticationToken) TestSecurityContextHolder.getContext().getAuthentication();

            var developers = serviceVendorRepository.getServiceVendorsOfDeveloper(jwtAuthenticationToken);
            assertThat(developers).hasSize(0);
        }

        @Test
        @DisplayName("One vendor associated")
        @WithMockJwtAuth(claims = @OpenIdClaims(
                otherClaims = @Claims(stringArrayClaims = @StringArrayClaim(name = "groups", value = { "vendor_c12c5a32-c57d-4afd-89f4-9bcd4ae7bee3" })
        )))
        public void oneVendorAssociated() {
            var jwtAuthenticationToken = (JwtAuthenticationToken) TestSecurityContextHolder.getContext().getAuthentication();
            var serviceVendor = generateTestServiceVendorWithId(UUID.fromString("c12c5a32-c57d-4afd-89f4-9bcd4ae7bee3"));

            var serviceVendors = serviceVendorRepository.getServiceVendorsOfDeveloper(jwtAuthenticationToken);
            assertThat(serviceVendors).hasSize(1);
            assertThat(serviceVendors).contains(serviceVendor.getId());
        }
    }

    private ServiceVendor generateTestServiceVendor(UUID serviceVendorId, byte[] logo) {
        var serviceVendor = new ServiceVendor(serviceVendorId);
        serviceVendor.setName("TestVendor");
        serviceVendor.setDescription("Test Vendor Description");
        serviceVendor.setLogo(logo);
        return serviceVendor;
    }

    private ServiceVendor generateTestServiceVendor() {
        return this.generateTestServiceVendor(UUID.randomUUID(), null);
    }

    private ServiceVendor generateTestServiceVendorWithId(UUID serviceVendorId) {
        return this.generateTestServiceVendor(serviceVendorId, null);
    }

    private ServiceVendor generateTestServiceVendorWithLogo() {
        SecureRandom random = new SecureRandom();
        byte[] randomLogo = new byte[20];
        random.nextBytes(randomLogo);
        return this.generateTestServiceVendor(UUID.randomUUID(), null);
    }

    private ServiceVendor addTestServiceVendorInDatabase(UUID serviceVendorId, byte[] logo) {
        var serviceVendor = this.generateTestServiceVendor(serviceVendorId, logo);
        serviceVendor = this.serviceVendorJpaRepository.save(serviceVendor);
        return serviceVendor;
    }

    private ServiceVendor addTestServiceVendorInDatabase(UUID serviceVendorId) {
        return this.addTestServiceVendorInDatabase(serviceVendorId, null);
    }

    private ServiceVendor addTestServiceVendorInDatabase(ServiceVendor serviceVendor) {
        serviceVendor = this.serviceVendorJpaRepository.save(serviceVendor);
        return serviceVendor;
    }

    private ServiceVendor addTestServiceVendorInDatabase() {
        return this.addTestServiceVendorInDatabase(UUID.randomUUID(), null);
    }

    private ServiceVendor addTestServiceVendorWithLogoInDatabase() {
        var serviceVendor = generateTestServiceVendorWithLogo();
        return this.addTestServiceVendorInDatabase(serviceVendor);
    }

    private ServiceVendorDeveloper generateTestServiceVendorDeveloper() {
        var serviceVendorDeveloper = new ServiceVendorDeveloper();
        serviceVendorDeveloper.setId(UUID.randomUUID());
        serviceVendorDeveloper.setFirstName("Test");
        serviceVendorDeveloper.setLastName("tesT");
        serviceVendorDeveloper.setEmail("test@test.test");
        serviceVendorDeveloper.setUsername("testUser");

        return serviceVendorDeveloper;
    }

    private UserRepresentation getKeycloakUserRepresentationForServiceVendorDeveloper (ServiceVendorDeveloper serviceVendorDeveloper) {
        var developerUserRepresentation = new UserRepresentation();
        developerUserRepresentation.setId(serviceVendorDeveloper.getId().toString());
        developerUserRepresentation.setEmail(serviceVendorDeveloper.getEmail());
        developerUserRepresentation.setFirstName(serviceVendorDeveloper.getFirstName());
        developerUserRepresentation.setLastName(serviceVendorDeveloper.getLastName());
        developerUserRepresentation.setUsername(serviceVendorDeveloper.getUsername());

        return developerUserRepresentation;
    }

}
