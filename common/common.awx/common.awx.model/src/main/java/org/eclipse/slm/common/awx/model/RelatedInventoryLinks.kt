package org.eclipse.slm.common.awx.model

data class RelatedInventoryLinks (
    var created_by: String,
    var modified_by: String = "",
    var hosts: String,
    var groups: String,
    var root_groups: String,
    var variable_data: String,
    var script: String,
    var tree: String,
    var inventory_sources: String,
    var update_inventory_sources: String,
    var activity_stream: String,
    var job_templates: String,
    var ad_hoc_commands: String,
    var access_list: String,
    var object_roles: String,
    var instance_groups: String,
    var copy: String,
    var organization: String,
    var labels: String
)
