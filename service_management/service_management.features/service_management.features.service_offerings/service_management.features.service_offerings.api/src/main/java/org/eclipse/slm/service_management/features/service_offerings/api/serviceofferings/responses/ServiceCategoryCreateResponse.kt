package org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.responses

import com.fasterxml.jackson.annotation.JsonProperty

data class ServiceCategoryCreateResponse (

    @JsonProperty("serviceCategoryId")
    val serviceCategoryId: Long

)
{
}
