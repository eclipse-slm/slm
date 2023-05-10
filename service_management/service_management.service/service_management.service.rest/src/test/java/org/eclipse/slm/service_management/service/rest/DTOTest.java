package org.eclipse.slm.service_management.service.rest;

import org.eclipse.slm.common.utils.objectmapper.ObjectMapperUtils;
import org.eclipse.slm.service_management.model.offerings.ServiceCategory;
import org.eclipse.slm.service_management.model.offerings.ServiceOffering;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingDTOApi;
import org.eclipse.slm.service_management.model.vendors.ServiceVendor;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class DTOTest {

    @BeforeAll
    public static void init()
    {
        DTOConfig.configureModelMapper();
    }

    @Nested
    public class ServiceOfferingMapping {

        @Test
        public void mapEntityToDTOApi(){
            var serviceCategory = new ServiceCategory(UUID.randomUUID().toString());
            var serviceVendor = new ServiceVendor(UUID.randomUUID());
            serviceVendor.setName(UUID.randomUUID().toString());
            serviceVendor.setDescription(UUID.randomUUID().toString());
            var serviceOffering = new ServiceOffering();
            serviceOffering.setId(UUID.randomUUID());
            serviceOffering.setName(UUID.randomUUID().toString());
            serviceOffering.setDescription(UUID.randomUUID().toString());
            serviceOffering.setShortDescription(UUID.randomUUID().toString());
            serviceOffering.setServiceVendor(serviceVendor);
            serviceOffering.setServiceCategory(serviceCategory);

            final var expectedServiceOfferingDTOApi = new ServiceOfferingDTOApi(
                    serviceOffering.getName(),
                    serviceOffering.getDescription(),
                    serviceOffering.getShortDescription(),
                    0L,
                    serviceVendor.getId());
            expectedServiceOfferingDTOApi.setId(serviceOffering.getId());

            var serviceOfferingDTOApi = ObjectMapperUtils.map(serviceOffering, ServiceOfferingDTOApi.class);

            var compareConfig = RecursiveComparisonConfiguration.builder()
                    .withIgnoredFields("persisted").build();
            assertThat(serviceOfferingDTOApi).usingRecursiveComparison(compareConfig).isEqualTo(expectedServiceOfferingDTOApi);
        }
    }
}
