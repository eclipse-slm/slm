package org.eclipse.slm.service_management.service.rest.service_vendors;

import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenIdClaims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockJwtAuth;
import com.fasterxml.jackson.core.type.TypeReference;
import org.eclipse.slm.service_management.service.rest.AbstractRestControllerIT;
import org.eclipse.slm.service_management.model.vendors.ServiceVendor;
import org.eclipse.slm.service_management.model.vendors.exceptions.ServiceVendorNotFoundException;
import org.eclipse.slm.service_management.persistence.keycloak.ServiceVendorRepository;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServiceVendorsRestController.class)
@ContextConfiguration(
        classes = {
                ServiceVendorsRestController.class
        }
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockJwtAuth(
    claims = @OpenIdClaims(
            iss = "https://localhost/auth/realms/fabos",
            preferredUsername = "testUser123"
    )
)
@DisplayName("REST API | Service Vendors (/services/vendors)")
public class ServiceVendorsRestControllerTest extends AbstractRestControllerIT {

    private final static String BASE_PATH = "/services/vendors";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServiceVendorRepository serviceVendorRepository;

    @Nested
    @DisplayName("Get service vendors (GET " + BASE_PATH + ")")
    public class getCategories {

        @Test
        @DisplayName("Get all service vendors when no vendor is defined")
        public void getVendorsNoVendorDefined() throws Exception {
            var path = BASE_PATH;

            var responseBody = mockMvc.perform(get(path).with(csrf()))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            var serviceVendors = objectMapper.readValue(responseBody, new TypeReference<List<ServiceVendor>>() {});

            assertThat(serviceVendors).hasSize(0);
        }

        @Test
        @DisplayName("Get all service vendors when one vendor is defined")
        public void getVendorsOneVendorDefined() throws Exception {
            var path = BASE_PATH;

            var serviceVendor = new ServiceVendor(UUID.randomUUID());
            serviceVendor.setName("Test Service Vendor");
            serviceVendor.setDescription("Test Service Vendor Description");
            Mockito.when(serviceVendorRepository.getServiceVendors()).thenReturn(List.of(serviceVendor));

            var responseBody = mockMvc.perform(get(path).with(csrf()))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            var serviceVendors = objectMapper.readValue(responseBody, new TypeReference<List<ServiceVendor>>() {});

            assertThat(serviceVendors).hasSize(1)
                    .usingRecursiveFieldByFieldElementComparator().contains(serviceVendor);
        }

        @Test
        @DisplayName("Get service vendor by id")
        public void getVendorById() throws Exception {
            var serviceVendor = new ServiceVendor(UUID.randomUUID());
            serviceVendor.setName("Test Service Vendor");
            serviceVendor.setDescription("Test Service Vendor Description");
            var path = BASE_PATH + "/" + serviceVendor.getId();

            Mockito.when(serviceVendorRepository.getServiceVendorById(any())).thenReturn(Optional.of(serviceVendor));

            var responseBody = mockMvc.perform(get(path).with(csrf()))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            var receivedServiceVendor = objectMapper.readValue(responseBody, ServiceVendor.class);

            assertThat(receivedServiceVendor).usingRecursiveComparison().isEqualTo(serviceVendor);
        }

        @Test
        @DisplayName("Get non existing category by id")
        public void getNonExistingVendorById() throws Exception {
            var serviceVendorId = UUID.randomUUID();
            var path = BASE_PATH + "/" + serviceVendorId;

            var responseBody = mockMvc.perform(get(path).with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> assertTrue(result.getResolvedException() instanceof ServiceVendorNotFoundException));
        }
    }

    @Test
    @DisplayName("Create vendor (POST " + BASE_PATH + ")")
    public void createVendorUsingPost() throws Exception {
        var path = BASE_PATH;
        var serviceVendor = new ServiceVendor(UUID.randomUUID());
        serviceVendor.setName("Test Service Vendor");
        serviceVendor.setDescription("Test Service Vendor Description");
        Mockito.when(serviceVendorRepository.createOrUpdateServiceVendorWithId(any(), any())).thenReturn(serviceVendor);

        var responseBody = mockMvc.perform(post(path)
                        .content(asJsonString(serviceVendor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ArgumentCaptor<ServiceVendor> serviceVendorArg = ArgumentCaptor.forClass(ServiceVendor.class);
        ArgumentCaptor<String> keycloakRealmArg = ArgumentCaptor.forClass(String.class);
        Mockito.verify(serviceVendorRepository).createOrUpdateServiceVendorWithId(serviceVendorArg.capture(), keycloakRealmArg.capture());

        RecursiveComparisonConfiguration comparisonConfiguration = RecursiveComparisonConfiguration.builder()
                .withIgnoredFields("id", "persisted")
                .build();
        assertThat(serviceVendorArg.getValue()).usingRecursiveComparison(comparisonConfiguration).isEqualTo(serviceVendor);
        assertThat(keycloakRealmArg.getValue()).isEqualTo("fabos");
    }

    @Test
    @DisplayName("Create or update vendor (PUT " + BASE_PATH + "/{serviceVendorId})")
    public void createOrUpdateVendorUsingPut() throws Exception {
        var serviceVendor = new ServiceVendor(UUID.randomUUID());
        serviceVendor.setName("Test Service Vendor");
        serviceVendor.setDescription("Test Service Vendor Description");
        var path = BASE_PATH + "/" + serviceVendor.getId();

        var responseBody = mockMvc.perform(put(path)
                        .content(asJsonString(serviceVendor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ArgumentCaptor<ServiceVendor> serviceVendorArg = ArgumentCaptor.forClass(ServiceVendor.class);
        ArgumentCaptor<String> keycloakRealmArg = ArgumentCaptor.forClass(String.class);
        Mockito.verify(serviceVendorRepository).createOrUpdateServiceVendorWithId(serviceVendorArg.capture(), keycloakRealmArg.capture());

        RecursiveComparisonConfiguration comparisonConfiguration = RecursiveComparisonConfiguration.builder()
                .withIgnoredFields("id", "persisted")
                .build();
        assertThat(serviceVendorArg.getValue()).usingRecursiveComparison(comparisonConfiguration).isEqualTo(serviceVendor);
        assertThat(keycloakRealmArg.getValue()).isEqualTo("fabos");
    }

    @Nested
    @DisplayName("Delete vendor (PUT " + BASE_PATH + ")")
    public class deleteVendor {
        @Test
        @DisplayName("Delete existing vendor")
        public void deleteVendor() throws Exception {
            var serviceVendorId = UUID.randomUUID();
            var path = BASE_PATH + "/" + serviceVendorId;

            var responseBody = mockMvc.perform(delete(path).with(csrf()))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            ArgumentCaptor<UUID> serviceVendorIdArg = ArgumentCaptor.forClass(UUID.class);
            Mockito.verify(serviceVendorRepository).deleteServiceVendorById(serviceVendorIdArg.capture(), any());
            assertThat(serviceVendorIdArg.getValue()).isEqualTo(serviceVendorId);
        }

        @Test
        @DisplayName("Delete non existing vendor")
        public void deleteNonExistingVendorById() throws Exception {
            var serviceVendorId = UUID.randomUUID();
            var path = BASE_PATH + "/" + serviceVendorId;

            Mockito.doThrow(new ServiceVendorNotFoundException(serviceVendorId))
                    .when(serviceVendorRepository).deleteServiceVendorById(any(), any());

            var responseBody = mockMvc.perform(delete(path).with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> assertTrue(result.getResolvedException() instanceof ServiceVendorNotFoundException));
        }
    }
}
