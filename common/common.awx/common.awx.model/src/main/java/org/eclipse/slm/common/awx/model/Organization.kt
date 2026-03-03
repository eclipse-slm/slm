package org.eclipse.slm.common.awx.model

import java.util.*

data class Organization(
        var id: Int = 0,
        var type: String = "",
        var url: String = "",
        var related: Map<String, String> = emptyMap(),
        var summary_fields: Map<String, Any>? = null,
        var created: Date = Date(),
        var modified: Date = Date(),
        var name: String = "",
        var description: String = "",
        var max_hosts: Int = 0,
        var custom_virtualenv: String? = null,
        var default_environment: String? = null,
)
