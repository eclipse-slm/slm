package org.eclipse.slm.resource_management.common.access

data class UserContext @JvmOverloads constructor(
    val groups: Set<String>,
    val isAdmin: Boolean,
    val accessToken: String = ""
)
