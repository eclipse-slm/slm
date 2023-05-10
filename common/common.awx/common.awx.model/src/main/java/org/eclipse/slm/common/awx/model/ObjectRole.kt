package org.eclipse.slm.common.awx.model

data class ObjectRole(
    val id: Int,
    val name: String,
    val description: String,
    val type: String,
    val url: String,
    val related: Map<String, String>,
    val summary_fields: Map<String, Any>,)
