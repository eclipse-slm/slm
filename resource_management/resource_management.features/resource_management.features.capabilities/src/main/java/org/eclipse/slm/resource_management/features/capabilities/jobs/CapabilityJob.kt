package org.eclipse.slm.resource_management.features.capabilities.jobs

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
class CapabilityJob(id: UUID? = null,
                    resourceId: UUID?,
                    capabilityId: UUID?,
                    skipInstall: Boolean = false,
)
    : AbstractBaseEntityUuid(id) {

    @Column(name = "resource_id", nullable = false)
    var resourceId: UUID? = resourceId

    @Column(name = "capability_id", nullable = false)
    var capabilityId: UUID? = capabilityId

    @Column(name = "skip_install", nullable = false)
    var skipInstall: Boolean = skipInstall

    @Column(name = "config_parameters", columnDefinition = "LONGTEXT")
    @JdbcTypeCode(SqlTypes.JSON)
    var configParameters: MutableMap<String, String> = mutableMapOf()

    @Column(name = "state", nullable = false)
    var state: CapabilityJobState? = CapabilityJobState.CREATED

    @Column(name = "created_at", nullable = false)
    var createdAt: Date = Date()

    @OneToMany(mappedBy = "capabilityJob", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    var stateTransitions: MutableList<CapabilityJobStateTransition> = mutableListOf()

    @Column(name = "log_messages", columnDefinition = "LONGTEXT")
    @JdbcTypeCode(SqlTypes.JSON)
    var logMessages: MutableList<String> = mutableListOf()

    protected constructor()
    : this(null, null, null)

    fun addLogMessage(message: String) {
            logMessages.add(message);
    }

 }
