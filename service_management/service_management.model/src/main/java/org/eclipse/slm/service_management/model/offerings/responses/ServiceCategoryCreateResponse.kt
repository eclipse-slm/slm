package org.eclipse.slm.service_management.model.offerings.responses

import com.fasterxml.jackson.annotation.JsonProperty

data class ServiceCategoryCreateResponse (

    @JsonProperty("serviceCategoryId")
    val serviceCategoryId: Long

)
{
}
