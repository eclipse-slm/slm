package org.eclipse.slm.resource_management.service.rest.resources

import org.eclipse.slm.resource_management.service.rest.aas.resources.digitalnameplate.DigitalNameplateV3

class CreateResourceRequest() {
    var resourceHostname: String? = null
    var resourceIp: String? = null
    var digitalNameplateV3: DigitalNameplateV3?  = null
}