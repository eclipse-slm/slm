package org.eclipse.slm.service_management.model.service_repositories

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class ServiceRepositoryCreateResponse (

    @JsonProperty("serviceRepositoryId")
    val serviceRepositoryId: UUID

)
{

}
