package org.eclipse.slm.service_management.model.offerings.kubernetes

import com.fasterxml.jackson.annotation.JsonTypeName
import org.eclipse.slm.common.model.DeploymentType
import org.eclipse.slm.service_management.model.offerings.DeploymentDefinitionDTOFileImport
import java.util.*

@JsonTypeName("KUBERNETES")
class KubernetesDeploymentDefinitionDTOFileImport(id: UUID? = null) : DeploymentDefinitionDTOFileImport(DeploymentType.KUBERNETES) {

    var manifestFilename = ""

}
