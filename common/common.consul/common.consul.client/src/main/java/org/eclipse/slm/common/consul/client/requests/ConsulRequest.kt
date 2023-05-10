package org.eclipse.slm.common.consul.client.requests

import org.eclipse.slm.common.consul.client.requests.parameters.UrlParameters

interface ConsulRequest {
    fun asUrlParameters(): List<UrlParameters?>?
}
