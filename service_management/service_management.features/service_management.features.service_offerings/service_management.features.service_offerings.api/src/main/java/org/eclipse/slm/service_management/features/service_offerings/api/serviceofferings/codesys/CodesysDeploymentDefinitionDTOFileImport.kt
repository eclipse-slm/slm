package org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.codesys

import com.fasterxml.jackson.annotation.JsonTypeName
import org.eclipse.slm.common.model.DeploymentType
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.DeploymentDefinitionDTOFileImport
import java.util.*

@JsonTypeName("CODESYS")
class CodesysDeploymentDefinitionDTOFileImport(id: UUID? = null) : DeploymentDefinitionDTOFileImport(DeploymentType.CODESYS) {

    var manifestFilename = ""

}
