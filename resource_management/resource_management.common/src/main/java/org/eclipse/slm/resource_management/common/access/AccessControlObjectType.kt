package org.eclipse.slm.resource_management.common.access

/**
 * Object types this service registers in [org.eclipse.slm.common.access.AccessControlPolicy].
 * Values match the strings already stored in the policy_objects.object_type column.
 */
object AccessControlObjectType {
    const val RESOURCE = "RESOURCE"
    const val CAPABILITY_SERVICE = "CAPABILITY_SERVICE"
    const val REMOTE_ACCESS = "REMOTE_ACCESS"
}
