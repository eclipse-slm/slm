package org.eclipse.slm.service_management.model.offerings

import com.vladmihalcea.hibernate.type.json.JsonStringType
import org.eclipse.slm.service_management.model.AbstractBaseEntityUuid
import org.eclipse.slm.service_management.model.offerings.options.ServiceOptionValue
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "service_order")
@TypeDefs(TypeDef(name = "json", typeClass = JsonStringType::class))
class ServiceOrder(id: UUID? = null) : AbstractBaseEntityUuid(id) {

    var created: Date = Calendar.getInstance().time;

    @Type(type="uuid-char")
    @Column(length = 36, nullable = false)
    var serviceInstanceId: UUID? = null

    @Type(type = "json")
    @Column(columnDefinition = "LONGTEXT")
    var serviceOptionValues: List<ServiceOptionValue> = ArrayList()

    @Type(type="uuid-char")
    @Column(length = 36, nullable = false)
    var deploymentCapabilityServiceId: UUID? = null

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var serviceOrderResult: ServiceOrderResult? = null

}
