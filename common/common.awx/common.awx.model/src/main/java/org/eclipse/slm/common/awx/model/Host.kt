package org.eclipse.slm.common.awx.model

import java.util.*

data class Host(
        var id: Int,
        var type: String,
        var url: String,
        var related: Map<String, String>,
        var summary_fields: Map<String, Any>,
        var created: Date,
        var modified: Date,
        var name: String,
        var description: String,
        var inventory: Int,
        var enabled: Boolean,
        var instance_id: String,
        var variables: String,
        var has_active_failures: Boolean,
        var has_inventory_sources: Boolean,
        var last_job: String = "",
        var last_job_host_summary: String = "",
        var insights_system_id: String = "",
        var ansible_facts_modified: String = ""
)
