package org.eclipse.slm.service_management.model.offerings.options

import com.fasterxml.jackson.annotation.JsonFormat

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class DeploymentVariableType(val key: String, val prettyName: String) {
    TARGET_RESOURCE_ID("TARGET_RESOURCE_ID", "Target Resource Id"),
}
