package org.eclipse.slm.common.keycloak.config.jwt

class ClaimMappingProperties {
    var jsonPath: String? = null
    var caseProcessing: CaseProcessing = CaseProcessing.UNCHANGED
    var prefix: String = ""
}