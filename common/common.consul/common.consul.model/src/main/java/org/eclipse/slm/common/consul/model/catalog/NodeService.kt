package org.eclipse.slm.common.consul.model.catalog

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

/** NodeService represents a service running on a Consul node.
 *
 * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/catalog#sample-response-5">Consul API docs</a>.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
open class NodeService(

    /** The unique ID of the service */
    @field:JsonProperty("ID")
    open var id: String,

    /** The name of the service */
    @field:JsonProperty("Service")
    open val serviceName: String = "",

    /** The address of the service */
    @field:JsonProperty("TaggedAddresses")
    open val taggedAddresses: TaggedAddresses? = null,

    /** The list of tags associated with the service */
    @field:JsonProperty("Tags")
    open val tags: List<String>? = ArrayList(),

    /** User-defined metadata key/value pairs for the service */
    @field:JsonProperty("Meta")
    open val meta: Map<String, String>? = HashMap(),

    /** The port on which the service is running */
    @field:JsonProperty("Port")
    var port: Int? = null,

    /** <ENTERPRISE FEATURE> Specifies the namespace of the service. */
    @field:JsonProperty("Namespace")
    val namespace: String? = null,
)
{
}
