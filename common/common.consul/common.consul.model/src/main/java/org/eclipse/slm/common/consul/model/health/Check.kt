package org.eclipse.slm.common.consul.model.health

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a check of a node or service in Consul.
 *
 * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/health#sample-response">Consul API docs</a>.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Check(

    /** Unique identifier for the health check instance. */
    @field:JsonProperty("ID")
    val id: String? = null,

    /** Name of the node associated with the check. */
    @field:JsonProperty("Node")
    val node: String? = null,

    /** Check identifier (e.g., "serfHealth" or "service:<name>"). */
    @field:JsonProperty("CheckID")
    val checkId: String? = null,

    /** Human-readable check name. */
    @field:JsonProperty("Name")
    val name: String? = null,

    /** Current status of the check (e.g., "passing", "warning", "critical"). */
    @field:JsonProperty("Status")
    val status: String? = null,

    /** Additional notes for the check. */
    @field:JsonProperty("Notes")
    val notes: String? = null,

    /** Output from the last check execution. */
    @field:JsonProperty("Output")
    val output: String? = null,

    /** ID of the associated service. */
    @field:JsonProperty("ServiceID")
    val serviceId: String? = null,

    /** Name of the associated service. */
    @field:JsonProperty("ServiceName")
    val serviceName: String? = null,

    /** Tags of the associated service. */
    @field:JsonProperty("ServiceTags")
    val serviceTags: List<String> = emptyList(),

    /** Namespace of this check. */
    @field:JsonProperty("Namespace")
    val namespace: String? = null
)