package org.eclipse.slm.resource_management.features.capabilities.model

import com.fasterxml.jackson.annotation.JsonTypeName
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.eclipse.slm.common.model.DeploymentType
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*

@Entity
@Table(name = "deployment_capability")
@JsonTypeName("DeploymentCapability")
open class DeploymentCapability(id: UUID? = null) : Capability(id) {

    @Column(name = "supported_deployment_types")
    @JdbcTypeCode(SqlTypes.JSON)
    open var supportedDeploymentTypes: List<DeploymentType> = ArrayList()

}
