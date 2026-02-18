package org.eclipse.slm.common.consul.model.exceptions

/** General runtime exception for Consul-related errors. */
class ConsulRuntimeException(message: String, cause: Throwable) : RuntimeException(message, cause)
