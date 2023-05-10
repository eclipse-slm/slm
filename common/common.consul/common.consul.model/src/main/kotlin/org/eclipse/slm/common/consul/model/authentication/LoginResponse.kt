package org.eclipse.slm.common.consul.model.authentication

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.slm.common.consul.model.acl.Role

@JsonIgnoreProperties(ignoreUnknown = true)
class LoginResponse {

    @JsonProperty("AccessorID")
    var accessorID: String? = null

    @JsonProperty("SecretID")
    var secretID: String? = null

    @JsonProperty("Description")
    var description: String? = null

    @JsonProperty("Roles")
    var roles: List<Role>? = null

    @JsonProperty("Local")
    var local: Boolean? = null

    @JsonProperty("AuthMethod")
    var authMethod: String? = null

    @JsonProperty("CreateTime")
    var createTime: String? = null

    @JsonProperty("Hash")
    var hash: String? = null

    @JsonProperty("CreateIndex")
    var createIndex = 0

    @JsonProperty("ModifyIndex")
    var modifyIndex = 0

    constructor() {}
    constructor(
        accessorID: String?,
        secretID: String?,
        description: String?,
        roles: List<Role>?,
        local: Boolean?,
        authMethod: String?,
        createTime: String?,
        hash: String?,
        createIndex: Int,
        modifyIndex: Int
    ) {
        this.accessorID = accessorID
        this.secretID = secretID
        this.description = description
        this.roles = roles
        this.local = local
        this.authMethod = authMethod
        this.createTime = createTime
        this.hash = hash
        this.createIndex = createIndex
        this.modifyIndex = modifyIndex
    }
}
