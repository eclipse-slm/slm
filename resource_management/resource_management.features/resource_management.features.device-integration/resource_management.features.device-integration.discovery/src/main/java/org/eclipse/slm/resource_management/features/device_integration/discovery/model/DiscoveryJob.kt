package org.eclipse.slm.resource_management.features.device_integration.discovery.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.springframework.format.annotation.DateTimeFormat
import java.util.*

@Entity
class DiscoveryJob {

    @JsonProperty
    @Id
    @Column(name = "id", length = 36, unique = true, nullable = false)
    var id: UUID? = UUID.randomUUID()

    @JsonProperty
    var driverId: String? = null

    @JsonProperty
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var startDate: Date? = Calendar.getInstance().getTime()

    @JsonProperty
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var finishDate: Date? = null

    @JsonProperty
    var state: DiscoveryJobState? = DiscoveryJobState.CREATED

    @JsonProperty
    @Column(columnDefinition = "LONGTEXT")
    @JdbcTypeCode(SqlTypes.JSON)
    var discoveryResult: MutableList<DiscoveredResource?>? = ArrayList<DiscoveredResource?>()

    constructor(driverId: String?) {
        this.driverId = driverId
    }

    constructor()
}
