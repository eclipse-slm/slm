package org.eclipse.slm.common.awx.model

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*

data class Inventory(
        var id: Int,
        var type: String,
        var url: String,
        var related: RelatedInventoryLinks,
        @JsonIgnore var summary_fields: String = "",
        var created: Date,
        var modified: Date,
        var name: String,
        var description: String,
        var organization: Int,
        var kind: String,
        var host_filter: String? = "",
        var variables: String,
        var has_active_failures: Boolean,
        var total_hosts: Int,
        var hosts_with_active_failures: Int,
        var total_groups: Int,
        var has_inventory_sources: Boolean,
        var total_inventory_sources: Int,
        var inventory_sources_with_failures: Int,
        var insights_credential: String? = "",
        var pending_deletion: Boolean
)
