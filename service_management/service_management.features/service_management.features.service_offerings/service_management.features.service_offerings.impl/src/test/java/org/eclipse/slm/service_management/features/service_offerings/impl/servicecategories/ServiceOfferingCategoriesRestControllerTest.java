package org.eclipse.slm.service_management.features.service_offerings.impl.servicecategories;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockJwtAuth;
import com.fasterxml.jackson.core.type.TypeReference;
import org.eclipse.slm.service_management.common.impl.AbstractRestControllerIT;
import org.eclipse.slm.service_management.features.service_offerings.api.servicecategories.ServiceCategoryNotFoundException;
import org.eclipse.slm.service_management.features.service_offerings.api.servicecategories.ServiceOfferingCategory;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServiceOfferingOfferingCategoriesRestController.class)
@ContextConfiguration(
        classes = {
                ServiceOfferingOfferingCategoriesRestController.class
        }
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockJwtAuth
@DisplayName("REST API | Service Categories (/services/offerings/categories)")
public class ServiceOfferingCategoriesRestControllerTest extends AbstractRestControllerIT {

    private final static String BASE_PATH = "/services/offerings/categories";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServiceOfferingCategoryHandler serviceOfferingCategoryHandler;

    @MockBean
    private ServiceCategoryJpaRepository serviceCategoryJpaRepository;

    @Nested
    @DisplayName("Get categories (GET " + BASE_PATH + ")")
    public class getCategories {

        @Test
        @DisplayName("Get all categories when no category is defined")
        public void getCategoriesNoCategoryDefined() throws Exception {
            var path = BASE_PATH;

            var responseBody = mockMvc.perform(get(path).with(csrf()))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            var serviceCategories = objectMapper.readValue(responseBody, new TypeReference<List<ServiceOfferingCategory>>() {
            });

            assertThat(serviceCategories).hasSize(0);
        }

        @Test
        @DisplayName("Get all categories when one category is defined")
        public void getCategoriesOneCategoryDefined() throws Exception {
            var path = BASE_PATH;

            var serviceCategory = new ServiceOfferingCategory("TestCategory");
            Mockito.when(serviceOfferingCategoryHandler.getServiceCategories()).thenReturn(List.of(serviceCategory));

            var responseBody = mockMvc.perform(get(path).with(csrf()))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            var serviceCategories = objectMapper.readValue(responseBody, new TypeReference<List<ServiceOfferingCategory>>() {
            });

            assertThat(serviceCategories).hasSize(1)
                    .usingRecursiveFieldByFieldElementComparator().contains(serviceCategory);
        }

        @Test
        @DisplayName("Get category by id")
        public void getCategoryById() throws Exception {
            var serviceCategory = new ServiceOfferingCategory("TestCategory");
            var path = BASE_PATH + "/" + serviceCategory.getId();

            Mockito.when(serviceOfferingCategoryHandler.getServiceCategoryById(0)).thenReturn(serviceCategory);

            var responseBody = mockMvc.perform(get(path).with(csrf()))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            var receivedServiceCategory = objectMapper.readValue(responseBody, ServiceOfferingCategory.class);

            assertThat(receivedServiceCategory).usingRecursiveComparison().isEqualTo(serviceCategory);
        }

        @Test
        @DisplayName("Get non existing category by id")
        public void getNonExistingCategoryById() throws Exception {
            var serviceCategoryId = 1234;
            var path = BASE_PATH + "/" + serviceCategoryId;

            Mockito.when(serviceOfferingCategoryHandler.getServiceCategoryById(serviceCategoryId))
                    .thenThrow(new ServiceCategoryNotFoundException("Service category with id '" + serviceCategoryId + "' not found"));

            var responseBody = mockMvc.perform(get(path).with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> assertTrue(result.getResolvedException() instanceof ServiceCategoryNotFoundException));
        }
    }

    @Test
    @DisplayName("Create category (POST " + BASE_PATH + ")")
    public void createCategoryUsingPost() throws Exception {
        var path = BASE_PATH;
        var serviceCategory = new ServiceOfferingCategory("TestCategory");
        Mockito.when(serviceOfferingCategoryHandler.createServiceCategory(any())).thenReturn(serviceCategory);

        var responseBody = mockMvc.perform(post(path)
                        .content(asJsonString(serviceCategory))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ArgumentCaptor<ServiceOfferingCategory> serviceCategoryArg = ArgumentCaptor.forClass(ServiceOfferingCategory.class);
        Mockito.verify(serviceOfferingCategoryHandler).createServiceCategory(serviceCategoryArg.capture());
        assertThat(serviceCategoryArg.getValue()).usingRecursiveComparison().isEqualTo(serviceCategory);
    }

    @Test
    @DisplayName("Create or update category (PUT " + BASE_PATH + ")")
    public void createOrUpdateCategoryUsingPut() throws Exception {
        var path = BASE_PATH;
        var serviceCategory = new ServiceOfferingCategory("TestCategory");

        var responseBody = mockMvc.perform(put(path)
                        .content(asJsonString(serviceCategory))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ArgumentCaptor<ServiceOfferingCategory> serviceCategoryArg = ArgumentCaptor.forClass(ServiceOfferingCategory.class);
        Mockito.verify(serviceOfferingCategoryHandler).createOrUpdateServiceCategory(serviceCategoryArg.capture());
        assertThat(serviceCategoryArg.getValue()).usingRecursiveComparison().isEqualTo(serviceCategory);
    }

    @Nested
    @DisplayName("Delete category (PUT " + BASE_PATH + ")")
    public class deleteCategory {
        @Test
        @DisplayName("Delete existing category")
        public void deleteCategory() throws Exception {
            var serviceCategoryId = 1234;
            var path = BASE_PATH + "/" + serviceCategoryId;

            var responseBody = mockMvc.perform(delete(path).with(csrf()))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            ArgumentCaptor<Long> serviceCategoryIdArg = ArgumentCaptor.forClass(Long.class);
            Mockito.verify(serviceOfferingCategoryHandler).deleteCategory(serviceCategoryIdArg.capture());
            assertThat(serviceCategoryIdArg.getValue()).isEqualTo(serviceCategoryId);
        }

        @Test
        @DisplayName("Delete non existing category")
        public void getNonExistingCategoryById() throws Exception {
            var serviceCategoryId = 1234;
            var path = BASE_PATH + "/" + serviceCategoryId;

            Mockito.when(serviceOfferingCategoryHandler.getServiceCategoryById(serviceCategoryId))
                    .thenThrow(new ServiceCategoryNotFoundException("Service category with id '" + serviceCategoryId + "' not found"));

            var responseBody = mockMvc.perform(delete(path).with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> assertTrue(result.getResolvedException() instanceof ServiceCategoryNotFoundException));
        }
    }
}

