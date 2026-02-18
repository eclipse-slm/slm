package org.eclipse.slm.common.consul.model.exceptions

/** Exception indicating that a Consul policy with the specified name was not found. */
class ConsulPolicyNotFoundException(policyName: String) : RuntimeException("Consul policy[name='$policyName' not found.")
