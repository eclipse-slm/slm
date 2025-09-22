package org.eclipse.slm.resource_management.features.device_integration.firmware_update.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
import org.eclipse.slm.common.model.AbstractBaseEntityUuid
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class FirmwareUpdateJob(id: UUID? = null,
                        resourceId: UUID?,
                        driverId: String?,
                        softwareNameplateId: String?,
                        userId: String?
)
    : AbstractBaseEntityUuid(id) {

    @Column(name = "resource_id", nullable = false)
    var resourceId: UUID? = resourceId

    @Column(name = "driver_id", nullable = false)
    var driverId: String? = driverId

    @Column(name = "software_nameplate_id", nullable = false)
    var softwareNameplateId: String? = softwareNameplateId

    @Column(name = "user_id", nullable = false)
    var userId: String? = userId

    @Column(name = "state", nullable = false)
    var state: FirmwareUpdateJobState? = FirmwareUpdateJobState.CREATED

    @Column(name = "created_at", nullable = false)
    var createdAt: Date = Date()

    @OneToMany(mappedBy = "firmwareUpdateJob", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    var stateTransitions: MutableList<FirmwareUpdateJobStateTransition> = mutableListOf()

    @Column(name = "log_messages", columnDefinition = "LONGTEXT")
    @JdbcTypeCode(SqlTypes.JSON)
    var logMessages: MutableList<String> = mutableListOf()

    protected constructor()
    : this(null, null, null, null, null)

    fun addLogMessage(message: String) {
            logMessages.add(message);
    }

 }
