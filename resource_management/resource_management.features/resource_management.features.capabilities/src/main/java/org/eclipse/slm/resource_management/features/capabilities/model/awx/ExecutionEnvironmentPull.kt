package org.eclipse.slm.resource_management.features.capabilities.model.awx

import com.fasterxml.jackson.annotation.JsonValue


enum class ExecutionEnvironmentPull(@JsonValue val prettyName: String = "") {
    None(""),
    Always("always"),
    Missing("missing"),
    Never("never")
}