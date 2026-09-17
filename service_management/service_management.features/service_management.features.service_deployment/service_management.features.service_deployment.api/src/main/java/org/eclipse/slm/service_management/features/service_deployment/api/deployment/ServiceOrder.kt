package org.eclipse.slm.service_management.features.service_deployment.api.deployment

import jakarta.persistence.*
import org.eclipse.slm.common.model.AbstractBaseEntityUuid
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.options.ServiceOptionValue
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*

@Entity
@Table(name = "service_order")
class ServiceOrder(id: UUID? = null) : AbstractBaseEntityUuid(id) {

    var created: Date = Calendar.getInstance().time

    @Column(length = 36, nullable = false)
    var serviceInstanceId: UUID? = null

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "LONGTEXT")
    var serviceOptionValues: List<ServiceOptionValue> = ArrayList()

    @Column(name = "deployment_target_submodel_id", length = 255, nullable = true)
    var deploymentTargetSubmodelId: String? = null

    @Column(name = "deployment_job_id", length = 255, nullable = true)
    var deploymentJobId: String? = null

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var serviceOrderResult: ServiceOrderResult? = null

}
