package org.eclipse.slm.resource_management.features.capabilities.model

import java.net.URLEncoder
import java.util.*

class CapabilityFilter private constructor(
    val capabilityId: UUID?,
    val capabilityHostType: CapabilityHostType?
) {
    data class Builder(
        var capabilityId: UUID? = null,
        var capabilityHostType: CapabilityHostType? = null) {

        fun capabilityId(capabilityId: UUID) = apply { this.capabilityId = capabilityId }
        fun capabilityHostType(capabilityHostType: CapabilityHostType) = apply { this.capabilityHostType = capabilityHostType }
        fun build() = CapabilityFilter(capabilityId, capabilityHostType)
    }

    enum class CapabilityHostType {
        SINGLE_HOST,
        MULTI_HOST
    }

    @Override
    override fun toString(): String {
        var queryString = ""

        var queryParams = mutableMapOf<String, Any>()
        capabilityId?.let { queryParams.put("capabilityId", capabilityId) }
        capabilityHostType?.let { queryParams.put("capabilityHostType", capabilityHostType) }

        queryString = queryParams.entries.stream()
            .map { p -> URLEncoder.encode(p.key, "UTF-8") + "=" + URLEncoder.encode(p.value.toString(), "UTF-8") }
            .reduce { p1, p2 -> p1.toString() + "&" + p2 }
            .orElse("")

        return queryString
    }
}
