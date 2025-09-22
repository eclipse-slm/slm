package org.eclipse.slm.resource_management.features.capabilities.jobs

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.eclipse.slm.common.model.StateTransition
import java.util.*

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class CapabilityJobStateTransition(

    fromState: CapabilityJobState?,

    toState: CapabilityJobState?,

    timestamp: Date = Date(),

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "firmware_update_job_id", nullable = false)
    var capabilityJob: CapabilityJob? = null


) : StateTransition(fromState.toString(), toState.toString(), timestamp) {
    protected constructor()
            : this(null, null, Date(), null)
}