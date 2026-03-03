package org.eclipse.slm.common.consul.model.exceptions

import java.util.UUID

/** Exception indicating that a Consul service with the specified name on a given node was not found. */
class ConsulServiceNotFoundException : RuntimeException {

    constructor(nodeName: String, serviceName: String) :
        super("Consul service[name='$serviceName' on node[name='$nodeName'] not found.")

    constructor(serviceId: UUID) :
        super("Consul service[id='$serviceId'] not found.")
}
