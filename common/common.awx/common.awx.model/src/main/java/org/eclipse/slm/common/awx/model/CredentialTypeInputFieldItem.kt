package org.eclipse.slm.common.awx.model

data class CredentialTypeInputFieldItem(
    val id: String,
    val label: String,
    val secret: Boolean,
    val type: String
)
