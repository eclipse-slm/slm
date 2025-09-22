package org.eclipse.slm.service_management.model.vendors

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.eclipse.slm.common.model.AbstractBaseEntityUuid
import java.util.*

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

    @Column(name = "logo", columnDefinition="MEDIUMBLOB")
    var logo: ByteArray? = null

    @JsonIgnore
    fun getKeycloakGroupName(): String {
        return KEYCLOAK_GROUP_PREFIX + this.id
    }
}
