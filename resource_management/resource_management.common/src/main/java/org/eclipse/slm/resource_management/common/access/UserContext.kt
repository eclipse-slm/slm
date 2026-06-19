package org.eclipse.slm.resource_management.common.access

data class UserContext(
    val groups: Set<String>,
    val isAdmin: Boolean
)
