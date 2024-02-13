package org.eclipse.slm.service_management.model.offerings.codesys

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import org.eclipse.slm.common.model.DeploymentType
import org.eclipse.slm.service_management.model.offerings.DeploymentDefinition

@JsonTypeName("CODESYS")
class CodesysDeploymentDefinition : DeploymentDefinition(DeploymentType.CODESYS) {

}
