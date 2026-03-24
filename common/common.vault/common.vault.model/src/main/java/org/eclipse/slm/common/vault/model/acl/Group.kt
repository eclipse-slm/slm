package org.eclipse.slm.common.vault.model.acl

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import org.eclipse.slm.common.vault.model.auth.JwtGroupAlias

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Group(
    var alias: JwtGroupAlias? = null,

    var creationTime: String? = null,

    var id: String? = null,

    var lastUpdateTime: String? = null,

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    var memberEntityIds: List<String> = emptyList(),

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    var memberGroupIds: List<String> = emptyList(),

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    var metadata: Map<String, String> = emptyMap(),

    var modifyIndex: Int? = null,

    var name: String? = null,

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    var parentGroupIds: List<String> = emptyList(),

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    var policies: List<String>? = emptyList(),

    var type: String? = null
)
