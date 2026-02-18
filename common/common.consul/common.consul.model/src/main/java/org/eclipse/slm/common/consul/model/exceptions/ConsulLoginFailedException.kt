package org.eclipse.slm.common.consul.model.exceptions

/** Exception indicating that a login attempt to Consul has failed. */
class ConsulLoginFailedException(message: String) : RuntimeException(message)
