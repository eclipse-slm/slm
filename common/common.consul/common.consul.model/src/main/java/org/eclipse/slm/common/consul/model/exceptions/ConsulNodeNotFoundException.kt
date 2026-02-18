package org.eclipse.slm.common.consul.model.exceptions

import java.util.*

/** Exception indicating that a Consul node with the specified ID was not found. */
class ConsulNodeNotFoundException(nodeId: UUID) : RuntimeException("Consul node[id='$nodeId'] not found.")
