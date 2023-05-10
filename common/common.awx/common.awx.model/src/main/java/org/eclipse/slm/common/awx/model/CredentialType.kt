package org.eclipse.slm.common.awx.model

import java.util.*

data class CredentialType(
    val id: Int,
    val type: String,
    val url: String,
    val related: Map<String,String>,
    val summary_fields: Map<String,Any>,
    val created: Date,
    val modified: Date,
    val name: String,
    val description: String,
    val kind: String,
    val namespace: String?,
    val managed_by_tower: Boolean,
    val inputs: Map<String, Any>,
    val injectors: Map<String, Map<String, String>>?
)
