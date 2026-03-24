package org.eclipse.slm.service_management.model.offerings.codesys

import com.fasterxml.jackson.annotation.JsonTypeName
import org.eclipse.slm.common.model.DeploymentType
import org.eclipse.slm.service_management.model.offerings.DeploymentDefinitionDTOFileImport
import java.util.*

@JsonTypeName("CODESYS")
class CodesysDeploymentDefinitionDTOFileImport(id: UUID? = null) : DeploymentDefinitionDTOFileImport(DeploymentType.CODESYS) {

    var manifestFilename = ""

}
