package org.eclipse.slm.resource_management.common.resources

import org.eclipse.slm.resource_management.common.aas.submodels.digitalnameplate.DigitalNameplateV3

class CreateResourceRequest() {
    var resourceHostname: String? = null
    var resourceIp: String? = null
    var digitalNameplateV3: DigitalNameplateV3?  = null
}