package org.eclipse.slm.common.consul.model.acl

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

/** Represents an ACL policy in Consul
 *
 *  For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl/policies#sample-response">Consul API docs</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Policy(

    @field:JsonProperty("ID", required = false)
    var id: String? = null,

    @field:JsonProperty("Name")
    var name: String = "",
    
    @field:JsonProperty("Description")
    var description: String = "",
    
    @field:JsonProperty("Rules")
    var rules: String? = null,
    
    @field:JsonProperty("Datacenters")
    var datacenters: List<String>? = null,
    
    @field:JsonProperty("Hash")
    var hash: String? = null,
) {
    companion object {
        @JvmStatic
        fun builder(name: String) = Builder(name)
    }

    class Builder(private val name: String) {
        private var id: String? = null
        private var description: String = ""
        private var rules: String? = null
        private var datacenters: List<String>? = null
        private var hash: String? = null

        fun id(id: String?) = apply { this.id = id }
        fun description(description: String) = apply { this.description = description }
        fun rules(rules: String?) = apply { this.rules = rules }
        fun datacenters(datacenters: List<String>?) = apply { this.datacenters = datacenters }
        fun hash(hash: String?) = apply { this.hash = hash }

        fun build() = Policy(id, name, description, rules, datacenters, hash)
    }
}
