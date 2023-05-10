package org.eclipse.slm.common.vault.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
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
