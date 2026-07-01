package org.eclipse.slm.service_management.features.service_deployment.api.deploymentdefinitions.dockercompose

class DockerComposeFileDependsOnDefinition(val serviceName: String)
{
    constructor(serviceName: String, condition: DockerComposeFileDependsOnConditionType) : this(serviceName) {
        this.condition = condition
    }

    var condition: DockerComposeFileDependsOnConditionType? = null
}
