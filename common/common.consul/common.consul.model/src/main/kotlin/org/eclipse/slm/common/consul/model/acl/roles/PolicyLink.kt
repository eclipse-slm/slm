package org.eclipse.slm.common.consul.model.acl

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

/** Represents a link to an ACL policy in Consul
 *
 *  For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl/roles#policies">Consul API docs</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class PolicyLink(
    @field:JsonProperty("ID")
    /** Policy identifier. */
    val id: String? = null,

    @field:JsonProperty("Name")
    /** Policy name. */
    val name: String? = null
) {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var id: String? = null
        private var name: String? = null

        fun id(id: String?) = apply { this.id = id }
        fun name(name: String?) = apply { this.name = name }

        fun build(): PolicyLink = PolicyLink(
            id = id,
            name = name
        )
    }
}
