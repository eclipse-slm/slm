package org.eclipse.slm.common.consul.client.requests.parameters

interface UrlParameters {
    fun toUrlParameters(): List<String?>?
}
