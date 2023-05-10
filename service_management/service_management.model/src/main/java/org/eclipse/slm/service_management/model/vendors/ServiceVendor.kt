package org.eclipse.slm.service_management.model.vendors

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.eclipse.slm.service_management.model.AbstractBaseEntityUuid
import org.hibernate.annotations.Type
import java.util.*
import javax.persistence.*
import kotlin.collections.ArrayList
import kotlin.jvm.Transient

@Entity
@Table(name = "service_vendor")
class ServiceVendor(id: UUID? = null) : AbstractBaseEntityUuid(id) {

    companion object {
        val KEYCLOAK_GROUP_PREFIX = "vendor_";
        fun getKeycloakGroupPrefix():String { return "vendor_" }
    }

    @Column(name = "name")
    var name = ""

    @Column(name = "description")
    var description = ""

    @Lob
    @Column(name = "logo")
    var logo: ByteArray? = null

    @JsonIgnore
    fun getKeycloakGroupName(): String {
        return KEYCLOAK_GROUP_PREFIX + this.id
    }
}
