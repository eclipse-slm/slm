package org.eclipse.slm.common.vault.model.acl

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class Policy {
    var name: String? = ""
    var rules: String? = ""

    constructor(name: String, rules: String) {
        this.name = name
        this.rules = rules
    }

    constructor() {
    }
}
