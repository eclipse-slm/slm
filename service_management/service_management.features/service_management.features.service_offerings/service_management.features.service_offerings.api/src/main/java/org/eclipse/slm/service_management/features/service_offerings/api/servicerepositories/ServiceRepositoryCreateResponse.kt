package org.eclipse.slm.service_management.features.service_offerings.api.servicerepositories

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class ServiceRepositoryCreateResponse (

    @JsonProperty("serviceRepositoryId")
    val serviceRepositoryId: UUID

)
{

}
