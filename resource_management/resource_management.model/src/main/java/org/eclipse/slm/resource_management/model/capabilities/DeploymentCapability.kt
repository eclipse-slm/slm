package org.eclipse.slm.resource_management.model.capabilities

import com.fasterxml.jackson.annotation.JsonTypeName
import com.vladmihalcea.hibernate.type.json.JsonStringType
import org.eclipse.slm.common.model.DeploymentType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "deployment_capability")
@TypeDefs(TypeDef(name = "json", typeClass = JsonStringType::class))
@JsonTypeName("DeploymentCapability")
class DeploymentCapability(id: UUID? = null) : Capability(id) {

    @Column(name = "supported_deployment_types")
    @Type(type = "json")
    open var supportedDeploymentTypes: List<DeploymentType> = ArrayList()

}
