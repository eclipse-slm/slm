package org.eclipse.slm.resource_management.features.capabilities.model

import java.net.URLEncoder
import java.util.*

class CapabilityServiceFilter private constructor(
    val capabilityServiceId: UUID?,
    val capabilityHostType: CapabilityHostType?
) {
    data class Builder(
        var capabilityServiceId: UUID? = null,
        var capabilityHostType: CapabilityHostType? = null) {

        fun capabilityId(capabilityId: UUID) = apply { this.capabilityServiceId = capabilityId }
        fun capabilityHostType(capabilityHostType: CapabilityHostType) = apply { this.capabilityHostType = capabilityHostType }
        fun build() = CapabilityServiceFilter(capabilityServiceId, capabilityHostType)
    }

    enum class CapabilityHostType {
        SINGLE_HOST,
        MULTI_HOST
    }

    @Override
    override fun toString(): String {
        var queryString = ""

        var queryParams = mutableMapOf<String, Any>()
        capabilityServiceId?.let { queryParams.put("capabilityServiceId", capabilityServiceId) }
        capabilityHostType?.let { queryParams.put("capabilityHostType", capabilityHostType) }

        queryString = queryParams.entries.stream()
            .map { p -> URLEncoder.encode(p.key, "UTF-8") + "=" + URLEncoder.encode(p.value.toString(), "UTF-8") }
            .reduce { p1, p2 -> p1.toString() + "&" + p2 }
            .orElse("")

        return queryString
    }
}
