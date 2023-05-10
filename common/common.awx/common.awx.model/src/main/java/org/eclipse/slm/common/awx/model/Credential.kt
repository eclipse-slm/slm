package org.eclipse.slm.common.awx.model

import java.util.*

data class Credential(
    val id: Int,
    val type: String,
    val url: String,
    val related: Map<String,String>,
    val summary_fields: Map<String, Any>,
    val created: Date,
    val modified: Date,
    val name: String,
    val description: String,
    val organization: Int,
    val credential_type: Int,
    val managed_by_tower: Boolean,
    val inputs: Map<String, String>,
    val kind: String?,
    val cloud: Boolean?,
    val kubernetes: Boolean?
)
