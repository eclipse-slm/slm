package org.eclipse.slm.resource_management.features.providers

import org.eclipse.slm.common.model.DeploymentType
import java.net.URLEncoder
import java.util.*

class ServiceHosterFilter private constructor(
    val capabilityServiceId: UUID?,
    val supportedDeploymentType: DeploymentType?,
    val capabilityHostType: CapabilityHostType?
) {

    data class Builder(
        var capabilityServiceId: UUID? = null,
        var supportedDeploymentType: DeploymentType? = null,
        var capabilityHostType: CapabilityHostType? = null) {

        fun capabilityServiceId(capabilityServiceId: UUID) = apply { this.capabilityServiceId = capabilityServiceId }
        fun supportedDeploymentType(supportedDeploymentType: DeploymentType) = apply { this.supportedDeploymentType = supportedDeploymentType }
        fun capabilityHostType(capabilityHostType: CapabilityHostType) = apply { this.capabilityHostType = capabilityHostType }
        fun build() = ServiceHosterFilter(capabilityServiceId, supportedDeploymentType, capabilityHostType)
    }

    enum class CapabilityHostType {
        SINGLE_HOST,
        MULTI_HOST
    }

    @Override
    override fun toString(): String {
        var queryParams = mutableMapOf<String, Any>()
        capabilityServiceId?.let { queryParams.put("capabilityServiceId", capabilityServiceId) }
        supportedDeploymentType?.let { queryParams.put("supportedDeploymentType", supportedDeploymentType) }
        capabilityHostType?.let { queryParams.put("capabilityHostType", capabilityHostType) }

        var queryString = queryParams.entries.stream()
            .map { p -> URLEncoder.encode(p.key, "UTF-8") + "=" + URLEncoder.encode(p.value.toString(), "UTF-8") }
            .reduce { p1, p2 -> p1.toString() + "&" + p2 }
            .orElse("")

        return queryString
    }
}
