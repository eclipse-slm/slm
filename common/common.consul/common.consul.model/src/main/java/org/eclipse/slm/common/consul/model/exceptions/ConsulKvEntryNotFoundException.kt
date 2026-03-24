package org.eclipse.slm.common.consul.model.exceptions

/** Exception thrown when a specific key-value entry is not found in Consul. */
class ConsulKvEntryNotFoundException(message: String, cause: Throwable) : RuntimeException(message, cause)
