package org.eclipse.slm.common.consul.model.exceptions

/** Exception indicating that a Consul role with the specified name was not found. */
class ConsulRoleNotFoundException(roleName: String) : RuntimeException("Consul role[name='$roleName' not found.")
