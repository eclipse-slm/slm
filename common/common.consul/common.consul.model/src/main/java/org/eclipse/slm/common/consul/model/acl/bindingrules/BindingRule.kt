package org.eclipse.slm.common.consul.model.acl.bindingrules

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

/** Represents a Consul ACL binding rule.
 *
 * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl/binding-rules">Consul API Docs</a>.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class BindingRule(

    /** The ID of the binding rule. */
    @field:JsonProperty("ID")
    val id: String? = null,

    /** Free form human readable description of the binding rule. */
    @field:JsonProperty("Description")
    var description: String = "",

    /** The name of the auth method that this rule applies to. This field is immutable. */
    @field:JsonProperty("AuthMethod")
    var authMethod: String = "",

    /** Specifies the expression used to match this rule against valid identities returned from an auth method validation. If empty this binding rule matches
     *  all valid identities returned from the auth method. */
    @field:JsonProperty("Selector")
    var selector: String = "",

    /**
     * Specifies the way the binding rule affects a token created at login. You can specify one of the following: service, node, role, templated-policy
     * For more information, see <a href="https://developer.hashicorp.com/consul/api-docs/acl/binding-rules#bindtype">Consul API Docs</a>.
     */
    @field:JsonProperty("BindType")
    var bindType: String = "",

    /** The name to bind to a token at login-time. What it binds to can be adjusted with different values of the BindType field. This can either be a plain
     *  string or lightly templated using HIL syntax to interpolate the same values that are usable by the Selector syntax. */
    @field:JsonProperty("BindName")
    var bindName: String = ""
)
